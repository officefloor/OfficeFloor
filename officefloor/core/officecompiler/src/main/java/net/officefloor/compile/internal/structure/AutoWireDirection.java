/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.compile.internal.structure;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Team;

/**
 * Direction of {@link AutoWire}.
 * 
 * @author Daniel Sagenschneider
 */
public enum AutoWireDirection {

	/**
	 * <p>
	 * Flags that the source requires to use the target. Hence, target must be child
	 * of source.
	 * <p>
	 * This is typically used in {@link ManagedObject} auto-wirings to provide
	 * dependent {@link ManagedObject}.
	 */
	SOURCE_REQUIRES_TARGET,

	/**
	 * <p>
	 * Flags that the target categories the source. Hence, source must be child of
	 * target.
	 * <p>
	 * This is typically used in {@link Team} auto-wirings to assign
	 * {@link ManagedFunction} to {@link Team}.
	 */
	TARGET_CATEGORISES_SOURCE
}