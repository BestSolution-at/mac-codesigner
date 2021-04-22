package at.bestsolution.maccodesigner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.jboss.logging.Logger;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import picocli.CommandLine;

@QuarkusMain
@CommandLine.Command(name = "demo", mixinStandardHelpOptions = true)
public class MacCodesignerMain implements Runnable, QuarkusApplication {
	private static Logger LOG = Logger.getLogger(MacCodesignerMain.class);

	@CommandLine.Option(names = { "--app-path" }, description = "Path to the $application.app", required = true)
	Path applicationPath;

	@CommandLine.Option(names = { "--clear-xattr" }, description = "Clear the extended attributes")
	boolean clearXAttr;

	@CommandLine.Option(names = { "--without-sign" }, description = "Without signing the application")
	boolean withoutSign;

	@CommandLine.Option(names = {
			"--developer-certificate-key" }, description = "Key of the 'Developer ID Application' certificate")
	String appCertificateKey;

	@CommandLine.Option(names = { "--entitlements" }, description = "Entitlements for the application")
	Path entitlements;

	@CommandLine.Option(names = { "--with-standard-entitlements" }, description = "Assign standard entitlements")
	boolean standardEntitlements;

	@CommandLine.Option(names = { "--output-dir" }, description = "Assign standard entitlements")
	Path outputDir;

	@CommandLine.Option(names = { "--with-pkg" }, description = "Create a pkg")
	boolean withPkg;

	@CommandLine.Option(names = { "--pkg-identifier" }, description = "Package identifier")
	String pkgIdentifier;

	@CommandLine.Option(names = {
			"--installer-certificate-key" }, description = "Key of the 'Developer ID Installer' certificate")
	String installerCertificateKey;

	@CommandLine.Option(names = { "--with-dmg" }, description = "Create a dmg")
	boolean withDmg;

	@CommandLine.Option(names = { "--with-notarization" }, description = "Notarize the dmg/pkg")
	boolean withNotarization;

	@CommandLine.Option(names = { "--notarization-primary-bundle-id" }, description = "Primary bundle id")
	String notarizationPrimaryBundleId;

	@CommandLine.Option(names = {
			"--notarization-username" }, description = "Username used to notarize the application")
	String notarizationUser;

	@CommandLine.Option(names = {
			"--notarization-password" }, description = "Password used to notarize the application")
	String notarizationPwd;

	@CommandLine.Option(names = { "--with-staple" }, description = "Staple the dmg/pkg")
	boolean withStaple;

	@Inject
	CommandLine.IFactory factory;

	public MacCodesignerMain() {

	}

	@Override
	public void run() {
		validateAppPath(applicationPath);

		Path applicationPath = duplicate(this.applicationPath);

		LOG.infof("Working copy at %s", applicationPath);

		if (clearXAttr) {
			clearXAttr(applicationPath);
		}
		if (!withoutSign) {
			Objects.requireNonNull(appCertificateKey);
			sign(applicationPath, appCertificateKey, entitlements, standardEntitlements);
		}

		List<Path> packages = new ArrayList<Path>();

		if (withDmg) {
			Path file = createDmg(applicationPath, outputDir == null ? this.applicationPath.getParent() : outputDir);
			packages.add(file);
		}

		if (withPkg) {
			Objects.requireNonNull(pkgIdentifier);
			Objects.requireNonNull(installerCertificateKey);
			Path file = createPkg(applicationPath, outputDir == null ? this.applicationPath.getParent() : outputDir,
					pkgIdentifier, installerCertificateKey);
			packages.add(file);
		}

		if (withNotarization) {
			if (!packages.isEmpty()) {
				Objects.requireNonNull(notarizationPrimaryBundleId);
				Objects.requireNonNull(notarizationUser);
				Objects.requireNonNull(notarizationPwd);
				List<NotarizationInfo> result = notarize(notarizationPrimaryBundleId, notarizationUser, notarizationPwd,
						packages);
				for (int i = 0; i < result.size(); i++) {
					NotarizationInfo r = result.get(i);
					if (r.statusCode.intValue() != 0) {
						LOG.errorf("Failed to notarize '%s' consult '%'", packages.get(i).getFileName(), r.logFileURL);
					} else {
						LOG.infof("Successfully notarized '%s'", packages.get(i).getFileName());
						if (withStaple) {
							staple(packages.get(i));
						}
					}
				}
			} else {
				throw new IllegalStateException("No packages to notarize");
			}
		}
	}

	private static Path duplicate(Path applicationPath) {
		try {
			Path directory = Files.createTempDirectory("mac-codesigner");

			executeCommand(List.of("cp", "-a", applicationPath.toAbsolutePath().toString(),
					directory.toAbsolutePath().toString()));

			return directory.resolve(applicationPath.getFileName());
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private static void clearXAttr(Path applicationPath) {
		LOG.infof("Clearing extended attributes of %s", applicationPath);
		executeCommand(List.of("xattr", "-cr", applicationPath.toAbsolutePath().toString()));
	}

	private static void sign(Path applicationPath, String certificate, Path entitlements,
			boolean standardEntitlements) {
		LOG.infof("Sign application in %s", applicationPath);

		List<String> command = new ArrayList<String>();
		command.addAll(List.of("codesign", "--strict", "--verbose=4", "--deep", "--force", "--options", "runtime",
				"--sign", "" + certificate + ""));
		if (entitlements != null) {
			command.add("--entitlements");
			command.add(entitlements.toAbsolutePath().toString());
		} else if (standardEntitlements) {
			command.add("--entitlements");
			try {
				Path entitlementsFile = Files.createTempFile("entitlements", ".plist");
				Files.writeString(entitlementsFile, standardEntitlements(), StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}

		command.add("--timestamp");
		command.add(applicationPath.toAbsolutePath().toString());

		executeCommand(command);
	}

	private static Path createDmg(Path applicationPath, Path targetDirectory) {
		String name = applicationPath.getFileName().toString().replace(".app", "");
		Path path = targetDirectory.resolve(name + ".dmg");

		LOG.infof("Creating dmg '%s'", path);

		executeCommand(List.of("hdiutil", "create", "-volname", name, "-srcfolder",
				applicationPath.toAbsolutePath().toString(), "-ov", "-format", "UDZO",
				path.toAbsolutePath().toString()));

		return path;
	}

	private static Path createPkg(Path applicationPath, Path targetDirectory, String pkgIdentifier,
			String installerCertificateKey) {
		String name = applicationPath.getFileName().toString().replace(".app", "");
		Path path = targetDirectory.resolve(name + ".pkg");
		
		LOG.infof("Creating pkg '%s'", path);

		try {
			Path dir = Files.createTempDirectory("mac-codesigner-pkg");
			Path appDir = dir.resolve(name);
			Path targetDir = appDir.resolve("Applications");

			executeCommand(List.of("ditto", applicationPath.toAbsolutePath().toString(),
					targetDir.toAbsolutePath().toString()));
			executeCommand(List.of("productbuild", "--identifier", pkgIdentifier, "--sign", installerCertificateKey,
					"--timestamp", "--root", appDir.toAbsolutePath().toString(), "/",
					path.toAbsolutePath().toString()));
			return path;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public static List<NotarizationInfo> notarize(String primaryBundleId, String username, String password,
			List<Path> packages) {
		LOG.infof("Notarize %s", packages);
		Jsonb jsonb = JsonbBuilder.create();

		List<NotarizationInfo> rv = new ArrayList<>();
		List<NotarizationResult> uploadResult = new ArrayList<NotarizationResult>();
		for (Path pack : packages) {
			ProcessResult procResult = executeCommand(List.of("xcrun", "altool", //
					"--output-format", "json", //
					"--notarize-app", //
					"--primary-bundle-id", primaryBundleId, //
					"--username", username, //
					"--password", password, //
					"--file", pack.toAbsolutePath().toString()));

			NotarizationResult result = jsonb.fromJson(procResult.stdout.toString(), NotarizationResult.class);
			uploadResult.add(result);
			LOG.infof("Uploaded '%s' as '%s'", pack, result.notarizationUpload.requestUUID);
		}

		while (!uploadResult.isEmpty()) {
			String requestId = uploadResult.get(0).notarizationUpload.requestUUID;

			ProcessResult checkProcResult = executeCommand(List.of("xcrun", "altool", //
					"--output-format", "json", //
					"--notarization-info", requestId, //
					"--username", username, //
					"--password", password));
			NotarizationResult result = jsonb.fromJson(checkProcResult.stdout.toString(), NotarizationResult.class);
			if (result.notarizationInfo.statusCode != null) {
				uploadResult.remove(0);
				rv.add(result.notarizationInfo);
				LOG.infof("Notarization of '%s' finished", requestId);
			} else {
				try {
					Thread.sleep(30 * 3600); // Wait 30 Seconds
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return rv;
	}

	public static void staple(Path pack) {
		LOG.infof("Staple '%s'", pack);
		executeCommand(List.of("xcrun", "stapler", "staple", pack.toAbsolutePath().toString()));
	}

	private static ProcessResult executeCommand(List<String> command) {
		try {
			Process process = new ProcessBuilder(command).start();

			var stdOut = CompletableFuture.supplyAsync(() -> handleStream(process.getInputStream()));
			var stdErr = CompletableFuture.supplyAsync(() -> handleStream(process.getErrorStream()));

			if (process.waitFor() != 0) {
				LOG.warn("process finished with an exit value != 0");
			}

			ProcessResult rv = new ProcessResult();
			rv.stdout = stdOut.join();
			rv.stderr = stdErr.join();
			return rv;
		} catch (IOException e) {
			throw new IllegalStateException(e);
		} catch (InterruptedException e) {
			throw new IllegalStateException(e);
		}
	}

	private static CharSequence handleStream(InputStream in) {
		BufferedReader r = new BufferedReader(new InputStreamReader(in));
		StringBuilder result = new StringBuilder();
		String line;
		try {
			while ((line = r.readLine()) != null) {
				result.append(line);
				result.append("\n");
			}
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		return result;
	}

	private static void validateAppPath(Path applicationPath) {
		if (!applicationPath.getFileName().toString().endsWith(".app")) {
			throw new IllegalStateException(
					String.format("Path '%s' does not point to a valid Mac Application.", applicationPath));
		}
		if (!Files.exists(applicationPath)) {
			throw new IllegalStateException(String.format("Path '%s' does not exist", applicationPath));
		}
		if (!Files.isDirectory(applicationPath)) {
			throw new IllegalStateException(String.format("Path '%s' is not a directory", applicationPath));
		}
	}

	@Override
	public int run(String... args) throws Exception {
		return new CommandLine(this, factory).execute(args);
	}

	public static void main(String[] args) {
		Quarkus.run(MacCodesignerMain.class, args);
	}

	private static final String standardEntitlements() {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n"
				+ "<plist version=\"1.0\">\n" + "<dict>\n" + "    <key>com.apple.security.cs.allow-jit</key>\n"
				+ "    <true/>\n" + "    <key>com.apple.security.cs.allow-unsigned-executable-memory</key>\n"
				+ "    <true/>\n" + "    <key>com.apple.security.cs.disable-library-validation</key>\n"
				+ "    <true/>\n" + "    <key>com.apple.security.cs.allow-dyld-environment-variables</key>\n"
				+ "    <true/>\n" + "    <key>com.apple.security.cs.debugger</key>\n" + "    <true/>\n" + "</dict>\n"
				+ "</plist>";
	}

	static class ProcessResult {
		CharSequence stdout;
		CharSequence stderr;
	}

}
