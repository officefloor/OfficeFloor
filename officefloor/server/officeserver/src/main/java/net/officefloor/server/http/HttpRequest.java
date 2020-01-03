package net.officefloor.server.http;

import net.officefloor.server.stream.ServerInputStream;

/**
 * HTTP request from the {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequest {

	/**
	 * Obtains the {@link HttpMethod}.
	 * 
	 * @return {@link HttpMethod}.
	 */
	HttpMethod getMethod();

	/**
	 * Obtains the request URI as provided on the request.
	 * 
	 * @return Request URI as provided on the request.
	 */
	String getUri();

	/**
	 * Obtains the {@link HttpVersion}.
	 * 
	 * @return {@link HttpVersion}.
	 */
	HttpVersion getVersion();

	/**
	 * Obtains the {@link HttpRequestHeaders}.
	 * 
	 * @return {@link HttpRequestHeaders}.
	 */
	HttpRequestHeaders getHeaders();

	/**
	 * Obtains the {@link HttpRequestCookies}.
	 * 
	 * @return {@link HttpRequestCookies}.
	 */
	HttpRequestCookies getCookies();

	/**
	 * Obtains the {@link ServerInputStream} to the entity of the HTTP request.
	 * 
	 * @return {@link ServerInputStream} to the entity of the HTTP request.
	 */
	ServerInputStream getEntity();

}