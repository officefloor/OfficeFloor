package net.officefloor.frame.api.team.source.impl;

import net.officefloor.frame.api.team.source.TeamSourceProperty;

/**
 * {@link TeamSourceProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class TeamSourcePropertyImpl implements TeamSourceProperty {

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
	public TeamSourcePropertyImpl(String name, String label) {
		this.name = name;
		this.label = label;
	}

	/*
	 * =============== TeamSourceProperty =============================
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