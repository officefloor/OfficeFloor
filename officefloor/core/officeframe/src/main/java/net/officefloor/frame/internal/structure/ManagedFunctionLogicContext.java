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

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;

/**
 * Context for the execution of a {@link ManagedFunction}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionLogicContext {

	/**
	 * Specifies the next {@link FunctionLogic} to be executed before the next
	 * {@link ManagedFunctionLogic}.
	 * 
	 * @param function
	 *            Next {@link FunctionLogic}.
	 */
	void next(FunctionLogic function);

	/**
	 * Obtains the {@link Object} from a {@link ManagedObject}.
	 * 
	 * @param index
	 *            {@link ManagedObjectIndex} identifying the
	 *            {@link ManagedObject}.
	 * @return Object from the {@link ManagedObject}.
	 */
	Object getObject(ManagedObjectIndex index);

	/**
	 * Invokes a {@link Flow}.
	 * 
	 * @param flowIndex
	 *            Index of the {@link Flow}.
	 * @param parameter
	 *            Parameter for the initial {@link ManagedFunction} of the
	 *            {@link Flow}.
	 * @param callback
	 *            Optional {@link FlowCallback}. May be <code>null</code>.
	 */
	void doFlow(int flowIndex, Object parameter, FlowCallback callback);

}