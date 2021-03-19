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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import javax.inject.Inject;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link Inject} {@link FieldDependencyExtractor}.
 * 
 * @author Daniel Sagenschneider
 */
public class InjectFieldDependencyExtractor
		implements FieldDependencyExtractor, FieldDependencyExtractorServiceFactory {

	/*
	 * =================== FieldDependencyExtractorServiceFactory =================
	 */

	@Override
	public FieldDependencyExtractor createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public RequiredDependency extractRequiredDependency(Field field) {

		// Determine if inject
		if (!field.isAnnotationPresent(Inject.class)) {
			return null; // not inject
		}

		// Determine qualifier
		String qualifier = null;
		for (Annotation annotation : field.getAnnotations()) {
			Class<?> annotationType = annotation.annotationType();
			if (annotationType.isAnnotationPresent(javax.inject.Qualifier.class)) {
				qualifier = annotationType.getName();
			}
		}

		// Return the required dependency
		Class<?> type = field.getType();
		return new RequiredDependency(qualifier, type);
	}

}
