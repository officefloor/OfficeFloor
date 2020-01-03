package net.officefloor.activity.impl.procedure;

import net.officefloor.activity.procedure.ProcedureProperty;

/**
 * {@link ProcedureProperty} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedurePropertyImpl implements ProcedureProperty {

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
	public ProcedurePropertyImpl(String name, String label) {
		this.name = name;
		this.label = label;
	}

	/*
	 * ================= ProcedureProperty ======================
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