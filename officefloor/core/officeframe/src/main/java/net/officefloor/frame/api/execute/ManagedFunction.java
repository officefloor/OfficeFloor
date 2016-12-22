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
package net.officefloor.frame.api.execute;

import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionState;

/**
 * Managed {@link FunctionState}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunction<W extends Work, D extends Enum<D>, F extends Enum<F>> {

	/**
	 * Executes the {@link FunctionState}.
	 * 
	 * @param context
	 *            {@link ManagedFunctionContext} for the
	 *            {@link ManagedFunction}.
	 * @return Parameter for the next {@link ManagedFunction}. This allows
	 *         stringing {@link ManagedFunction} instances together into a
	 *         {@link Flow}.
	 * @throws Throwable
	 *             Indicating failure of the {@link ManagedFunction}.
	 */
	Object execute(ManagedFunctionContext<W, D, F> context) throws Throwable;

}