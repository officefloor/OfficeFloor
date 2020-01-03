package net.officefloor.compile.spi.governance.source.impl;

import net.officefloor.compile.spi.governance.source.GovernanceSourceProperty;

/**
 * {@link GovernanceSourceProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceSourcePropertyImpl implements GovernanceSourceProperty {

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
	public GovernanceSourcePropertyImpl(String name, String label) {
		this.label = label;
		this.name = name;
	}

	/*
	 * ==================== GoveranceSourceProperty =======================
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