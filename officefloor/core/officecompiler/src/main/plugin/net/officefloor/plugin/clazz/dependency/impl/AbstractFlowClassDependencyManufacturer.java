package net.officefloor.plugin.clazz.dependency.impl;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerServiceFactory;
import net.officefloor.plugin.clazz.flow.ClassFlowBuilder;
import net.officefloor.plugin.clazz.flow.ClassFlowInterfaceFactory;

/**
 * {@link ClassDependencyManufacturer} for providing {@link Flow} invocations.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractFlowClassDependencyManufacturer
		implements ClassDependencyManufacturer, ClassDependencyManufacturerServiceFactory {

	/**
	 * Obtains the {@link Annotation} type.
	 * 
	 * @return {@link Annotation} type.
	 */
	protected abstract Class<? extends Annotation> getAnnotationType();

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
		Class<? extends Annotation> annotationType = this.getAnnotationType();
		if (!dependencyType.isAnnotationPresent(annotationType)) {
			return null; // not flow interface
		}

		// Obtain details to create flow
		SourceContext sourceContext = context.getSourceContext();

		// Create the flow
		ClassFlowBuilder<? extends Annotation> flowBuilder = new ClassFlowBuilder<>(annotationType);
		ClassFlowInterfaceFactory factory = flowBuilder.buildFlowParameterFactory(dependencyType,
				(name, argumentType) -> context.newFlow(name).setArgumentType(argumentType)
						.addAnnotations(Arrays.asList(context.getDependencyAnnotations())).build().getIndex(),
				sourceContext);

		// Create and return the factory
		return new FlowClassDependencyFactory(factory);
	}

}