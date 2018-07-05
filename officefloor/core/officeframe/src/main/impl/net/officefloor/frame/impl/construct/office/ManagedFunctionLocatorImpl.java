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
package net.officefloor.frame.impl.construct.office;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.internal.structure.ManagedFunctionLocator;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;

/**
 * {@link ManagedFunctionLocator} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedFunctionLocatorImpl implements ManagedFunctionLocator {

	/**
	 * {@link ManagedFunctionMetaData} instances of the {@link Office}.
	 */
	private final Map<String, ManagedFunctionMetaData<?, ?>> officeFunctions = new HashMap<String, ManagedFunctionMetaData<?, ?>>();

	/**
	 * Initiate.
	 * 
	 * @param allFunctionMetaData
	 *            Listing of all {@link ManagedFunctionMetaData} within the
	 *            {@link Office}.
	 */
	public ManagedFunctionLocatorImpl(ManagedFunctionMetaData<?, ?>[] allFunctionMetaData) {

		// Create the mapping of functions for easier access
		for (ManagedFunctionMetaData<?, ?> function : allFunctionMetaData) {
			this.officeFunctions.put(function.getFunctionName(), function);
		}
	}

	/*
	 * ================== ManagedFunctionLocator ===============================
	 */

	@Override
	public ManagedFunctionMetaData<?, ?> getManagedFunctionMetaData(String functionName) {
		return this.officeFunctions.get(functionName);
	}

}