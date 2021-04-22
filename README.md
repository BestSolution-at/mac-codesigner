# mac-codesigner project

Commandline utility to not going insane when codesigning and notarizing application for Apple Devices. 

This utility allows to:
* Codesign your applications
* Package as DMG
* Package as PKG
* Notarize the DMG and PKG
* Staple DMG and PKG artifact

## Synopsis

To print all available commandline options 
```shell script
./mac-codesigner-1.0.0-SNAPSHOT-runner -h
```
Example:
```shell script
./mac-codesigner-1.0.0-SNAPSHOT-runner  --app-path=/path/to/my/Sample.app \
 --developer-certificate-key=1111111111111111 \
 --with-dmg \
 --with-pkg \
 --installer-certificate-key=1111111111 \
 --pkg-identifier=my.application.identifier.pkg \
 --entitlements=/path/to/entitlements.plist \
 --with-notarization \
 --notarization-primary-bundle-id=my.application.id \
 --notarization-username=john@doe.com \
 --notarization-password="johndoe-secret"
 --with-staple
```

## Prequisits

* Make sure you have xcode command line utilities installed
```shell script
sudo xcode-select --install
```
* Create an "App-Specific password for altool" [Apple Instructions](https://appleid.apple.com) - **Use Safari - Firefox nor Chrome worked for me**
  * Name it `Developer-altool`
  * Save the password created we you need to pass it to as the `--notarization-password`
* Create 2 certificates at [Apple Developer](https://developer.apple.com)
  * `Developer ID Application`
  * `Developer ID Installer`
* Add them to your "KeyChain Access" Application

## Acknowledgement

There are other utilities which helped me implement this or provided useful information. One could argue
that I suffer from the NIH-Syndrom but I found it interesting to get something done with Quarkus and GraalVM to create
a native binary so for me it was more of a research project teaching about this tech-stack.

Similar utilities:
* [codesign](https://github.com/txoof/codesign#codesign)
* [gon](https://github.com/mitchellh/gon)

Sites / Articles I found useful while implementing this:
* [Stackoverflow Question](https://blog.adoptopenjdk.net/2020/05/a-simple-guide-to-notarizing-your-java-application/)
* [BlogPost on notarizing Java](https://stackoverflow.com/questions/64652704/how-to-notarize-an-macos-command-line-tool-created-outside-of-xcode)

## Building

The application can be packaged using:
```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Pnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Pnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/mac-codesigner-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.html.
