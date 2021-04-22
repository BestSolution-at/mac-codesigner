package at.bestsolution.maccodesigner;

import javax.json.bind.annotation.JsonbProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class NotarizationResult {
	@JsonbProperty("tool-version")
	public String toolVersion;

	@JsonbProperty("tool-path")
	public String toolPath;

	@JsonbProperty("notarization-upload")
	public NotarizationUpload notarizationUpload;

	@JsonbProperty("notarization-info")
	public NotarizationInfo notarizationInfo;

	@JsonbProperty("success-message")
	public String successMessage;

	@JsonbProperty("os-version")
	public String osVersion;
	
	public NotarizationResult() {
	}
}