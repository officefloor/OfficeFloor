package net.officefloor.web.state;

import java.io.Serializable;

import net.officefloor.web.build.HttpValueLocation;

/**
 * HTTP argument.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpArgument implements Serializable {

	/**
	 * {@link Serializable} version.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Argument name.
	 */
	public final String name;

	/**
	 * Argument value.
	 */
	public final String value;

	/**
	 * Location that this {@link HttpArgument} was sourced.
	 */
	public final HttpValueLocation location;

	/**
	 * Next {@link HttpArgument}.
	 */
	public HttpArgument next = null;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            Argument name.
	 * @param value
	 *            Argument value.
	 * @param location
	 *            {@link HttpValueLocation}.
	 */
	public HttpArgument(String name, String value, HttpValueLocation location) {
		this.name = name;
		this.value = value;
		this.location = location;
	}

}