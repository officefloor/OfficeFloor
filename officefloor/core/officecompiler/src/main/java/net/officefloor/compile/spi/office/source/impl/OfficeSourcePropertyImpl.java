package net.officefloor.compile.spi.office.source.impl;

import net.officefloor.compile.spi.office.source.OfficeSourceProperty;

/**
 * {@link OfficeSourceProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeSourcePropertyImpl implements OfficeSourceProperty {

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Label.
	 */
	private final String label;

	/**
	 * Initiate.
	 * 
	 * @param name
	 *            Name.
	 * @param label
	 *            Label. Defaults to <code>name</code> if <code>null</code>.
	 */
	public OfficeSourcePropertyImpl(String name, String label) {
		this.name = name;
		this.label = ((label == null) || (label.trim().length() == 0)) ? name
				: label;
	}

	/*
	 * ====================== OfficeSourceProperty ==========================
	 */

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public String getName() {
		return this.name;
	}

}