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
import java.util.function.Function;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturer;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext.ClassDependency;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerServiceFactory;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableAnnotation;
import net.officefloor.plugin.variable.VariableManagedObjectSource;

/**
 * {@link ClassDependencyManufacturer} for a variable.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractVariableClassDependencyManufacturer
		implements ClassDependencyManufacturer, ClassDependencyManufacturerServiceFactory {

	/**
	 * Obtains the parameter {@link Class}.
	 * 
	 * @return Parameter {@link Class}.
	 */
	protected abstract Class<?> getParameterClass();

	/**
	 * Obtains the {@link Var} transform to appropriate dependency.
	 * 
	 * @return {@link Var} transform to appropriate dependency.
	 */
	protected abstract Function<Object, Object> getVariableTransform();

	/*
	 * =========== ClassDependencyManufacturerServiceFactory ===============
	 */

	@Override
	public ClassDependencyManufacturer createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================ ClassDependencyManufacturer =================
	 */

	@Override
	public ClassDependencyFactory createParameterFactory(ClassDependencyManufacturerContext context) throws Exception {

		// Determine if appropriate type
		if (!this.getParameterClass().equals(context.getDependencyClass())) {
			return null; // not appropriate type
		}

		// Obtain the variable details
		String qualifier = context.getDependencyQualifier();
		String type = VariableManagedObjectSource.extractVariableType(context.getDependencyType());
		String qualifiedName = VariableManagedObjectSource.name(qualifier, type);

		// Add the variable
		ClassDependency dependency = context.newDependency(Var.class).setQualifier(qualifiedName);
		for (Annotation annotation : context.getDependencyAnnotations()) {
			dependency.addAnnotation(annotation);
		}
		dependency.addAnnotation(new VariableAnnotation(qualifiedName, type));
		int objectIndex = dependency.build().getIndex();

		// Return variable
		return new VariableClassDependencyFactory(objectIndex, this.getVariableTransform());
	}

}
