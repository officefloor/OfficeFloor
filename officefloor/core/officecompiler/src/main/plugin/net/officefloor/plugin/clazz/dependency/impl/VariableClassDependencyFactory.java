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
