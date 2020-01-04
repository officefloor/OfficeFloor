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

package net.officefloor.compile.section;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * <code>Type definition</code> of an {@link Office}
 * {@link SectionManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeSectionManagedObjectSourceType {

	/**
	 * Obtains the name of this {@link Office}
	 * {@link SectionManagedObjectSource}.
	 * 
	 * @return Name of this {@link Office} {@link SectionManagedObjectSource}.
	 */
	String getOfficeSectionManagedObjectSourceName();

	/**
	 * <p>
	 * Obtains the {@link OfficeSectionManagedObjectTeamType} instances required by this
	 * {@link Office} {@link SectionManagedObjectSource}.
	 * <p>
	 * Should there be an issue by the underlying {@link ManagedObjectSource}
	 * providing the listing, an empty array will be returned with an issue
	 * reported to the {@link CompilerIssues}.
	 * 
	 * @return {@link OfficeSectionManagedObjectTeamType} instances required by this
	 *         {@link Office} {@link SectionManagedObjectSource}.
	 */
	OfficeSectionManagedObjectTeamType[] getOfficeSectionManagedObjectTeamTypes();

}
