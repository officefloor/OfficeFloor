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

package net.officefloor.plugin.clazz.qualifier.impl;

import java.lang.annotation.Annotation;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.InvalidConfigurationError;
import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.clazz.QualifierNameFactory;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogator;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogatorContext;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogatorServiceFactory;

/**
 * {@link TypeQualifierInterrogator} for the {@link Qualifier}.
 * 
 * @author Daniel Sagenschneider
 */
public class QualifierTypeQualifierInterrogator
		implements TypeQualifierInterrogator, TypeQualifierInterrogatorServiceFactory {

	/*
	 * ================ TypeQualifierInterrogatorServiceFactory ===========
	 */

	@Override
	public TypeQualifierInterrogator createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ==================== TypeQualifierInterrogator =====================
	 */

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String interrogate(TypeQualifierInterrogatorContext context) throws Exception {

		// Determine parameter qualifier
		String qualifier = null;
		for (Annotation annotation : context.getAnnotatedElement().getAnnotations()) {

			// Obtain the annotation type
			Class<?> annotationType = annotation.annotationType();

			// Determine if qualifier annotation
			Qualifier qualifierAnnotation = annotationType.getAnnotation(Qualifier.class);
			if (qualifierAnnotation != null) {

				// Allow only one qualifier
				if (qualifier != null) {
					throw new InvalidConfigurationError(
							context.toLocation() + " has more than one " + Qualifier.class.getSimpleName());
				}

				// Obtain the qualifier name factory
				Class<? extends QualifierNameFactory> nameFactoryClass = qualifierAnnotation.nameFactory();
				QualifierNameFactory<Annotation> nameFactory = nameFactoryClass.getDeclaredConstructor().newInstance();

				// Provide type qualifier
				qualifier = nameFactory.getQualifierName(annotation);
			}
		}

		// Return the possible qualifier
		return qualifier;
	}

}
