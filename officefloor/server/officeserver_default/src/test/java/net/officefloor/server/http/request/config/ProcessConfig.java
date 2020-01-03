package net.officefloor.server.http.request.config;

/**
 * Configuration of how to process the request.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessConfig {

	/**
	 * Body to send back in the response message.
	 */
	public String body;

	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * Message of exception to be thrown in processing.
	 */
	public String exception;

	public void setException(String exception) {
		this.exception = exception;
	}
}
