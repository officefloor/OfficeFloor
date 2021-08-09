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

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.Property;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerServiceFactory;

/**
 * {@link ClassDependencyManufacturer} for the {@link Property}.
 * 
 * @author Daniel Sagenschneider
 */
public class PropertyClassDependencyManufacturer
		implements ClassDependencyManufacturer, ClassDependencyManufacturerServiceFactory {

	/*
	 * =========== ClassDependencyManufacturerServiceFactory ===============
	 */

	@Override
	public ClassDependencyManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =================== ClassDependencyManufacturer =====================
	 */

	@Override
	public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context) throws Exception {

		// Determine if property
		Annotation[] annotations = context.getDependencyAnnotations();
		for (Annotation annotation : annotations) {

			// Determine if property
			if (Property.class.equals(annotation.annotationType())) {
				Property property = (Property) annotation;

				// Obtain the value
				String value = context.getSourceContext().getProperty(property.value());

				// Return the property parameter
				return new PropertyClassDependencyFactory(value);
			}
		}

		// Not property
		return null;
	}

}
