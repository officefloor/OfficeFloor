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
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.OfficeTeam;

/**
 * {@link OfficeTeam} node.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeTeamNode extends LinkTeamNode, OfficeTeam {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Office Team";

	/**
	 * Initialises the {@link OfficeTeamNode}.
	 */
	void initialise();

	/**
	 * Obtains the {@link TypeQualification} instances for the
	 * {@link OfficeTeam}.
	 * 
	 * @return {@link TypeQualification} instances for the {@link OfficeTeam}.
	 */
	TypeQualification[] getTypeQualifications();

	/**
	 * Loads the {@link OfficeTeamType}.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @return {@link OfficeTeamType} or <code>null</code> with issues reported
	 *         to the {@link CompilerIssues}.
	 */
	OfficeTeamType loadOfficeTeamType(CompileContext compileContext);

}
