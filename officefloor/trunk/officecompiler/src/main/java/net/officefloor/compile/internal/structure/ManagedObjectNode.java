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
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.managedobject.ManagedObject;

/**
 * Node representing an instance use of a {@link ManagedObject}..
 * 
 * @author Daniel
 */
public interface ManagedObjectNode extends SectionManagedObject,
		OfficeSectionManagedObject, OfficeManagedObject,
		OfficeFloorManagedObject, LinkObjectNode {

	/**
	 * Obtains the name under which this {@link ManagedObject} is made available
	 * to the {@link Office}.
	 * 
	 * @return Name under which this {@link ManagedObject} is made available to
	 *         the {@link Office}.
	 */
	String getManagedObjectName();

	/**
	 * <p>
	 * Builds the {@link ManagedObject} into the {@link Office}.
	 * <p>
	 * This may be called more than once for an {@link Office} due to dependency
	 * management. Only the first invocation should build this
	 * {@link ManagedObject} into the {@link Office} and all further invocations
	 * are to be ignored.
	 * 
	 * @param office
	 *            {@link OfficeNode} of the {@link Office} that this
	 *            {@link ManagedObject} is to build itself into.
	 * @param officeBuilder
	 *            {@link OfficeBuilder} for the {@link Office}.
	 */
	void buildOfficeManagedObject(OfficeNode office, OfficeBuilder officeBuilder);

}