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
