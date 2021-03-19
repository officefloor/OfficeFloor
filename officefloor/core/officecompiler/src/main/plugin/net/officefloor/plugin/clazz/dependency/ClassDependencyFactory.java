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

package net.officefloor.plugin.clazz.dependency;

import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.plugin.managedobject.clazz.ClassManagedObject;

/**
 * Creates the dependency for the {@link ClassManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassDependencyFactory {

	/**
	 * Loads the {@link ManagedObjectExecuteContext}.
	 * 
	 * @param executeContext {@link ManagedObjectExecuteContext}.
	 */
	default void loadManagedObjectExecuteContext(ManagedObjectExecuteContext<Indexed> executeContext) {
		// do nothing
	}

	/**
	 * Creates the dependency for a {@link ManagedObject}.
	 * 
	 * @param managedObject {@link ManagedObject}.
	 * @param context       {@link ManagedObjectContext}.
	 * @param registry      {@link ObjectRegistry}.
	 * @return Dependency.
	 * @throws Throwable If fails to create the dependency.
	 */
	Object createDependency(ManagedObject managedObject, ManagedObjectContext context, ObjectRegistry<Indexed> registry)
			throws Throwable;

	/**
	 * Creates the dependency for a {@link ManagedFunction}.
	 * 
	 * @param context {@link ManagedFunctionContext}.
	 * @return Dependency.
	 * @throws Throwable If fails to create the dependency.
	 */
	Object createDependency(ManagedFunctionContext<Indexed, Indexed> context) throws Throwable;

	/**
	 * Creates the dependency for {@link Administration}.
	 * 
	 * @param context {@link AdministrationContext}.
	 * @return Dependency.
	 * @throws Throwable If fails to create the dependency.
	 */
	Object createDependency(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable;
}
