package net.officefloor.plugin.managedfunction.method.parameter;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.ClassFlowMethodMetaData;
import net.officefloor.plugin.clazz.ClassFlowParameterFactory;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;

/**
 * {@link MethodParameterFactory} to obtain the {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowInterfaceParameterFactory implements MethodParameterFactory {

	/**
	 * {@link ClassFlowParameterFactory}.
	 */
	private final ClassFlowParameterFactory flowParameterFactory;

	/**
	 * Initiate.
	 * 
	 * @param flowParameterFactory
	 *            {@link ClassFlowParameterFactory}.
	 */
	public FlowInterfaceParameterFactory(ClassFlowParameterFactory flowParameterFactory) {
		this.flowParameterFactory = flowParameterFactory;
	}

	/**
	 * Obtains the {@link ClassFlowMethodMetaData}.
	 * 
	 * @return {@link ClassFlowMethodMetaData} instances.
	 */
	public ClassFlowMethodMetaData[] getFlowMethodMetaData() {
		return this.flowParameterFactory.getFlowMethodMetaData();
	}

	/*
	 * ==================== ParameterFactory ========================
	 */

	@Override
	public Object createParameter(ManagedFunctionContext<?, ?> context) throws Exception {
		return this.flowParameterFactory.createParameter(context);
	}

}