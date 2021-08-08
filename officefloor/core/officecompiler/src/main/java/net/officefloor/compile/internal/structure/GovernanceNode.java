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

import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.team.Team;

/**
 * {@link OfficeGovernance} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface GovernanceNode extends LinkTeamNode, OfficeGovernance {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Governance";

	/**
	 * Initialises the {@link GovernanceNode}.
	 * 
	 * @param governanceSourceClassName Class name of the {@link GovernanceSource}.
	 * @param governanceSource          Optional instantiated
	 *                                  {@link GovernanceSource} to use. May be
	 *                                  <code>null</code>.
	 */
	void initialise(String governanceSourceClassName, GovernanceSource<?, ?> governanceSource);

	/**
	 * Loads the {@link GovernanceType} for this {@link GovernanceNode}.
	 * 
	 * @param isLoadingType Indicates using to load type.
	 * @return {@link GovernanceType} for this {@link GovernanceNode} or
	 *         <code>null</code> if fails to load the {@link GovernanceType}.
	 */
	GovernanceType<?, ?> loadGovernanceType(boolean isLoadingType);

	/**
	 * Sources the {@link Governance}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced. Otherwise,
	 *         <code>false</code> with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceGovernance(CompileContext compileContext);

	/**
	 * Indicates whether to auto-wire {@link ManagedObjectExtensionNode} instances
	 * for {@link Governance}.
	 * 
	 * @return <code>true</code> to auto-wire.
	 */
	boolean isAutoWireGovernance();

	/**
	 * Auto wires the {@link ManagedObjectExtensionNode} for this
	 * {@link Governance}.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param compileContext {@link CompileContext}.
	 */
	void autoWireExtensions(AutoWirer<ManagedObjectExtensionNode> autoWirer, CompileContext compileContext);

	/**
	 * Auto wires the {@link Team} for this {@link Governance}.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param compileContext {@link CompileContext}.
	 */
	void autoWireTeam(AutoWirer<LinkTeamNode> autoWirer, CompileContext compileContext);

	/**
	 * Builds this {@link Governance} into the {@link OfficeBuilder}.
	 * 
	 * @param officeBuilder  {@link OfficeBuilder}.
	 * @param compileContext {@link CompileContext}.
	 */
	void buildGovernance(OfficeBuilder officeBuilder, CompileContext compileContext);

}
