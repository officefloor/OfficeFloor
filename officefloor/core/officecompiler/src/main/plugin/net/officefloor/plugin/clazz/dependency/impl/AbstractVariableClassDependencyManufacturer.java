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
import java.util.function.Function;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext.ClassDependency;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerServiceFactory;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableAnnotation;
import net.officefloor.plugin.variable.VariableManagedObjectSource;

/**
 * {@link ClassDependencyManufacturer} for a variable.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractVariableClassDependencyManufacturer
		implements ClassDependencyManufacturer, ClassDependencyManufacturerServiceFactory {

	/**
	 * Obtains the parameter {@link Class}.
	 * 
	 * @return Parameter {@link Class}.
	 */
	protected abstract Class<?> getParameterClass();

	/**
	 * Obtains the {@link Var} transform to appropriate dependency.
	 * 
	 * @return {@link Var} transform to appropriate dependency.
	 */
	protected abstract Function<Object, Object> getVariableTransform();

	/*
	 * =========== ClassDependencyManufacturerServiceFactory ===============
	 */

	@Override
	public ClassDependencyManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================ ClassDependencyManufacturer =================
	 */

	@Override
	public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context) throws Exception {

		// Determine if appropriate type
		if (!this.getParameterClass().equals(context.getDependencyClass())) {
			return null; // not appropriate type
		}

		// Obtain the variable details
		String qualifier = context.getDependencyQualifier();
		String type = VariableManagedObjectSource.extractVariableType(context.getDependencyType());
		String qualifiedName = VariableManagedObjectSource.name(qualifier, type);

		// Add the variable
		ClassDependency dependency = context.newDependency(Var.class).setQualifier(qualifiedName);
		for (Annotation annotation : context.getDependencyAnnotations()) {
			dependency.addAnnotation(annotation);
		}
		dependency.addAnnotation(new VariableAnnotation(qualifiedName, type));
		int objectIndex = dependency.build().getIndex();

		// Return variable
		return new VariableClassDependencyFactory(objectIndex, this.getVariableTransform());
	}

}
