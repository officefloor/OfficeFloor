package net.officefloor.plugin.clazz.dependency.impl;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;
import net.officefloor.plugin.clazz.flow.ClassFlowContext;

/**
 * {@link ClassDependencyManufacturer} for providing a dependency
 * {@link FlowInterface}.
 * 
 * @author Daniel Sagenschneider
 */
public class FlowInterfaceClassDependencyManufacturer extends AbstractFlowClassDependencyManufacturer {

	@Override
	protected Class<? extends Annotation> getAnnotationType() {
		return FlowInterface.class;
	}

	@Override
	protected int addFlow(ClassDependencyManufacturerContext dependencyContext, ClassFlowContext flowContext) {
		return dependencyContext.newFlow(flowContext.getMethod().getName())
				.setArgumentType(flowContext.getParameterType())
				.addAnnotations(Arrays.asList(dependencyContext.getDependencyAnnotations())).build().getIndex();
	}

}