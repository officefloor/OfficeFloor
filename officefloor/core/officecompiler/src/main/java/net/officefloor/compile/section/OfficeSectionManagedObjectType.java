/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.section;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <code>Type definition</code> for an {@link Office}
 * {@link SectionManagedObject}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionManagedObjectType extends DependentObjectType {

	/**
	 * Obtains the name of this {@link Office} {@link SectionManagedObject}.
	 * 
	 * @return Name of this {@link Office} {@link SectionManagedObject}.
	 */
	String getOfficeSectionManagedObjectName();

	/**
	 * <p>
	 * Obtains the supported extension interfaces by this {@link Office}
	 * {@link SectionManagedObject}.
	 * <p>
	 * Should there be an issue by the underlying {@link ManagedObjectSource}
	 * providing the listing, an empty array will be returned with an issue
	 * reported to the {@link CompilerIssues}.
	 * 
	 * @return Supported extension interfaces by this
	 *         {@link OfficeSectionManagedObject}.
	 */
	Class<?>[] getSupportedExtensionInterfaces();

	/**
	 * Obtains the {@link OfficeSectionManagedObjectSourceType} for this
	 * {@link OfficeSectionManagedObject}.
	 * 
	 * @return {@link OfficeSectionManagedObjectSourceType} for this
	 *         {@link OfficeSectionManagedObject}.
	 */
	OfficeSectionManagedObjectSourceType getOfficeSectionManagedObjectSourceType();

}