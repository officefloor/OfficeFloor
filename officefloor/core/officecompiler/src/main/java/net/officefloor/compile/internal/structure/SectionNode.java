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

import java.util.Map;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.section.OfficeSectionType;
import net.officefloor.compile.section.OfficeSubSectionType;
import net.officefloor.compile.section.SectionType;
import net.officefloor.compile.spi.office.ExecutionExplorer;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionTransformerContext;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.SectionFunction;
import net.officefloor.compile.spi.section.SubSection;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.FunctionManager;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.team.Team;

/**
 * Node within the hierarchy of {@link OfficeSection} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface SectionNode extends Node, ManagedObjectRegistry, ManagedFunctionRegistry,
		OfficeSectionTransformerContext, SectionDesigner, SubSection, OfficeSection {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Section";

	/**
	 * Initialises this {@link SectionNode}.
	 * 
	 * @param sectionSourceClassName {@link SectionSource} class name.
	 * @param sectionSource          Optional instantiated {@link SectionSource}.
	 *                               May be <code>null</code>.
	 * @param sectionLocation        Location of the {@link OfficeSection}.
	 */
	void initialise(String sectionSourceClassName, SectionSource sectionSource, String sectionLocation);

	/**
	 * <p>
	 * Sources the section into this {@link SectionNode}.
	 * <p>
	 * This will only source the top level {@link OfficeSection}.
	 * 
	 * @param managedFunctionVisitor     {@link ManagedFunctionVisitor}.
	 * @param managedObjectSourceVisitor {@link ManagedObjectSourceVisitor}.
	 * @param compileContext             {@link CompileContext}.
	 * @param isLoadingType              Indicates if used for loading type.
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the {@link CompilerIssues}.
	 */
	boolean sourceSection(ManagedFunctionVisitor managedFunctionVisitor,
			ManagedObjectSourceVisitor managedObjectSourceVisitor, CompileContext compileContext,
			boolean isLoadingType);

	/**
	 * Sources this {@link SectionNode} and all its descendant {@link Node}
	 * instances recursively.
	 * 
	 * @param managedFunctionVisitor     {@link ManagedFunctionVisitor}.
	 * @param managedObjectSourceVisitor {@link ManagedObjectSourceVisitor}.
	 * @param compileContext             {@link CompileContext}.
	 * @param isLoadingType              Indicates if used for loading type.
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the {@link CompilerIssues}.
	 */
	boolean sourceSectionTree(ManagedFunctionVisitor managedFunctionVisitor,
			ManagedObjectSourceVisitor managedObjectSourceVisitor, CompileContext compileContext,
			boolean isLoadingType);

	/**
	 * Sources the inheritance of the {@link SectionNode}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the {@link CompilerIssues}.
	 */
	boolean sourceInheritance(CompileContext compileContext);

	/**
	 * Obtains the parent {@link SectionNode} to this {@link SectionNode}.
	 * 
	 * @return Parent {@link SectionNode} to this {@link SectionNode}.
	 */
	SectionNode getSuperSection();

	/**
	 * Obtains the possible {@link SectionOutputNode}.
	 * 
	 * @param outputName Name of the {@link SectionOutputNode}.
	 * @return {@link SectionOutputNode} or <code>null</code> if no
	 *         {@link SectionOutputNode} by name on the {@link SectionNode}.
	 */
	SectionOutputNode getSectionOutputNode(String outputName);

	/**
	 * Loads the {@link SectionType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link SectionType} or <code>null</code> if issue loading with issue
	 *         reported to the {@link CompilerIssues}.
	 */
	SectionType loadSectionType(CompileContext compileContext);

	/**
	 * Loads the {@link OfficeSectionType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link OfficeSectionType} or <code>null</code> if issue loading with
	 *         issue reported to the {@link CompilerIssues}.
	 */
	OfficeSectionType loadOfficeSectionType(CompileContext compileContext);

	/**
	 * Loads the {@link OfficeSubSectionType}.
	 * 
	 * @param parentSectionType Parent {@link OfficeSubSectionType}.
	 * @param compileContext    {@link CompileContext}.
	 * @return {@link OfficeSubSectionType} or <code>null</code> if issue loading
	 *         with issue reported to the {@link CompilerIssues}.
	 */
	OfficeSubSectionType loadOfficeSubSectionType(OfficeSubSectionType parentSectionType,
			CompileContext compileContext);

	/**
	 * Loads the {@link OfficeAvailableSectionInputType} instances.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link OfficeAvailableSectionInputType} instances or
	 *         <code>null</code> with issues reported to the {@link CompilerIssues}.
	 */
	OfficeAvailableSectionInputType[] loadOfficeAvailableSectionInputTypes(CompileContext compileContext);

	/**
	 * Obtains the {@link DeployedOfficeInput}.
	 * 
	 * @param inputName Input name as per the
	 *                  {@link OfficeAvailableSectionInputType}.
	 * @return {@link DeployedOfficeInput}.
	 */
	DeployedOfficeInput getDeployedOfficeInput(String inputName);

	/**
	 * <p>
	 * Obtains the {@link GovernanceNode} instances providing {@link Governance}
	 * over this {@link SectionNode}.
	 * <p>
	 * This is list comprised of the:
	 * <ol>
	 * <li>{@link GovernanceNode} assigned to this particular
	 * {@link SectionNode}</li>
	 * <li>{@link GovernanceNode} assigned to any parent {@link SectionNode}</li>
	 * </ol>
	 * 
	 * @return {@link GovernanceNode} instances providing {@link Governance} over
	 *         this {@link SectionNode}.
	 */
	GovernanceNode[] getGoverningGovernances();

	/**
	 * Obtains the parent {@link SectionNode} containing this {@link SectionNode}.
	 * 
	 * @return Parent {@link SectionNode} or <code>null</code> if this
	 *         {@link SectionNode} is the top level {@link SectionNode} (in other
	 *         words a {@link OfficeSection}).
	 */
	SectionNode getParentSectionNode();

	/**
	 * Obtains the {@link OfficeNode} containing this {@link SectionNode}.
	 * 
	 * @return {@link OfficeNode} containing this {@link SectionNode}.
	 */
	OfficeNode getOfficeNode();

	/**
	 * Obtains the {@link SectionNode} qualified name within the {@link OfficeNode}.
	 * 
	 * @param simpleName Simple name to qualify with the {@link SectionNode} name
	 *                   space.
	 * @return {@link SectionNode} qualified name.
	 */
	String getSectionQualifiedName(String simpleName);

	/**
	 * Loads the {@link AutoWire} extension targets for the
	 * {@link OfficeSectionManagedObject} extension targets..
	 * 
	 * @param autoWirer      {@link AutoWirer} to be loaded with the
	 *                       {@link OfficeSectionManagedObject} extension targets.
	 * @param compileContext {@link CompileContext}.
	 */
	void loadAutoWireExtensionTargets(AutoWirer<ManagedObjectExtensionNode> autoWirer, CompileContext compileContext);

	/**
	 * Auto-wires the {@link SectionObjectNode} instances that are unlinked.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param compileContext {@link CompileContext}.
	 */
	void autoWireObjects(AutoWirer<LinkObjectNode> autoWirer, CompileContext compileContext);

	/**
	 * Auto-wires the {@link SectionFunction} instances to a possible responsible
	 * {@link Team}.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param compileContext {@link CompileContext}.
	 */
	void autoWireTeams(AutoWirer<LinkTeamNode> autoWirer, CompileContext compileContext);

	/**
	 * Loads the {@link ManagedFunctionNode} instances.
	 * 
	 * @param managedFunctionNodes {@link Map} to be loaded with the
	 *                             {@link ManagedFunctionNode} instances by their
	 *                             qualified name.
	 */
	void loadManagedFunctionNodes(Map<String, ManagedFunctionNode> managedFunctionNodes);

	/**
	 * Runs the {@link ExecutionExplorer} instances.
	 * 
	 * @param managedFunctions {@link ManagedFunctionNode} instances by their
	 *                         qualified name.
	 * @param compileContext   {@link CompileContext}.
	 * @return <code>true</code> if successfully explored execution.
	 */
	boolean runExecutionExplorers(Map<String, ManagedFunctionNode> managedFunctions, CompileContext compileContext);

	/**
	 * Builds this {@link OfficeSection} for this {@link SectionNode}.
	 * 
	 * @param officeBuilder  {@link OfficeBuilder} of the {@link Office} containing
	 *                       this {@link SectionNode}.
	 * @param officeBindings {@link OfficeBindings} of the {@link Office} containing
	 *                       this {@link SectionNode}.
	 * @param compileContext {@link CompileContext}.
	 */
	void buildSection(OfficeBuilder officeBuilder, OfficeBindings officeBindings, CompileContext compileContext);

	/**
	 * Loads the {@link FunctionManager} instances to externally trigger this
	 * {@link SectionNode}.
	 * 
	 * @param office {@link Office} containing this {@link SectionNode}.
	 * @throws UnknownFunctionException {@link UnknownFunctionException}.
	 */
	void loadExternalServicing(Office office) throws UnknownFunctionException;

}
