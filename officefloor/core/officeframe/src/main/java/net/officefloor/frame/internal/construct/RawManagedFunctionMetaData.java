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
package net.officefloor.frame.internal.construct;

import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionMetaData;

/**
 * Raw meta-data for a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public interface RawManagedFunctionMetaData<O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Obtains the name of the {@link ManagedFunction}.
	 * 
	 * @return Name of the {@link ManagedFunction}.
	 */
	String getFunctionName();

	/**
	 * Obtains the {@link ManagedFunctionMetaData}.
	 * 
	 * @return {@link ManagedFunctionMetaData}.
	 */
	ManagedFunctionMetaData<O, F> getManagedFunctionMetaData();

	/**
	 * Links the {@link ManagedFunctionMetaData} instances to create
	 * {@link Flow} of execution.
	 * 
	 * @param functionLocator
	 *            {@link ManagedFunctionLocator}.
	 * @param issues
	 *            {@link OfficeFloorIssues}.
	 */
	void linkFunctions(ManagedFunctionLocator functionLocator, OfficeFloorIssues issues);

}