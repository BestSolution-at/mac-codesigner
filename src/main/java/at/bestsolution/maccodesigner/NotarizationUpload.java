package at.bestsolution.maccodesigner;

import javax.json.bind.annotation.JsonbProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class NotarizationUpload {
	@JsonbProperty("RequestUUID")
	public String requestUUID;
	
	public NotarizationUpload() {
	}
}