package net.officefloor.compile.spi.supplier.source.impl;

import net.officefloor.compile.spi.supplier.source.SupplierSourceProperty;

/**
 * {@link SupplierSourceProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierSourcePropertyImpl implements SupplierSourceProperty {

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
	public SupplierSourcePropertyImpl(String name, String label) {
		this.name = name;
		this.label = ((label == null) || (label.trim().length() == 0)) ? name
				: label;
	}

	/*
	 * ====================== SupplierSourceProperty ==============================
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