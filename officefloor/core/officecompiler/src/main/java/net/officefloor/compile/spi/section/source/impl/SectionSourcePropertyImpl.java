package net.officefloor.compile.spi.section.source.impl;

import net.officefloor.compile.spi.section.source.SectionSourceProperty;

/**
 * {@link SectionSourceProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionSourcePropertyImpl implements SectionSourceProperty {

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
	public SectionSourcePropertyImpl(String name, String label) {
		this.name = name;
		this.label = ((label == null) || (label.trim().length() == 0)) ? name
				: label;
	}

	/*
	 * ==================== SectionSourceProperty ===========================
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