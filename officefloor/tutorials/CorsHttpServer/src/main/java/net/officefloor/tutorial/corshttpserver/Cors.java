package net.officefloor.tutorial.corshttpserver;

import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * CORS handling.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class Cors {

	private static HttpHeaderName ALLOW_ORIGIN = new HttpHeaderName("Access-Control-Allow-Origin");

	private static HttpHeaderName ALLOW_METHODS = new HttpHeaderName("Access-Control-Allow-Methods");

	private static HttpHeaderName ALLOW_HEADERS = new HttpHeaderName("Access-Control-Allow-Headers");

	public static HttpHeaderValue ALL = new HttpHeaderValue("*");

	public static void cors(ServerHttpConnection connection) {
		HttpResponseHeaders headers = connection.getResponse().getHeaders();
		headers.addHeader(ALLOW_ORIGIN, ALL);
		headers.addHeader(ALLOW_METHODS, ALL);
		headers.addHeader(ALLOW_HEADERS, ALL);
	}

}
// END SNIPPET: tutorial