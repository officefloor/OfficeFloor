package net.officefloor.app.subscription;

import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.ServerHttpConnection;

public class Cors {

	private static HttpHeaderName ALLOW_ORIGIN = new HttpHeaderName("Access-Control-Allow-Origin");

	private static HttpHeaderName ALLOW_METHODS = new HttpHeaderName("Access-Control-Allow-Methods");

	private static HttpHeaderName ALLOW_HEADERS = new HttpHeaderName("Access-Control-Allow-Headers");

	public static HttpHeaderValue ALL = new HttpHeaderValue("*");

	public void options(ServerHttpConnection connection) {
		HttpResponse response = connection.getResponse();
		HttpResponseHeaders headers = response.getHeaders();
		headers.addHeader(ALLOW_ORIGIN, ALL);
		headers.addHeader(ALLOW_METHODS, ALL);
		headers.addHeader(ALLOW_HEADERS, ALL);
	}

}