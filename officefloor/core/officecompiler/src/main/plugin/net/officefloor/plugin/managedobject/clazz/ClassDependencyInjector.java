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

package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Method;

import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;

/**
 * Injector of dependencies into an object.
 * 
 * @author Daniel Sagenschneider
 */
public interface ClassDependencyInjector {

	/**
	 * Injects dependencies into the object.
	 * 
	 * @param object        Object to receive the dependencies.
	 * @param managedObject {@link ManagedObject}.
	 * @param context       {@link ManagedObjectContext}.
	 * @param registry      {@link ObjectRegistry}.
	 * @throws Throwable If fails to inject the dependencies.
	 */
	void injectDependencies(Object object, ManagedObject managedObject, ManagedObjectContext context,
			ObjectRegistry<Indexed> registry) throws Throwable;

	/**
	 * Injects dependencies into the object (typically to invoke {@link Method}
	 * against).
	 * 
	 * @param object  Object to receive the dependencies.
	 * @param context {@link ManagedFunctionContext}.
	 * @throws Throwable If fails to inject the dependencies.
	 */
	void injectDependencies(Object object, ManagedFunctionContext<Indexed, Indexed> context) throws Throwable;

	/**
	 * Injects dependencies into the object (typically to invoke {@link Method}
	 * against).
	 * 
	 * @param object  Object to receive the dependencies.
	 * @param context {@link AdministrationContext}.
	 * @throws Throwable If fails to inject the dependencies.
	 */
	void injectDependencies(Object object, AdministrationContext<Object, Indexed, Indexed> context) throws Throwable;
}
