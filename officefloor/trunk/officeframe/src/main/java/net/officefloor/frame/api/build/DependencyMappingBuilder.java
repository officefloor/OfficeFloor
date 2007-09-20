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

/**
 * Provides the mappings of the dependencies of a
 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} to the
 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} providing
 * necessary functionality.
 * 
 * @author Daniel
 */
public interface DependencyMappingBuilder {

	/**
	 * Specifies the
	 * {@link net.officefloor.frame.spi.managedobject.ManagedObject} for the
	 * dependency key.
	 * 
	 * @param key
	 *            Key of the dependency.
	 * @param managedObjectId
	 *            Id of the
	 *            {@link net.officefloor.frame.spi.managedobject.ManagedObject}
	 *            providing the dependency functionality.
	 * @throws BuildException
	 *             Indicate failure in building.
	 */
	<D extends Enum<D>> void registerDependencyMapping(D key, String managedObjectId)
			throws BuildException;

}
