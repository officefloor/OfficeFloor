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
package net.officefloor.web.spi.security;

import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.ProcessManager;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectStartupProcess;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Context that the {@link HttpSecurity} is to execute within.
 * <p>
 * This is similar to the {@link ManagedObjectExecuteContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecurityExecuteContext<F extends Enum<F>> {

	/**
	 * Registers a start up {@link Flow}.
	 * 
	 * @param key       Key identifying the {@link Flow} to instigate.
	 * @param parameter Parameter to first {@link ManagedFunction} of the
	 *                  {@link Flow}.
	 * @param callback  {@link FlowCallback} on completion of the {@link Flow}.
	 * @return {@link ManagedObjectStartupProcess}.
	 * @throws IllegalArgumentException If
	 *                                  <ul>
	 *                                  <li>unknown {@link Flow} key</li>
	 *                                  <li>parameter is incorrect type</li>
	 *                                  </ul>
	 */
	ManagedObjectStartupProcess registerStartupProcess(F key, Object parameter, FlowCallback callback)
			throws IllegalArgumentException;

	/**
	 * Instigates a {@link Flow}.
	 * 
	 * @param key       Key identifying the {@link Flow} to instigate.
	 * @param parameter Parameter to first {@link ManagedFunction} of the
	 *                  {@link Flow}.
	 * @param delay     Delay in milliseconds before the {@link Flow} is invoked. A
	 *                  <code>0</code> or negative value invokes the {@link Flow}
	 *                  immediately.
	 * @param callback  {@link FlowCallback} on completion of the {@link Flow}.
	 * @return {@link ProcessManager} for the {@link ProcessState}.
	 * @throws IllegalArgumentException If
	 *                                  <ul>
	 *                                  <li>unknown {@link Flow} key</li>
	 *                                  <li>parameter is incorrect type</li>
	 *                                  </ul>
	 */
	ProcessManager invokeProcess(F key, Object parameter, long delay, FlowCallback callback)
			throws IllegalArgumentException;

}