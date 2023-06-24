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

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import jakarta.inject.Inject;
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
			if (annotationType.isAnnotationPresent(jakarta.inject.Qualifier.class)) {
				qualifier = annotationType.getName();
			}
		}

		// Return the required dependency
		Class<?> type = field.getType();
		return new RequiredDependency(qualifier, type);
	}

}
