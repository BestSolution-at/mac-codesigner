package at.bestsolution.maccodesigner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;

import org.junit.jupiter.api.Test;

public class TestDeserialization {
	@Test
	public void notarizeApp_Success() {
		Jsonb jsonb = JsonbBuilder.create();
		NotarizationResult result = jsonb.fromJson(
				TestDeserialization.class.getClassLoader().getResourceAsStream("notarizeApp_Success.json"),
				NotarizationResult.class);

		assertEquals("4.029.1194", result.toolVersion, "Invalid toolVersion");
		assertEquals(
				"/Applications/Xcode.app/Contents/SharedFrameworks/ContentDeliveryServices.framework/Versions/A/Frameworks/AppStoreService.framework",
				result.toolPath, "Invalid toolPath");
		assertNotNull(result.notarizationUpload, "Invalid notarizationUpload");
		assertEquals("11111-11111-11111-11111-1111111", result.notarizationUpload.requestUUID,
				"Invalid notarizationUpload.requestUUID");
		assertNotNull("No errors uploading './SampleSuccess.dmg'.", result.successMessage);
		assertNotNull("11.2.3", result.osVersion);
	}

	@Test
	public void notarizeInfo_Success() {
		Jsonb jsonb = JsonbBuilder.create();
		NotarizationResult result = jsonb.fromJson(
				TestDeserialization.class.getClassLoader().getResourceAsStream("notarizeInfo_Success.json"),
				NotarizationResult.class);

		assertEquals("4.029.1194", result.toolVersion, "Invalid toolVersion");
		assertEquals(
				"/Applications/Xcode.app/Contents/SharedFrameworks/ContentDeliveryServices.framework/Versions/A/Frameworks/AppStoreService.framework",
				result.toolPath, "Invalid toolPath");
		assertNotNull("No errors getting notarization info.", result.successMessage);

		assertNotNull(result.notarizationInfo);
		assertEquals("success", result.notarizationInfo.status, "Invalid notarizationInfo.status");
		assertEquals("Package Approved", result.notarizationInfo.statusMessage,
				"Invalid notarizationInfo.statusMessage");
		assertEquals("https://osxapps-ssl.itunes.apple.com/itunes-assets/......", result.notarizationInfo.logFileURL,
				"Invalid notarizationInfo.logFileURL");
		assertEquals("2021-04-22T11:20:56.000Z", result.notarizationInfo.date, "Invalid notarizationInfo.date");
		assertEquals("11111-11111-11111-11111-1111111", result.notarizationInfo.requestUUID);
		assertNotNull(result.notarizationInfo.statusCode, "Invalid notarizationInfo.statusCode");
		assertEquals(0, result.notarizationInfo.statusCode, "Invalid notarizationInfo.statusCode");
		assertEquals("1111111111111111111111111111111", result.notarizationInfo.hash, "Invalid notarizationInfo.hash");

		assertNotNull("11.2.3", result.osVersion);
	}

	@Test
	public void notarizeInfo_InProgress() {
		Jsonb jsonb = JsonbBuilder.create();
		NotarizationResult result = jsonb.fromJson(
				TestDeserialization.class.getClassLoader().getResourceAsStream("notarizeInfo_InProgress.json"),
				NotarizationResult.class);

		assertEquals("4.029.1194", result.toolVersion, "Invalid toolVersion");
		assertEquals(
				"/Applications/Xcode.app/Contents/SharedFrameworks/ContentDeliveryServices.framework/Versions/A/Frameworks/AppStoreService.framework",
				result.toolPath, "Invalid toolPath");
		assertNotNull("No errors getting notarization info.", result.successMessage);
		assertNotNull(result.notarizationInfo);
		assertEquals("1111111111111111111111111111111", result.notarizationInfo.hash, "Invalid notarizationInfo.hash");
		assertEquals("in progress", result.notarizationInfo.status, "Invalid notarizationInfo.status");
		assertEquals("11111-11111-11111-11111-1111111", result.notarizationInfo.requestUUID);
		assertEquals("2021-04-22T11:20:56.000Z", result.notarizationInfo.date, "Invalid notarizationInfo.date");
		assertNull(result.notarizationInfo.statusCode, "Invalid notarizationInfo.statusCode");
		assertNotNull("11.2.3", result.osVersion);
	}
}
