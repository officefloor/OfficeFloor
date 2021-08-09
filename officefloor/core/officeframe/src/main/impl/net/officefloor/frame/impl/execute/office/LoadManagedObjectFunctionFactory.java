/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.office;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * {@link ManagedFunctionFactory} to load the object of a {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public class LoadManagedObjectFunctionFactory
		extends StaticManagedFunction<LoadManagedObjectFunctionFactory.Dependencies, None> {

	/**
	 * Dependency keys for the {@link LoadManagedObjectFunctionFactory}.
	 */
	public static enum Dependencies {
		PARAMETER, MANAGED_OBJECT
	}

	/**
	 * Interface for parameter to receive the loaded object of the
	 * {@link ManagedObject}.
	 */
	@FunctionalInterface
	public static interface LoadManagedObjectParameter {

		/**
		 * Loads the object.
		 * 
		 * @param object Object loaded from {@link ManagedObject}.
		 */
		void load(Object object);
	}

	/*
	 * ====================== ManagedFunction ======================
	 */

	@Override
	public void execute(ManagedFunctionContext<Dependencies, None> context) throws Throwable {

		// Obtain the dependencies
		LoadManagedObjectParameter parameter = (LoadManagedObjectParameter) context.getObject(Dependencies.PARAMETER);
		Object object = context.getObject(Dependencies.MANAGED_OBJECT);

		// Load the managed object
		parameter.load(object);
	}

}
