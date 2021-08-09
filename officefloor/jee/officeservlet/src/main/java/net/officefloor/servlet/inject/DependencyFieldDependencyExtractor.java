/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet.inject;

import java.lang.reflect.Field;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogation;
import net.officefloor.plugin.clazz.state.StatePoint;

/**
 * {@link Dependency} {@link FieldDependencyExtractor}.
 * 
 * @author Daniel Sagenschneider
 */
public class DependencyFieldDependencyExtractor implements FieldDependencyExtractorServiceFactory {

	/*
	 * =================== FieldDependencyExtractorServiceFactory =================
	 */

	@Override
	public FieldDependencyExtractor createService(ServiceContext context) throws Throwable {
		return new DependencyFieldDependencyExtractorImpl(context);
	}

	/**
	 * {@link Dependency} {@link FieldDependencyExtractor}.
	 */
	private static class DependencyFieldDependencyExtractorImpl implements FieldDependencyExtractor {

		/**
		 * {@link ServiceContext}.
		 */
		private final ServiceContext context;

		/**
		 * Instantiate.
		 * 
		 * @param context {@link ServiceContext}.
		 */
		private DependencyFieldDependencyExtractorImpl(ServiceContext context) {
			this.context = context;
		}

		/*
		 * ==================== FieldDependencyExtractor ==========================
		 */

		@Override
		public RequiredDependency extractRequiredDependency(Field field) throws Exception {

			// Determine if dependency
			if (!field.isAnnotationPresent(Dependency.class)) {
				return null; // not dependency
			}

			// Obtain the dependency details
			String qualifier = new TypeQualifierInterrogation(this.context).extractTypeQualifier(StatePoint.of(field));
			Class<?> type = field.getType();

			// Return the required dependency
			return new RequiredDependency(qualifier, type);
		}
	}

}
