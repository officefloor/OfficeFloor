/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.managedobject.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.clazz.QualifierNameFactory;

/**
 * Meta-data for a {@link Dependency}.
 * 
 * @author Daniel Sagenschneider
 */
public class DependencyMetaData {

	/**
	 * Type of dependency.
	 */
	public static enum DependencyType {
		MANAGE_OBJECT_CONTEXT, LOGGER, DEPENDENCY
	}

	/**
	 * Type of the dependency.
	 */
	public final DependencyType type;

	/**
	 * Name of the dependency. Only applicable if {@link DependencyType#DEPENDENCY}.
	 */
	public final String name;

	/**
	 * Index of the dependency within the {@link ObjectRegistry}. Only applicable if
	 * {@link DependencyType#DEPENDENCY}.
	 */
	public final int index;

	/**
	 * {@link Field} to receive the injected dependency.
	 */
	public final Field field;

	/**
	 * Initiate.
	 * 
	 * @param type  {@link DependencyType}.
	 * @param field {@link Field} to receive the injected dependency.
	 */
	public DependencyMetaData(DependencyType type, Field field) {
		this.type = type;
		this.name = null;
		this.index = -1;
		this.field = field;
	}

	/**
	 * Initiate.
	 * 
	 * @param name  Name of the dependency.
	 * @param index Index of the dependency within the {@link ObjectRegistry}.
	 * @param field {@link Field} to receive the injected dependency.
	 */
	public DependencyMetaData(String name, int index, Field field) {
		this.type = DependencyType.DEPENDENCY;
		this.name = name;
		this.index = index;
		this.field = field;
	}

	/**
	 * Obtains the type qualifier for the dependency.
	 * 
	 * @return Type qualifier. May be <code>null</code> if no type qualifier.
	 * @throws Exception If fails to obtain the type qualifier.
	 */
	@SuppressWarnings("unchecked")
	public String getTypeQualifier() throws Exception {

		// Determine type qualifier
		String typeQualifier = null;
		for (Annotation annotation : this.field.getAnnotations()) {

			// Obtain the annotation type
			Class<?> annotationType = annotation.annotationType();

			// Determine if qualifier annotation
			Qualifier qualifierAnnotation = annotationType.getAnnotation(Qualifier.class);
			if (qualifierAnnotation != null) {

				// Allow only one qualifier
				if (typeQualifier != null) {
					throw new IllegalArgumentException(
							"Dependency " + this.name + " has more than one " + Qualifier.class.getSimpleName());
				}

				// Obtain the qualifier name factory
				@SuppressWarnings("rawtypes")
				Class<? extends QualifierNameFactory> nameFactoryClass = qualifierAnnotation.nameFactory();
				QualifierNameFactory<Annotation> nameFactory = nameFactoryClass.getDeclaredConstructor().newInstance();

				// Provide type qualifier
				typeQualifier = nameFactory.getQualifierName(annotation);
			}
		}

		// Return the type qualifier
		return typeQualifier;
	}

	/**
	 * Injects the dependency into the object.
	 * 
	 * @param object     Object to receive the dependency.
	 * @param dependency Dependency to inject.
	 * @throws Exception If fails to inject the dependency.
	 */
	public void injectDependency(Object object, Object dependency) throws Exception {
		this.field.set(object, dependency);
	}

}