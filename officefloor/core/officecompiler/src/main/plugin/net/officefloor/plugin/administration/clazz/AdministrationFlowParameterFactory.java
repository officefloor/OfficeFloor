package net.officefloor.plugin.administration.clazz;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.ClassFlowMethodMetaData;
import net.officefloor.plugin.clazz.ClassFlowParameterFactory;

/**
 * {@link AdministrationParameterFactory} to obtain the {@link Flow}.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationFlowParameterFactory implements AdministrationParameterFactory {

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
	public AdministrationFlowParameterFactory(ClassFlowParameterFactory flowParameterFactory) {
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
	public Object createParameter(AdministrationContext<?, ?, ?> context) throws Exception {
		return this.flowParameterFactory.createParameter(context);
	}

}