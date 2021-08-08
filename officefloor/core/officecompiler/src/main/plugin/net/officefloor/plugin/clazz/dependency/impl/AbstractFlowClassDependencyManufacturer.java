/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext.ClassFlow;
import net.officefloor.plugin.clazz.flow.ClassFlowBuilder;
import net.officefloor.plugin.clazz.flow.ClassFlowContext;
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

	/**
	 * Adds a {@link Flow}.
	 * 
	 * @param dependencyContext {@link ClassDependencyManufacturerContext}.
	 * @param flowContext       {@link ClassFlowContext}.
	 * @return {@link ClassFlow}.
	 */
	protected ClassFlow addFlow(ClassDependencyManufacturerContext dependencyContext, ClassFlowContext flowContext) {

		// Create the flow
		String flowName = flowContext.getMethod().getName();
		ClassFlow flow = dependencyContext.newFlow(flowName).setArgumentType(flowContext.getParameterType())
				.addAnnotations(Arrays.asList(dependencyContext.getDependencyAnnotations()))
				.addAnnotations(Arrays.asList(flowContext.getMethod().getAnnotations()));

		// Return the flow
		return flow;
	}

	/**
	 * Builds the {@link Flow}
	 * 
	 * @param classFlow   {@link ClassFlow}.
	 * @param flowContext {@link ClassFlowContext}.
	 * @return Index of the {@link Flow}.
	 */
	protected int buildFlow(ClassFlow classFlow, ClassFlowContext flowContext) {
		return classFlow.build().getIndex();
	}

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
		ClassFlowInterfaceFactory factory = flowBuilder.buildFlowInterfaceFactory(dependencyType,
				(flowContext) -> this.buildFlow(this.addFlow(context, flowContext), flowContext), sourceContext);

		// Create and return the factory
		return new FlowClassDependencyFactory(factory);
	}

}
