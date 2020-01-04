/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.spi.office;

import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

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
	 * Obtains the {@link OfficeSectionManagedObjectTeam} required by this
	 * {@link OfficeSectionManagedObjectSource}.
	 * 
	 * @param teamName Name of the {@link ManagedObjectTeam}.
	 * @return {@link OfficeSectionManagedObjectTeam}.
	 */
	OfficeSectionManagedObjectTeam getOfficeSectionManagedObjectTeam(String teamName);

	/**
	 * Obtains the {@link OfficeSectionManagedObject} use of this
	 * {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectName Name of the {@link OfficeSectionManagedObject} to
	 *                          obtain.
	 * @return {@link OfficeSectionManagedObject}.
	 */
	OfficeSectionManagedObject getOfficeSectionManagedObject(String managedObjectName);

}
