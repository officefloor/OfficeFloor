package net.officefloor.web.resource.source;

import net.officefloor.server.http.HttpRequest;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.route.WebServicer;

/**
 * Path for a {@link HttpResource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpPath {

	/**
	 * Path.
	 */
	private final String path;

	/**
	 * {@link WebServicer}.
	 */
	private final WebServicer webServicer;

	/**
	 * Instantiate.
	 * 
	 * @param path Path.
	 */
	public HttpPath(String path) {
		this.path = path;
		this.webServicer = null;
	}

	/**
	 * Instantiate.
	 * 
	 * @param request {@link HttpRequest} to extract the path.
	 */
	public HttpPath(HttpRequest request, WebServicer webServicer) {
		this.path = request.getUri();
		this.webServicer = webServicer;
	}

	/**
	 * Obtains the path.
	 * 
	 * @return Path.
	 */
	public String getPath() {
		return this.path;
	}

	/**
	 * Obtains the {@link WebServicer}.
	 * 
	 * @return {@link WebServicer}.
	 */
	public WebServicer getWebServicer() {
		return this.webServicer;
	}

}