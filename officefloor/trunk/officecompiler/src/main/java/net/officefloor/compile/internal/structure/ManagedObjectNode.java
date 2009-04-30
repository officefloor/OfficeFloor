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
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectMetaData;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * Node representing an instance use of a {@link ManagedObject}..
 * 
 * @author Daniel
 */
public interface ManagedObjectNode extends SectionManagedObject,
		OfficeSectionManagedObject, OfficeManagedObject,
		OfficeFloorManagedObject, LinkObjectNode {

	/**
	 * <p>
	 * Registers this {@link ManagedObject} to the {@link Office}.
	 * <p>
	 * This may be called more than once for an {@link Office} due to dependency
	 * management. Only the first invocation should register this
	 * {@link ManagedObject} and all further invocations are to be ignored.
	 * 
	 * @param office
	 *            {@link OfficeNode} of the {@link Office} that this
	 *            {@link ManagedObject} is to register.
	 * @param objectNode
	 *            {@link OfficeObject} that represents this
	 *            {@link ManagedObject} within the {@link Office}. May be
	 *            <code>null</code> if is a dependency of a
	 *            {@link ManagedObject} that is not directly linked to the
	 *            {@link Office}.
	 * @param officeBuilder
	 *            {@link OfficeBuilder} of the {@link Office}.
	 */
	void registerToOffice(OfficeNode office, OfficeObject object,
			OfficeBuilder officeBuilder);

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