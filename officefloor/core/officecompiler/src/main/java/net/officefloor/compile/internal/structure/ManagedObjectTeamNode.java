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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.section.OfficeSectionManagedObjectTeamType;
import net.officefloor.compile.spi.office.OfficeManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectTeam;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectTeam;

/**
 * {@link OfficeTeam} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectTeamNode extends LinkTeamNode, AugmentedManagedObjectTeam, OfficeSectionManagedObjectTeam,
		OfficeManagedObjectTeam, OfficeFloorManagedObjectTeam {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Managed Object Source Team";

	/**
	 * Initialises the {@link ManagedObjectTeamNode}.
	 */
	void initialise();

	/**
	 * Loads the {@link OfficeSectionManagedObjectTeamType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link OfficeSectionManagedObjectTeamType} or <code>null</code> with
	 *         issues reported to the {@link CompilerIssues}.
	 */
	OfficeSectionManagedObjectTeamType loadOfficeSectionManagedObjectTeamType(CompileContext compileContext);

}