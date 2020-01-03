package net.officefloor.server.http.mock;

/**
 * Callback with the {@link MockHttpResponse} for the
 * {@link MockHttpRequestBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MockHttpRequestCallback {

	/**
	 * Callback with the {@link MockHttpResponse}.
	 * 
	 * @param response
	 *            {@link MockHttpResponse}.
	 */
	void response(MockHttpResponse response);

}