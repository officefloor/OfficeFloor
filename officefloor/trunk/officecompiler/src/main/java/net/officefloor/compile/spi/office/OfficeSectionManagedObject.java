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
package net.officefloor.compile.spi.office;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObject} contained within an {@link OfficeSubSection}.
 * 
 * @author Daniel
 */
public interface OfficeSectionManagedObject extends DependentManagedObject,
		AdministerableManagedObject {

	/**
	 * Obtains the name of this {@link OfficeSectionManagedObject}.
	 * 
	 * @return Name of this {@link OfficeSectionManagedObject}.
	 */
	String getOfficeSectionManagedObjectName();

	/**
	 * <p>
	 * Obtains the {@link ManagedObjectTeam} instances required by this
	 * {@link OfficeSectionManagedObject}.
	 * <p>
	 * Should there be an issue by the underlying {@link ManagedObjectSource}
	 * providing the listing, an empty array will be returned with an issue
	 * reported to the {@link CompilerIssues}.
	 * 
	 * @return {@link ManagedObjectTeam} instances required by this
	 *         {@link OfficeSectionManagedObject}.
	 */
	ManagedObjectTeam[] getOfficeSectionManagedObjectTeams();

	/**
	 * <p>
	 * Obtains the supported extension interfaces by this
	 * {@link OfficeSectionManagedObject}.
	 * <p>
	 * Should there be an issue by the underlying {@link ManagedObjectSource}
	 * providing the listing, an empty array will be returned with an issue
	 * reported to the {@link CompilerIssues}.
	 * 
	 * @return Supported extension interfaces by this
	 *         {@link OfficeSectionManagedObject}.
	 */
	Class<?>[] getSupportedExtensionInterfaces();

}