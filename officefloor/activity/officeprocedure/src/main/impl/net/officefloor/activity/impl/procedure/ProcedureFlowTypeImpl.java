package net.officefloor.activity.impl.procedure;

import net.officefloor.activity.procedure.ProcedureFlowType;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link ProcedureFlowType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcedureFlowTypeImpl implements ProcedureFlowType {

	/**
	 * Name of the {@link Flow}.
	 */
	private final String flowName;

	/**
	 * Argument type.
	 */
	private final Class<?> argumentType;

	/**
	 * Instantiate.
	 * 
	 * @param flowName     Name of the {@link Flow}.
	 * @param argumentType Argument type.
	 */
	public ProcedureFlowTypeImpl(String flowName, Class<?> argumentType) {
		this.flowName = flowName;
		this.argumentType = argumentType;
	}

	/*
	 * =================== ProcedureFlowType ===========================
	 */

	@Override
	public String getFlowName() {
		return this.flowName;
	}

	@Override
	public Class<?> getArgumentType() {
		return this.argumentType;
	}

}