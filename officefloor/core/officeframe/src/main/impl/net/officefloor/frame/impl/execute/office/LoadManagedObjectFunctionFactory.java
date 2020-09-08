/*-
 * #%L
 * OfficeFrame
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
