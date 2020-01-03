package net.officefloor.server.http.mock;

import net.officefloor.server.http.HttpResponse;

/**
 * Builder for a mock {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MockHttpResponseBuilder extends HttpResponse {

	/**
	 * Builds the {@link MockHttpResponse}.
	 * 
	 * @return {@link MockHttpResponse}.
	 */
	MockHttpResponse build();

}