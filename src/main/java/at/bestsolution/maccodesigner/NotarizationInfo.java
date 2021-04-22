package at.bestsolution.maccodesigner;

import javax.json.bind.annotation.JsonbProperty;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection
public class NotarizationInfo {
	@JsonbProperty("Status")
	public String status;

	@JsonbProperty("Status Message")
	public String statusMessage;

	@JsonbProperty("LogFileURL")
	public String logFileURL;
	
	@JsonbProperty("Date")
	public String date;

	@JsonbProperty("RequestUUID")
	public String requestUUID;

	@JsonbProperty("Status Code")
	public Integer statusCode;

	@JsonbProperty("Hash")
	public String hash;
	
	public NotarizationInfo() {
		
	}
}
