package net.officefloor.server.http.impl;

import net.officefloor.server.http.HttpHeader;

/**
 * Non materialized {@link HttpHeader} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface NonMaterialisedHttpHeaders extends Iterable<NonMaterialisedHttpHeader> {

	/**
	 * Obtains the number of {@link NonMaterialisedHttpHeader} instances.
	 * 
	 * @return Number of {@link NonMaterialisedHttpHeader} instances.
	 */
	int length();

}