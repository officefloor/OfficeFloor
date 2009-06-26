/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.compile.office;

import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * <code>Type definition</code> of a {@link ManagedObject} required by the
 * {@link OfficeType}.
 * 
 * @author Daniel Sagenschneider
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