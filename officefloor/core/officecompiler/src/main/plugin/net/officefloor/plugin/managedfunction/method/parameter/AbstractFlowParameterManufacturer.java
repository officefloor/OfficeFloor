package net.officefloor.plugin.managedfunction.method.parameter;

import java.lang.annotation.Annotation;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.ClassFlowBuilder;
import net.officefloor.plugin.clazz.ClassFlowParameterFactory;
import net.officefloor.plugin.clazz.ClassFlowRegistry;
import net.officefloor.plugin.managedfunction.method.MethodParameterFactory;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturer;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturerContext;
import net.officefloor.plugin.managedfunction.method.MethodParameterManufacturerServiceFactory;

/**
 * Abstract {@link MethodParameterManufacturer} for the annotated {@link Flow}
 * parameters.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractFlowParameterManufacturer<A extends Annotation>
		implements MethodParameterManufacturer, MethodParameterManufacturerServiceFactory {

	/**
	 * Obtains the {@link Class} of the {@link Annotation}.
	 * 
	 * @return {@link Class} of the {@link Annotation}.
	 */
	protected abstract Class<A> getFlowAnnotation();

	/*
	 * ============ MethodParameterManufacturerServiceFactory ===========
	 */

	@Override
	public MethodParameterManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =================== MethodParameterManufacturer ===================
	 */

	@Override
	public MethodParameterFactory createParameterFactory(MethodParameterManufacturerContext context) throws Exception {

		// Create the flow registry
		ClassFlowRegistry flowRegistry = (label, flowParameterType) -> {
			return context.addFlow((builder) -> {
				builder.setLabel(label);
				if (flowParameterType != null) {
					builder.setArgumentType(flowParameterType);
				}
			});
		};

		// Attempt to build flow parameter factory
		ClassFlowParameterFactory flowParameterFactory = new ClassFlowBuilder<A>(this.getFlowAnnotation())
				.buildFlowParameterFactory(context.getFunctionName(), context.getParameterClass(), flowRegistry,
						context.getSourceContext());
		if (flowParameterFactory == null) {
			return null; // not flow interface
		}

		// Return wrapping managed function flow parameter factory
		return new FlowInterfaceParameterFactory(flowParameterFactory);
	}

}
