package net.officefloor.frame.impl.execute.office;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FlowMetaData;
import net.officefloor.frame.internal.structure.OfficeStartupFunction;

/**
 * {@link OfficeStartupFunction} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeStartupFunctionImpl implements OfficeStartupFunction {

	/**
	 * {@link FlowMetaData} for the {@link OfficeStartupFunction}.
	 */
	private final FlowMetaData flowMetaData;

	/**
	 * Parameter for the startup {@link Flow}.
	 */
	private final Object parameter;

	/**
	 * Initiate.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData} for the {@link OfficeStartupFunction}.
	 * @param parameter
	 *            Parameter for the startup {@link Flow}.
	 */
	public OfficeStartupFunctionImpl(FlowMetaData flowMetaData, Object parameter) {
		this.flowMetaData = flowMetaData;
		this.parameter = parameter;
	}

	/*
	 * ================== OfficeStartupFunction ==================
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