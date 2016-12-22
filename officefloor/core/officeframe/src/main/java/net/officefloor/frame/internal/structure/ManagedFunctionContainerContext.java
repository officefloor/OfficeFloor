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

import net.officefloor.frame.api.execute.FlowCallback;

/**
 * Context to execute a {@link FunctionState} that is managed.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionContainerContext {

	/**
	 * Specifies a {@link FunctionState} to be executed before the
	 * {@link ManagedFunctionContainer} continues executing the graph of
	 * {@link ManagedFunctionContainer} instances.
	 * 
	 * @param nextFunction
	 *            {@link FunctionState}.
	 */
	void next(FunctionState nextFunction);

	/**
	 * Invokes the {@link Flow} for the input {@link FlowMetaData}.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData}.
	 * @param parameter
	 *            Parameter for the {@link Flow}.
	 * @param callback
	 *            Optional {@link FlowCallback}.
	 */
	void doFlow(FlowMetaData<?> flowMetaData, Object parameter, FlowCallback callback);

}