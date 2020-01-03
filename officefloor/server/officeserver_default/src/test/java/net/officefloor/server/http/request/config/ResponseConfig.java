package net.officefloor.server.http.request.config;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration of the expected HTTP response.
 * 
 * @author Daniel Sagenschneider
 */
public class ResponseConfig {

	/**
	 * Status.
	 */
	public int status = 200;

	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * Message.
	 */
	public String message = "OK";

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Version.
	 */
	public String version = "HTTP/1.1";

	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Headers.
	 */
	public final List<HeaderConfig> headers = new LinkedList<HeaderConfig>();

	public void addHeader(HeaderConfig header) {
		this.headers.add(header);
	}

	/**
	 * Body.
	 */
	public String body;

	public void setBody(String body) {
		this.body = body;
	}
}
