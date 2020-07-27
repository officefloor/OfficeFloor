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
