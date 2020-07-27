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
