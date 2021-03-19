/*-
 * #%L
 * Servlet
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
