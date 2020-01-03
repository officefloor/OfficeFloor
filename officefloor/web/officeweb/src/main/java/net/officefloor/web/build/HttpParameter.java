package net.officefloor.web.build;

import net.officefloor.server.http.HttpRequest;

/**
 * HTTP parameter.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpParameter {

	/**
	 * Obtains the name.
	 * 
	 * @return Name.
	 */
	String getName();

	/**
	 * Obtains the {@link HttpValueLocation} on the {@link HttpRequest}.
	 * 
	 * @return {@link HttpValueLocation} on the {@link HttpRequest}.
	 */
	HttpValueLocation getLocation();

	/**
	 * Indicates if required.
	 * 
	 * @return <code>true</code> if required.
	 */
	boolean isRequired();

}