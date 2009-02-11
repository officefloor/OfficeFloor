/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
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
 * @author Daniel
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

}