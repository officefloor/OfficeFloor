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
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * {@link OfficeFloorTeam} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamNode extends LinkTeamNode, OfficeFloorTeam {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Team";

	/**
	 * Initialises the {@link TeamNode}.
	 * 
	 * @param teamSourceClassName Class name of the {@link TeamSource}.
	 * @param teamSource          Optional instantiated {@link TeamSource}. May be
	 *                            <code>null</code>.
	 */
	void initialise(String teamSourceClassName, TeamSource teamSource);

	/**
	 * Sources the {@link Team}.
	 * 
	 * @param teamVisitor    {@link TeamVisitor}.
	 * @param compileContext {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced the {@link Team}.
	 *         <code>false</code> if failed to source, with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceTeam(TeamVisitor teamVisitor, CompileContext compileContext);

	/**
	 * Loads the {@link TeamType} for the {@link TeamSource}.
	 * 
	 * @return {@link TeamType} or <code>null</code> with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	TeamType loadTeamType();

	/**
	 * Loads the {@link OfficeFloorTeamSourceType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link OfficeFloorTeamSourceType} or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues}.
	 */
	OfficeFloorTeamSourceType loadOfficeFloorTeamSourceType(CompileContext compileContext);

	/**
	 * Obtains the {@link TypeQualification} instances for the
	 * {@link OfficeFloorTeam}.
	 * 
	 * @return {@link TypeQualification} instances for the {@link OfficeFloorTeam}.
	 */
	TypeQualification[] getTypeQualifications();

	/**
	 * Builds the {@link Team} for this {@link TeamNode}.
	 * 
	 * @param builder        {@link OfficeFloorBuilder}.
	 * @param compileContext {@link CompileContext}.
	 */
	void buildTeam(OfficeFloorBuilder builder, CompileContext compileContext);

}
