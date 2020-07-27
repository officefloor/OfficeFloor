/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
