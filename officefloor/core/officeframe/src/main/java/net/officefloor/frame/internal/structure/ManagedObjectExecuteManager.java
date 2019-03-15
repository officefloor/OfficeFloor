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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * Manages the {@link ManagedObjectExecuteContext}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectExecuteManager<F extends Enum<F>> {

	/**
	 * Obtains the {@link ManagedObjectExecuteContext}.
	 * 
	 * @return {@link ManagedObjectExecuteContext}.
	 */
	ManagedObjectExecuteContext<F> getManagedObjectExecuteContext();

	/**
	 * Invoked to indicate start for the corresponding {@link ManagedObjectSource}
	 * has completed.
	 * 
	 * @return {@link ManagedObjectStartupRunnable} instances to execute once ready
	 *         to start processing.
	 */
	ManagedObjectStartupRunnable[] startComplete();

}