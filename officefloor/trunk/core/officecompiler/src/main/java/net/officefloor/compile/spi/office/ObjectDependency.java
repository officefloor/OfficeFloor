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
package net.officefloor.compile.spi.office;

import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.work.TaskObjectType;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Dependency of an {@link OfficeTask} or {@link ManagedObject} on a
 * {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
@Deprecated // TODO remove as now use ObjectDependencyType
public interface ObjectDependency {

	/**
	 * <p>
	 * Obtains the name of this {@link ObjectDependency}.
	 * <p>
	 * This would correspond to either the {@link TaskObjectType} or the
	 * {@link ManagedObjectDependencyType} name.
	 * 
	 * @return Name of this {@link ObjectDependency}.
	 */
	String getObjectDependencyName();

}