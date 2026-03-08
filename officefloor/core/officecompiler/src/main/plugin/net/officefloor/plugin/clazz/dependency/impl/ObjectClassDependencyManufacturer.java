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

import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;
import net.officefloor.plugin.section.clazz.MethodParameterAnnotation;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

/**
 * {@link ClassDependencyManufacturer} for providing a dependency
 * {@link Object}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectClassDependencyManufacturer implements ClassDependencyManufacturer {

	/*
	 * ======================== ClassDependencyManufacturer =======================
	 */

	@Override
	public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context) throws Exception {

		// Obtain the details of the dependency
		String qualifier = context.getDependencyQualifier();
		Class<?> requiredType = context.getDependencyClass();

		// Add the dependency
		ClassDependencyManufacturerContext.ClassDependency dependency = context.newDependency(requiredType).setQualifier(qualifier);

		// Add the method parameter annotation
		Executable executable = context.getExecutable();
		int parameterIndex = context.getExecutableParameterIndex();
		if (executable instanceof Method) {
			Method method = (Method) executable;
			dependency.addAnnotation(new MethodParameterAnnotation() {
				@Override
				public Method getMethod() {
					return method;
				}

				@Override
				public int getParameterIndex() {
					return parameterIndex;
				}
			});
		}

		// Add the dependency
		int dependencyIndex = dependency.build().getIndex();

		// Create and return the factory
		return new ObjectClassDependencyFactory(dependencyIndex);
	}

}
