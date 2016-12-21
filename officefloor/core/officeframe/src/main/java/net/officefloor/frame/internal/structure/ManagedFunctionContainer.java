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
package net.officefloor.frame.internal.structure;

/**
 * Managed {@link FunctionState}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionContainer extends FunctionState, LinkedListSetEntry<ManagedFunctionContainer, Flow> {

	/**
	 * Obtains the {@link Flow} that this {@link ManagedFunctionContainer} is within.
	 * 
	 * @return {@link Flow} that this {@link ManagedFunctionContainer} is within.
	 */
	Flow getFlow();

	/**
	 * Enables specify the next sequential {@link ManagedFunctionContainer}.
	 * 
	 * @param nextFunction
	 *            Next sequential {@link ManagedFunctionContainer}.
	 */
	void setNextManagedFunction(ManagedFunctionContainer nextFunction);

}