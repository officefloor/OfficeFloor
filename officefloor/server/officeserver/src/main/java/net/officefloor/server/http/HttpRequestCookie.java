package net.officefloor.server.http;

/**
 * <p>
 * Cookie on the {@link HttpRequest}.
 * <p>
 * As per <a href="https://tools.ietf.org/html/rfc6265">RFC-6265</a>.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequestCookie {

	/**
	 * Obtains the name.
	 * 
	 * @return Name.
	 */
	String getName();

	/**
	 * Obtains the value.
	 * 
	 * @return Value.
	 */
	String getValue();

}