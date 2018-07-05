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
package net.officefloor.frame.api.function;

/**
 * Managed function.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunction<O extends Enum<O>, F extends Enum<F>> {

	/**
	 * Executes the function.
	 * 
	 * @param context
	 *            {@link ManagedFunctionContext} for the
	 *            {@link ManagedFunction}.
	 * @return Parameter for the next {@link ManagedFunction}. This allows
	 *         stringing {@link ManagedFunction} instances together.
	 * @throws Throwable
	 *             Indicating failure of the {@link ManagedFunction}.
	 */
	Object execute(ManagedFunctionContext<O, F> context) throws Throwable;

}