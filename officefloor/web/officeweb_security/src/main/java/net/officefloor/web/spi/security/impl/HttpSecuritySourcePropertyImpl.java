package net.officefloor.web.spi.security.impl;

import net.officefloor.web.spi.security.HttpSecuritySourceProperty;

/**
 * {@link HttpSecuritySourceProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySourcePropertyImpl implements
		HttpSecuritySourceProperty {

	/**
	 * Name of property.
	 */
	protected final String name;

	/**
	 * Label of property.
	 */
	protected final String label;

	/**
	 * Initiate with name and label of property.
	 * 
	 * @param name
	 *            Name of property.
	 * @param label
	 *            Label of property.
	 */
	public HttpSecuritySourcePropertyImpl(String name, String label) {
		this.name = name;
		this.label = label;
	}

	/*
	 * ================== HttpSecuritySourceProperty ==============
	 */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getLabel() {
		return this.label;
	}

}