/*-
 * #%L
 * OpenAPI
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
