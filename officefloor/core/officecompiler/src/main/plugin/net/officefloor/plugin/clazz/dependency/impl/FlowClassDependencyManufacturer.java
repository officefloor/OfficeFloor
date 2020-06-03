package net.officefloor.plugin.clazz.dependency.impl;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerServiceFactory;
import net.officefloor.plugin.clazz.flow.ClassFlowBuilder;
import net.officefloor.plugin.clazz.flow.ClassFlowInterfaceFactory;

/**
 * {@link ClassDependencyManufacturer} for providing a dependency
 * {@link FlowInterface}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowClassDependencyManufacturer
		implements ClassDependencyManufacturer, ClassDependencyManufacturerServiceFactory {

	/*
	 * ================= ClassDependencyManufacturerServiceFactory ================
	 */

	@Override
	public ClassDependencyManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================== ClassDependencyManufacturer =======================
	 */

	@Override
	public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context) throws Exception {

		// Determine if flow
		Class<?> dependencyType = context.getDependencyClass();
		if (!dependencyType.isAnnotationPresent(FlowInterface.class)) {
			return null; // not flow interface
		}

		// Obtain details to create flow
		SourceContext sourceContext = context.getSourceContext();

		// Create the flow
		ClassFlowBuilder<FlowInterface> flowBuilder = new ClassFlowBuilder<>(FlowInterface.class);
		ClassFlowInterfaceFactory factory = flowBuilder.buildFlowParameterFactory(dependencyType,
				(name, argumentType) -> context.addFlow().setName(name).setArgumentType(argumentType).getIndex(),
				sourceContext);

		// Create and return the factory
		return new FlowClassDependencyFactory(factory);
	}

}