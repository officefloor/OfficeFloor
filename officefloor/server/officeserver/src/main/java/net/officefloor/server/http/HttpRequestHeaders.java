package net.officefloor.server.http;

/**
 * {@link HttpHeader} instances for the {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequestHeaders extends Iterable<HttpHeader> {

	/**
	 * Obtains the first {@link HttpHeader} by the name.
	 * 
	 * @param name
	 *            Name of the {@link HttpHeader}.
	 * @return First {@link HttpHeader} or <code>null</code> if no
	 *         {@link HttpHeader} by the name.
	 */
	HttpHeader getHeader(CharSequence name);

	/**
	 * Obtains all the {@link HttpHeader} instances by the name.
	 * 
	 * @param name
	 *            Name of the {@link HttpHeader} instances.
	 * @return All {@link HttpHeader} instances by the name.
	 */
	Iterable<HttpHeader> getHeaders(CharSequence name);

	/**
	 * Obtains the {@link HttpHeader} at the index.
	 * 
	 * @param index
	 *            Index of the {@link HttpHeader}.
	 * @return {@link HttpHeader} at the index.
	 */
	HttpHeader headerAt(int index);

	/**
	 * Obtains the number of {@link HttpHeader} instances.
	 * 
	 * @return Number of {@link HttpHeader} instances.
	 */
	int length();

}