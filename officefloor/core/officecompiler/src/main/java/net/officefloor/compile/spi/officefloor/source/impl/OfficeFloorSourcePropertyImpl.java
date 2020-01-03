package net.officefloor.compile.spi.officefloor.source.impl;

import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceProperty;

/**
 * {@link OfficeFloorSourceProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorSourcePropertyImpl implements OfficeFloorSourceProperty {

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
	public OfficeFloorSourcePropertyImpl(String name, String label) {
		this.name = name;
		this.label = ((label == null) || (label.trim().length() == 0)) ? name
				: label;
	}

	/*
	 * ================= OfficeFloorSourceProperty ===========================
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