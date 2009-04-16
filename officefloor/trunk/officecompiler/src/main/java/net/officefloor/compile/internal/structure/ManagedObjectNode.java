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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * {@link SectionManagedObject} node.
 * 
 * @author Daniel
 */
public interface ManagedObjectNode extends SectionManagedObject,
		OfficeSectionManagedObject, OfficeManagedObject,
		OfficeFloorManagedObject, LinkObjectNode {

	/**
	 * Adds the context of the {@link Office} containing this
	 * {@link SectionManagedObject}.
	 * 
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	void addOfficeContext(String officeLocation);

	/**
	 * Adds the context of the {@link OfficeFloor} containing this
	 * {@link OfficeFloorManagedObject}.
	 * 
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 */
	void addOfficeFloorContext(String officeFloorLocation);

	/**
	 * Loads the {@link ManagedObjectMetaData} of this
	 * {@link SectionManagedObject}.
	 * 
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 */
	void loadManagedObjectMetaData(ConfigurationContext configurationContext,
			ClassLoader classLoader);

}