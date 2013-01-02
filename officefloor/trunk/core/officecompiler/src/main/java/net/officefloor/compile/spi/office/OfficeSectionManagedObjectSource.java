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

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectSource} contained within a {@link OfficeSection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionManagedObjectSource {

	/**
	 * Obtains the name of this {@link OfficeSectionManagedObjectSource}.
	 * 
	 * @return Name of this {@link OfficeSectionManagedObjectSource}.
	 */
	String getOfficeSectionManagedObjectSourceName();

	/**
	 * <p>
	 * Obtains the {@link ManagedObjectTeam} instances required by this
	 * {@link OfficeSectionManagedObjectSource}.
	 * <p>
	 * Should there be an issue by the underlying {@link ManagedObjectSource}
	 * providing the listing, an empty array will be returned with an issue
	 * reported to the {@link CompilerIssues}.
	 * 
	 * @return {@link ManagedObjectTeam} instances required by this
	 *         {@link OfficeSectionManagedObjectSource}.
	 */
	ManagedObjectTeam[] getOfficeSectionManagedObjectTeams();

	/**
	 * Obtains the {@link OfficeSectionManagedObject} instance uses of
	 * {@link ManagedObject} from the {@link ManagedObjectSource}.
	 * 
	 * @return {@link OfficeSectionManagedObject} instance uses of
	 *         {@link ManagedObject} from the {@link ManagedObjectSource}.
	 */
	OfficeSectionManagedObject[] getOfficeSectionManagedObjects();

}