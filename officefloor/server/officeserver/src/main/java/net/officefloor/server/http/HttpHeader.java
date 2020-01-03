package net.officefloor.server.http;

/**
 * HTTP header.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpHeader {

	/**
	 * Obtains the name of the {@link HttpHeader}.
	 *
	 * @return Name of the {@link HttpHeader}.
	 */
	String getName();

	/**
	 * Obtains the value for the {@link HttpHeader}.
	 *
	 * @return Value for the {@link HttpHeader}.
	 */
	String getValue();

}