package net.officefloor.web.value.load;

import net.officefloor.web.build.HttpValueLocation;

/**
 * Name of a value.
 * 
 * @author Daniel Sagenschneider
 */
public class ValueName {

	/**
	 * Name of value.
	 */
	private final String name;

	/**
	 * {@link HttpValueLocation}.
	 */
	private final HttpValueLocation location;

	/**
	 * Instantiate.
	 * 
	 * @param name     Name of value.
	 * @param location {@link HttpValueLocation}.
	 */
	public ValueName(String name, HttpValueLocation location) {
		this.name = name;
		this.location = location;
	}

	/**
	 * Obtains the name of the value.
	 * 
	 * @return Name of the value.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Obtains the {@link HttpValueLocation} for the value.
	 * 
	 * @return {@link HttpValueLocation} for the value. <code>null</code> to
	 *         indicate any.
	 */
	public HttpValueLocation getLocation() {
		return this.location;
	}

}