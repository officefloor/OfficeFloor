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

import java.util.function.Function;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.variable.Var;

/**
 * {@link ClassDependencyFactory} for a {@link Var} appropriately wrapped.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableClassDependencyFactory implements ClassDependencyFactory {

	/**
	 * Index of the dependency.
	 */
	private final int dependencyIndex;

	/**
	 * Transforms the {@link Var} to appropriate dependency.
	 */
	private final Function<Object, Object> transform;

	/**
	 * Instantiate.
	 * 
	 * @param dependencyIndex Index of the dependency.
	 * @param transform       Transforms the {@link Var} to appropriate dependency.
	 */
	public VariableClassDependencyFactory(int dependencyIndex, Function<Object, Object> transform) {
		this.dependencyIndex = dependencyIndex;
		this.transform = transform;
	}

	/*
	 * ==================== ClassDependencyFactory =======================
	 */

	@Override
	public Object createDependency(ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Throwable {
		Object dependency = registry.getObject(this.dependencyIndex);
		return this.transform.apply(dependency);
	}

	@Override
	public Object createDependency(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable {
		Object dependency = context.getObject(this.dependencyIndex);
		return this.transform.apply(dependency);
	}

	@Override
	public Object createDependency(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable {
		throw new IllegalStateException("Variable not available to " + Administration.class.getSimpleName());
	}

}
