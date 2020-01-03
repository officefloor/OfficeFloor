package net.officefloor.compile.spi.pool.source.impl;

import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceProperty;

/**
 * {@link ManagedObjectPoolSourceProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectPoolSourcePropertyImpl implements ManagedObjectPoolSourceProperty {

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
	public ManagedObjectPoolSourcePropertyImpl(String name, String label) {
		this.label = label;
		this.name = name;
	}

	/*
	 * ==================== ManagedObjectPoolSourceProperty ====================
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