package net.officefloor.plugin.clazz.dependency.impl;

import java.lang.annotation.Annotation;
import java.util.Arrays;

import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext.ClassFlow;
import net.officefloor.plugin.clazz.flow.ClassFlowContext;
import net.officefloor.plugin.section.clazz.FlowAnnotation;

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

		// Create the flow
		String flowName = flowContext.getMethod().getName();
		ClassFlow flow = dependencyContext.newFlow(flowName).setArgumentType(flowContext.getParameterType())
				.addAnnotations(Arrays.asList(dependencyContext.getDependencyAnnotations()));

		// Obtain the index of the flow
		int flowIndex = flow.build().getIndex();

		// Obtain details of flows
		boolean isSpawn = flowContext.isSpawn();
		Class<?> parameterType = flowContext.getParameterType();
		boolean isFlowCallback = flowContext.isFlowCallback();

		// Register the flow interface
		dependencyContext
				.addAnnotation(new FlowAnnotation(flowName, flowIndex, isSpawn, parameterType, isFlowCallback));

		// Return the flow index
		return flowIndex;
	}

}