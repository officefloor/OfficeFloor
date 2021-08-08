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

package net.officefloor.plugin.clazz.constructor;

import java.lang.reflect.Constructor;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.frame.api.source.ServiceFactory;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.InvalidConfigurationError;

/**
 * {@link ServiceFactory} for the {@link ClassConstructorInterrogator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassConstructorInterrogatorServiceFactory extends ServiceFactory<ClassConstructorInterrogator> {

	/**
	 * Extracts the {@link Constructor}.
	 * 
	 * @param objectClass   {@link Class} of object to inject dependencies.
	 * @param sourceContext {@link SourceContext}.
	 * @return {@link Constructor}.
	 * @throws Exception If fails to extract the {@link Constructor}.
	 */
	static Constructor<?> extractConstructor(Class<?> objectClass, SourceContext sourceContext) throws Exception {

		// Obtain the constructor
		ClassConstructorInterrogatorContextImpl constructorContext = new ClassConstructorInterrogatorContextImpl(
				objectClass);
		Constructor<?> objectConstructor = null;
		for (ClassConstructorInterrogator interrogator : sourceContext
				.loadServices(ClassConstructorInterrogatorServiceFactory.class, null)) {

			// Obtain the constructor
			objectConstructor = interrogator.interrogate(constructorContext);
			if (objectConstructor != null) {
				return objectConstructor;
			}
		}

		// Must find constructor
		String errorMessage = constructorContext.getErrorInformation();
		if (CompileUtil.isBlank(errorMessage)) {
			errorMessage = "Unable to find suitable constructor for " + objectClass.getName();
		}
		throw new InvalidConfigurationError(errorMessage);
	}

}
