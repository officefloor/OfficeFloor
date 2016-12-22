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
package net.officefloor.frame.api.build;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.execute.Work;

/**
 * Creates the {@link ManagedFunction} to be done.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionFactory<W extends Work, D extends Enum<D>, F extends Enum<F>> {

	/**
	 * Creates the {@link ManagedFunction}.
	 * 
	 * @param work
	 *            {@link Work} for the {@link ManagedFunction}.
	 * @return {@link ManagedFunction} to be done for the {@link Work}.
	 */
	ManagedFunction<W, D, F> createManagedFunction(W work);

}