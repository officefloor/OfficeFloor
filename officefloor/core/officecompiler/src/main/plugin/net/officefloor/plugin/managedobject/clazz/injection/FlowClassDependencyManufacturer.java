package net.officefloor.plugin.managedobject.clazz.injection;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.ClassFlowBuilder;
import net.officefloor.plugin.clazz.ClassFlowInterfaceFactory;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.managedobject.clazz.ClassDependencyFactory;
import net.officefloor.plugin.managedobject.clazz.ClassDependencyManufacturer;
import net.officefloor.plugin.managedobject.clazz.ClassDependencyManufacturerContext;
import net.officefloor.plugin.managedobject.clazz.ClassDependencyManufacturerServiceFactory;

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

		// Obtain details to create flow
		Class<?> dependencyType = context.getDependencyClass();
		SourceContext sourceContext = context.getSourceContext();

		// Create the flow
		ClassFlowBuilder<FlowInterface> flowBuilder = new ClassFlowBuilder<>(FlowInterface.class);
		ClassFlowInterfaceFactory factory = flowBuilder.buildFlowParameterFactory("TODO REMOVE", dependencyType,
				(name, argumentType) -> context.addFlow().setName(name).setArgumentType(argumentType).getIndex(),
				sourceContext);

		// Create and return the factory
		return new FlowClassDependencyFactory(factory);
	}

}