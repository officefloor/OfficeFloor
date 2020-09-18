package net.officefloor.frame.impl.execute.managedobject;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectStartupFunction;

/**
 * {@link ManagedObjectStartupFunction} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectStartupFunctionImpl implements ManagedObjectStartupFunction {

	/**
	 * {@link FlowMetaData} for the startup {@link ManagedFunction}.
	 */
	private final FlowMetaData flowMetaData;

	/**
	 * Parameter to the {@link ManagedFunction}.
	 */
	private final Object parameter;

	/**
	 * Instantiate.
	 * 
	 * @param flowMetaData {@link FlowMetaData} for the startup
	 *                     {@link ManagedFunction}.
	 * @param parameter    Parameter to the {@link ManagedFunction}.
	 */
	public ManagedObjectStartupFunctionImpl(FlowMetaData flowMetaData, Object parameter) {
		this.flowMetaData = flowMetaData;
		this.parameter = parameter;
	}

	/*
	 * ====================== ManagedObjectStartupFunction =========================
	 */

	@Override
	public FlowMetaData getFlowMetaData() {
		return this.flowMetaData;
	}

	@Override
	public Object getParameter() {
		return this.parameter;
	}

}