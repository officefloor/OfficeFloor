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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.StateManager;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.internal.structure.OfficeMetaData;
import net.officefloor.frame.internal.structure.ProcessMetaData;

/**
 * {@link Office} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeImpl implements Office {

	/**
	 * {@link OfficeMetaData} for this {@link Office}.
	 */
	private final OfficeMetaData metaData;

	/**
	 * Initiate.
	 * 
	 * @param metaData {@link OfficeMetaData}.
	 */
	public OfficeImpl(OfficeMetaData metaData) {
		this.metaData = metaData;
	}

	/*
	 * ====================== Office ==================================
	 */

	@Override
	public String[] getFunctionNames() {

		// Obtain the function names
		ManagedFunctionMetaData<?, ?>[] functionMetaData = this.metaData.getManagedFunctionMetaData();
		String[] functionNames = new String[functionMetaData.length];
		for (int i = 0; i < functionNames.length; i++) {
			functionNames[i] = functionMetaData[i].getFunctionName();
		}

		// Return the function names
		return functionNames;
	}

	@Override
	public FunctionManager getFunctionManager(String functionName) throws UnknownFunctionException {

		// Obtain the function meta-data for the function
		ManagedFunctionMetaData<?, ?> functionMetaData = this.metaData.getManagedFunctionLocator()
				.getManagedFunctionMetaData(functionName);
		if (functionMetaData == null) {
			throw new UnknownFunctionException(functionName);
		}

		// Have function meta-data, so return function manager for it
		return new FunctionManagerImpl(functionMetaData, this.metaData);
	}

	@Override
	public String[] getObjectNames() {

		// Obtain the object names
		Set<String> objectNames = new HashSet<>();

		// Load the process bound object names
		ProcessMetaData processMetaData = this.metaData.getProcessMetaData();
		for (ManagedObjectMetaData<?> mo : processMetaData.getManagedObjectMetaData()) {
			objectNames.add(mo.getBoundManagedObjectName());
		}

		// Load the thread bound object names
		for (ManagedObjectMetaData<?> mo : processMetaData.getThreadMetaData().getManagedObjectMetaData()) {
			objectNames.add(mo.getBoundManagedObjectName());
		}

		// Provide ordered listing of names
		String[] names = objectNames.toArray(new String[objectNames.size()]);
		Arrays.sort(names);

		// Return the names
		return names;
	}

	@Override
	public StateManager createStateManager() {
		return this.metaData.createStateManager();
	}

}
