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
