/*-
 * #%L
 * OpenAPI
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

package net.officefloor.web.openapi.response;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerServiceFactory;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;

/**
 * {@link ClassDependencyManufacturerServiceFactory} to provide enriched
 * annotations for the {@link ObjectResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectResponseClassDependencyManufacturerServiceFactory
		implements ClassDependencyManufacturerServiceFactory, ClassDependencyManufacturer {

	/*
	 * =============== ClassDependencyManufacturerServiceFactory ================
	 */

	@Override
	public ClassDependencyManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ======================= ClassDependencyManufacturer =======================
	 */

	@Override
	public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context) throws Exception {

		// Load the parameter type as annotation
		if (ObjectResponse.class.equals(context.getDependencyClass())) {

			// Determine the status code
			int statusCode = HttpStatus.OK.getStatusCode();
			for (Annotation annotation : context.getDependencyAnnotations()) {
				if (annotation instanceof HttpResponse) {
					HttpResponse httpResponse = (HttpResponse) annotation;
					statusCode = httpResponse.status();
				}
			}

			// Determine response type (defaulting to Object if can not determine)
			Type responseType = Object.class;
			Type type = context.getDependencyType();
			if (type instanceof ParameterizedType) {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Type[] paramTypes = parameterizedType.getActualTypeArguments();
				if (paramTypes.length > 0) {
					responseType = paramTypes[0];
				}
			}

			// Add the object response annotation
			context.addAnnotation(new ObjectResponseAnnotation(statusCode, responseType));
		}

		// Continue to inject the dependency
		return null;
	}

}
