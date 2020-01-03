package net.officefloor.server.http;

/**
 * {@link HttpRequestCookie} instances for the {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequestCookies extends Iterable<HttpRequestCookie> {

	/**
	 * Obtains the first {@link HttpRequestCookie} by the name.
	 * 
	 * @param name
	 *            Name of the {@link HttpRequestCookie}.
	 * @return First {@link HttpRequestCookie} or <code>null</code> if no
	 *         {@link HttpRequestCookie} by the name.
	 */
	HttpRequestCookie getCookie(String name);

	/**
	 * Obtains the {@link HttpRequestCookie} at the index.
	 * 
	 * @param index
	 *            Index of the {@link HttpRequestCookie}.
	 * @return {@link HttpRequestCookie} at the index.
	 */
	HttpRequestCookie cookieAt(int index);

	/**
	 * Obtains the number of {@link HttpRequestCookie} instances.
	 * 
	 * @return Number of {@link HttpRequestCookie} instances.
	 */
	int length();

}