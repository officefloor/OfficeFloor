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

package net.officefloor.plugin.clazz.constructor.impl;

import java.lang.reflect.Constructor;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.constructor.ClassConstructorInterrogator;
import net.officefloor.plugin.clazz.constructor.ClassConstructorInterrogatorContext;
import net.officefloor.plugin.clazz.constructor.ClassConstructorInterrogatorServiceFactory;

/**
 * {@link ClassConstructorInterrogator} for {@link Dependency}.
 * 
 * @author Daniel Sagenschneider
 */
public class DependencyClassConstructorInterrogator
		implements ClassConstructorInterrogator, ClassConstructorInterrogatorServiceFactory {

	/*
	 * =============== ClassConstructorInterrogatorServiceFactory ================
	 */

	@Override
	public ClassConstructorInterrogator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== ClassConstructorInterrogator =======================
	 */

	@Override
	public Constructor<?> interrogate(ClassConstructorInterrogatorContext context) throws Exception {

		// Obtain the class
		Class<?> clazz = context.getObjectClass();

		// Obtain the constructor
		Constructor<?>[] constructors = clazz.getConstructors();
		switch (constructors.length) {
		case 0:
			context.setErrorInformation("No public constructor available for " + clazz.getName());

		case 1:
			// Use only constructor
			return constructors[0];

		default:
			// Search for annotated constructor
			Constructor<?> annotatedConstructor = null;
			for (Constructor<?> constructor : constructors) {
				if (constructor.isAnnotationPresent(Dependency.class)) {

					// Ensure no other annotated constructor
					if (annotatedConstructor != null) {
						context.setErrorInformation("Multiple constructors annotated with "
								+ Dependency.class.getSimpleName() + " for " + clazz.getName());
						return null; // constructor not found
					}

					// Flag as constructor to use
					annotatedConstructor = constructor;
				}
			}
			if (annotatedConstructor != null) {
				return annotatedConstructor; // found constructor to use

			} else {
				// No annotated constructor
				context.setErrorInformation("Must specify one of the constructors with "
						+ Dependency.class.getSimpleName() + " for " + clazz.getName());
				return null; // constructor not found
			}
		}
	}

}
