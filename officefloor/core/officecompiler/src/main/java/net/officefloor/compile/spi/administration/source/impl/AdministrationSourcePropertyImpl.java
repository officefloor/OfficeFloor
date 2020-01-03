package net.officefloor.compile.spi.administration.source.impl;

import net.officefloor.compile.spi.administration.source.AdministrationSourceProperty;

/**
 * {@link AdministrationSourceProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationSourcePropertyImpl implements
		AdministrationSourceProperty {

	/**
	 * Label.
	 */
	private final String label;

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Initialise.
	 * 
	 * @param name
	 *            Name.
	 * @param label
	 *            Label.
	 */
	public AdministrationSourcePropertyImpl(String name, String label) {
		this.label = label;
		this.name = name;
	}

	/*
	 * ==================== AdministratorSourceProperty =======================
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