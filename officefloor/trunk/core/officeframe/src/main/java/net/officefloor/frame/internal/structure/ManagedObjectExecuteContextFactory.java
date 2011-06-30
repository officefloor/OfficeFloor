/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

import java.util.Timer;

import net.officefloor.frame.spi.managedobject.source.ManagedObjectExecuteContext;

/**
 * Factory for the creation of the {@link ManagedObjectExecuteContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecuteContextFactory<F extends Enum<F>> {

	/**
	 * Creates the {@link ManagedObjectExecuteContext}.
	 * 
	 * @param ticker
	 *            {@link ProcessTicker} to keep track of the active
	 *            {@link ProcessState} instances.
	 * @param timer
	 *            {@link Timer} to enable delay of {@link ProcessState}
	 *            invocation.
	 * @return {@link ManagedObjectExecuteContext}.
	 */
	ManagedObjectExecuteContext<F> createManagedObjectExecuteContext(
			ProcessTicker ticker, Timer timer);

}