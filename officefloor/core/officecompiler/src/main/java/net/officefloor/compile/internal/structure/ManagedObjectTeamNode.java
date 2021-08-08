/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
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
