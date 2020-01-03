package net.officefloor.server.http.request.config;

import java.util.LinkedList;
import java.util.List;

/**
 * Configuration of the HTTP request.
 * 
 * @author Daniel Sagenschneider
 */
public class RequestConfig {

	/**
	 * HTTP method.
	 */
	public String method = "GET";

	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * URI path of the request line.
	 */
	public String path = "";

	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * HTTP version.
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
