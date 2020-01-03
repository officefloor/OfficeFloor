package net.officefloor.server.http.impl;

import net.officefloor.server.http.HttpHeader;

/**
 * Non materialised {@link HttpHeader}.
 * 
 * @author Daniel Sagenschneider
 */
public interface NonMaterialisedHttpHeader {

	/**
	 * Obtains the {@link HttpHeader} name.
	 * 
	 * @return {@link HttpHeader} name.
	 */
	CharSequence getName();

	/**
	 * Materialises the {@link HttpHeader}.
	 * 
	 * @return {@link HttpHeader}.
	 */
	HttpHeader materialiseHttpHeader();

}