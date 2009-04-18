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
package net.officefloor.compile.office;

import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * <code>Type definition</code> of a {@link ManagedObject} required by the
 * {@link OfficeType}.
 * 
 * @author Daniel
 */
public interface OfficeManagedObjectType {

	/**
	 * Obtains the name of the {@link OfficeObject} required by the
	 * {@link Office}.
	 * 
	 * @return Name of the {@link OfficeObject} required by the
	 *         {@link Office}.
	 */
	String getOfficeManagedObjectName();

	/**
	 * Obtains the fully qualified class name of the {@link Object} that must be
	 * returned from the {@link ManagedObject}.
	 * 
	 * @return Fully qualified class name of the {@link Object} that must be
	 *         returned from the {@link ManagedObject}.
	 */
	String getObjectType();

	/**
	 * Obtains the fully qualified class names of the extension interfaces that
	 * must be supported by the {@link ManagedObject}.
	 * 
	 * @return Fully qualified class names of the extension interfaces that must
	 *         be supported by the {@link ManagedObject}.
	 */
	String[] getExtensionInterfaces();

}