/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;

/**
 * Meta-data for a {@link ProcessInterface}.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessMetaData {

	/**
	 * {@link Field} to receive the injected {@link ProcessInterface}.
	 */
	public final Field field;

	/**
	 * Indexes for the {@link Method} instances of the {@link ProcessInterface}.
	 */
	private final Map<String, Integer> methodIndexes;

	/**
	 * {@link Constructor} to create the {@link ProcessInterface}
	 * implementation.
	 */
	private final Constructor<?> processInterfaceConstructor;

	/**
	 * {@link ManagedObjectExecuteContext}.
	 */
	private final ManagedObjectExecuteContext<Indexed> executeContext;

	/**
	 * Initiate.
	 * 
	 * @param field
	 *            {@link Field} receiving the injected {@link ProcessInterface}.
	 * @param methodIndexes
	 *            Indexes for the {@link Method} instances of the
	 *            {@link ProcessInterface}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param executeContext
	 *            {@link ManagedObjectExecuteContext}.
	 * @throws Exception
	 *             If fails to create the proxy as the {@link ProcessInterface}
	 *             implementation.
	 */
	public ProcessMetaData(Field field, Map<String, Integer> methodIndexes,
			ClassLoader classLoader,
			ManagedObjectExecuteContext<Indexed> executeContext)
			throws Exception {
		this.field = field;
		this.methodIndexes = methodIndexes;
		this.executeContext = executeContext;

		// Create the proxy object to invoke processes
		this.processInterfaceConstructor = Proxy.getProxyClass(classLoader,
				field.getType()).getConstructor(InvocationHandler.class);
	}

	/**
	 * Creates the implementation of the {@link ProcessInterface} field type for
	 * the {@link ManagedObject} to be injected into the object.
	 * 
	 * @param managedObject
	 *            {@link ManagedObject} for the invoked {@link ProcessState}.
	 * @return Implementation to be injected into the object.
	 * @throws Exception
	 *             If fails to instantiate the {@link ProcessInterface}
	 *             implementation.
	 */
	public Object createProcessInterfaceImplementation(
			ManagedObject managedObject) throws Exception {

		// Create the invocation handler
		ProcessInvocationHandler handler = new ProcessInvocationHandler(
				managedObject);

		// Return the process interface implementation
		return this.processInterfaceConstructor.newInstance(handler);
	}

	/**
	 * {@link InvocationHandler} for the {@link ProcessInterface}
	 * implementation.
	 */
	private class ProcessInvocationHandler implements InvocationHandler {

		/**
		 * {@link ManagedObject}.
		 */
		private final ManagedObject managedObject;

		/**
		 * Initiate.
		 * 
		 * @param managedObject
		 *            {@link ManagedObject} for invoked {@link ProcessState}.
		 */
		public ProcessInvocationHandler(ManagedObject managedObject) {
			this.managedObject = managedObject;
		}

		/*
		 * ============= InvocationHandler =================================
		 */

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {

			// Obtain the parameter
			Object parameter = ((args != null) && (args.length == 1)) ? args[0]
					: null;

			// Obtain the index for the method
			Integer index = ProcessMetaData.this.methodIndexes.get(method
					.getName());

			// Invoke the process
			ProcessMetaData.this.executeContext.invokeProcess(index, parameter,
					this.managedObject, 0);

			// Never returns a value
			return null;
		}
	}

}