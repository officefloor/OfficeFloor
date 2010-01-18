/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * <p>
 * Provides the mappings of the dependencies of a {@link ManagedObject} to the
 * {@link ManagedObject} providing necessary functionality.
 * <p>
 * This works within the scope of where the {@link ManagedObject} is being
 * added.
 * 
 * @author Daniel Sagenschneider
 */
public interface DependencyMappingBuilder {

	/**
	 * Specifies the {@link ManagedObject} for the dependency key.
	 * 
	 * @param key
	 *            Key of the dependency.
	 * @param scopeManagedObjectName
	 *            Name of the {@link ManagedObject} within the scope that this
	 *            {@link DependencyMappingBuilder} was created.
	 */
	<D extends Enum<D>> void mapDependency(D key, String scopeManagedObjectName);

	/**
	 * Specifies the {@link ManagedObject} for the index identifying the
	 * dependency.
	 * 
	 * @param index
	 *            Index identifying the dependency.
	 * @param scopeManagedObjectName
	 *            Name of the {@link ManagedObject} within the scope that this
	 *            {@link DependencyMappingBuilder} was created.
	 */
	void mapDependency(int index, String scopeManagedObjectName);

}