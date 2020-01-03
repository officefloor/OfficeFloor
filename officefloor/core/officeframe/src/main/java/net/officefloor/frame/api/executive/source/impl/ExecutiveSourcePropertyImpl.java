package net.officefloor.frame.api.executive.source.impl;

import net.officefloor.frame.api.executive.source.ExecutiveSourceProperty;

/**
 * {@link ExecutiveSourceProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ExecutiveSourcePropertyImpl implements ExecutiveSourceProperty {

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
	 * @param name  Name of property.
	 * @param label Label of property.
	 */
	public ExecutiveSourcePropertyImpl(String name, String label) {
		this.name = name;
		this.label = label;
	}

	/*
	 * =============== ExecutiveSourceProperty =====================
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