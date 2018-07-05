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
package net.officefloor.frame.api.managedobject.source;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.internal.structure.Flow;

/**
 * {@link Flow} to be configured from the {@link ManagedObjectSource} (invoked
 * via {@link ManagedObjectExecuteContext}).
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceFlow {

	/**
	 * <p>
	 * Links in a {@link Flow} by specifying the first {@link ManagedFunction}
	 * of the {@link Flow}.
	 * <p>
	 * The {@link ManagedFunction} must be registered by this
	 * {@link ManagedObjectSource}.
	 * 
	 * @param functionName
	 *            Name of {@link ManagedFunction}.
	 */
	void linkFunction(String functionName);

}