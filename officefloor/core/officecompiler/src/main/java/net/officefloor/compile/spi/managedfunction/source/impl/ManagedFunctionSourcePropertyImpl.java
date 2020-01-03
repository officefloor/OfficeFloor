package net.officefloor.compile.spi.managedfunction.source.impl;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceProperty;

/**
 * {@link ManagedFunctionSourceProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionSourcePropertyImpl implements ManagedFunctionSourceProperty {

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
	public ManagedFunctionSourcePropertyImpl(String name, String label) {
		this.name = name;
		this.label = ((label == null) || (label.trim().length() == 0)) ? name
				: label;
	}

	/*
	 * ====================== WorkSourceProperty ==============================
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