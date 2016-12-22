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
import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.ManagedFunctionContext;
import net.officefloor.frame.spi.administration.Administrator;

/**
 * Context for an {@link Administrator}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AdministratorContext {

	/**
	 * Obtains the {@link ThreadState}.
	 * 
	 * @return {@link ThreadState}.
	 */
	ThreadState getThreadState();

	/**
	 * Instigates a flow to be run.
	 * 
	 * @param flowMetaData
	 *            {@link FlowMetaData} of the {@link Flow} to instigate.
	 * @param parameter
	 *            Parameter that will be available from the
	 *            {@link ManagedFunctionContext#getObject(int)} of the first {@link ManagedFunction}
	 *            of the flow to be run.
	 * @param callback
	 *            {@link FlowCallback} to be invoked once the {@link Flow}
	 *            completes.
	 */
	void doFlow(FlowMetaData<?> flowMetaData, Object parameter, FlowCallback callback);

}