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
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.section.OfficeSectionManagedObjectSourceType;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectSource;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectExecutionStrategy;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectFlow;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectTeam;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.section.SectionManagedObjectSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;

/**
 * Node representing a {@link ManagedObjectSource}.
 *
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceNode
		extends LinkPoolNode, LinkStartBeforeNode, LinkStartAfterNode, SectionManagedObjectSource,
		OfficeManagedObjectSource, OfficeSectionManagedObjectSource, OfficeFloorManagedObjectSource {

	/**
	 * {@link Node} type.
	 */
	String TYPE = "Managed Object Source";

	/**
	 * Initialises the {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceClassName Class name of the
	 *                                     {@link ManagedObjectSource}.
	 * @param managedObjectSource          Optional instantiated
	 *                                     {@link ManagedObjectSource}. May be
	 *                                     <code>null</code>.
	 */
	void initialise(String managedObjectSourceClassName, ManagedObjectSource<?, ?> managedObjectSource);

	/**
	 * Indicates if the {@link ManagedObjectSource} is from {@link SupplierSource}.
	 * 
	 * @return <code>true</code> if from {@link SupplierSource}.
	 */
	boolean isSupplied();

	/**
	 * Sources the {@link ManagedObjectSource}.
	 * 
	 * @param managedObjectSourceVisitor {@link ManagedObjectSourceVisitor}.
	 * @param compileContext             {@link CompileContext}.
	 * @return <code>true</code> if successfully sourced the
	 *         {@link ManagedObjectSource}. <code>false</code> if failed to source,
	 *         with issues reported to the {@link CompilerIssues}.
	 */
	boolean sourceManagedObjectSource(ManagedObjectSourceVisitor managedObjectSourceVisitor,
			CompileContext compileContext);

	/**
	 * Obtains the {@link AugmentedManagedObjectFlow}.
	 * 
	 * @param flowName Name of the {@link AugmentedManagedObjectFlow}.
	 * @return {@link AugmentedManagedObjectFlow}.
	 */
	AugmentedManagedObjectFlow getAugmentedManagedObjectFlow(String flowName);

	/**
	 * Obtains the {@link AugmentedManagedObjectTeam}.
	 * 
	 * @param teamName Name of the {@link AugmentedManagedObjectTeam}.
	 * @return {@link AugmentedManagedObjectTeam}.
	 */
	AugmentedManagedObjectTeam getAugmentedManagedObjectTeam(String teamName);

	/**
	 * Obtains the {@link AugmentedManagedObjectExecutionStrategy}.
	 * 
	 * @param executionStrategyName Name of the
	 *                              {@link AugmentedManagedObjectExecutionStrategy}.
	 * @return {@link AugmentedManagedObjectExecutionStrategy}.
	 */
	AugmentedManagedObjectExecutionStrategy getAugmentedManagedObjectExecutionStrategy(String executionStrategyName);

	/**
	 * Loads the {@link ManagedObjectType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link ManagedObjectType} or <code>null</code> if issue loading with
	 *         issue reported to the {@link CompilerIssues}.
	 */
	ManagedObjectType<?> loadManagedObjectType(CompileContext compileContext);

	/**
	 * Loads the {@link OfficeSectionManagedObjectSourceType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link OfficeSectionManagedObjectSourceType} or <code>null</code> if
	 *         issue loading with issue reported to the {@link CompilerIssues}.
	 */
	OfficeSectionManagedObjectSourceType loadOfficeSectionManagedObjectSourceType(CompileContext compileContext);

	/**
	 * Loads the {@link OfficeFloorManagedObjectSourceType}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @return {@link OfficeFloorManagedObjectSourceType} or <code>null</code> if
	 *         issue loading with issue reported to the {@link CompilerIssues}.
	 */
	OfficeFloorManagedObjectSourceType loadOfficeFloorManagedObjectSourceType(CompileContext compileContext);

	/**
	 * Obtains the {@link SectionNode} containing this
	 * {@link ManagedObjectSourceNode}.
	 * 
	 * @return {@link SectionNode} containing this {@link ManagedObjectSourceNode}.
	 *         May be <code>null</code> if not contained within an
	 *         {@link OfficeSection} (in other words included above the
	 *         {@link SectionNode} instances).
	 */
	SectionNode getSectionNode();

	/**
	 * Obtains the {@link OfficeNode} containing this
	 * {@link ManagedObjectSourceNode}.
	 * 
	 * @return {@link OfficeNode} containing this {@link ManagedObjectSourceNode}.
	 *         May be <code>null</code> if not contained within an {@link Office}
	 *         (in other words included above the {@link OfficeNode} instances).
	 */
	OfficeNode getOfficeNode();

	/**
	 * Obtains the {@link OfficeFloorNode} containing this
	 * {@link ManagedObjectSourceNode}.
	 * 
	 * @return {@link OfficeFloorNode} containing this
	 *         {@link ManagedObjectSourceNode}.
	 */
	OfficeFloorNode getOfficeFloorNode();

	/**
	 * Obtains the {@link OfficeNode} of the {@link ManagingOffice} for this
	 * {@link ManagedObjectSource}.
	 *
	 * @return {@link OfficeNode} of the {@link ManagingOffice} for this
	 *         {@link ManagedObjectSource} or <code>null</code> if can not obtain
	 *         it.
	 */
	OfficeNode getManagingOfficeNode();

	/**
	 * Links the {@link InputManagedObjectNode} for this
	 * {@link ManagedObjectSource}.
	 *
	 * @param inputManagedObject {@link InputManagedObjectNode}.
	 * @return <code>true</code> if linked.
	 */
	boolean linkInputManagedObjectNode(InputManagedObjectNode inputManagedObject);

	/**
	 * Obtains the {@link InputManagedObjectNode} for this
	 * {@link ManagedObjectSource}.
	 *
	 * @return {@link InputManagedObjectNode} for this {@link ManagedObjectSource}
	 *         or <code>null</code> if can not obtain it.
	 */
	InputManagedObjectNode getInputManagedObjectNode();

	/**
	 * Auto-wires the input dependencies for this {@link ManagedObjectSource}.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param office         {@link OfficeNode} requiring the auto-wiring.
	 * @param compileContext {@link CompileContext}.
	 */
	void autoWireInputDependencies(AutoWirer<LinkObjectNode> autoWirer, OfficeNode office,
			CompileContext compileContext);

	/**
	 * Auto-wires the function dependencies for this {@link ManagedObjectSource}.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param office         {@link OfficeNode} requiring the auto-wiring.
	 * @param compileContext {@link CompileContext}.
	 */
	void autoWireFunctionDependencies(AutoWirer<LinkObjectNode> autoWirer, OfficeNode office,
			CompileContext compileContext);

	/**
	 * Links an auto-wire start before {@link ManagedObject} object type.
	 * 
	 * @param managedObjectType {@link ManagedObject} object type.
	 * @return <code>true</code> if linked.
	 */
	boolean linkAutoWireStartBefore(String managedObjectType);

	/**
	 * Links an auto-wire start after {@link ManagedObject} object type.
	 * 
	 * @param managedObjectType {@link ManagedObject} object type.
	 * @return <code>true</code> if linked.
	 */
	boolean linkAutoWireStartAfter(String managedObjectType);

	/**
	 * Indicates if there is auto-wired start up ordering.
	 * 
	 * @return <code>true</code> if there is auto-wired start up ordering.
	 */
	boolean isAutoWireStartupOrdering();

	/**
	 * Auto-wires the start up ordering for this {@link ManagedObjectSource}.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param office         {@link OfficeNode} requiring the auto-wiring.
	 * @param compileContext {@link CompileContext}.
	 */
	void autoWireStartupOrdering(AutoWirer<ManagedObjectSourceNode> autoWirer, OfficeNode office,
			CompileContext compileContext);

	/**
	 * Auto-wires the {@link Team} instances for this {@link ManagedObjectSource}.
	 * 
	 * @param autoWirer      {@link AutoWirer}.
	 * @param compileContext {@link CompileContext}.
	 */
	void autoWireTeams(AutoWirer<LinkTeamNode> autoWirer, CompileContext compileContext);

	/**
	 * Auto-wires this {@link ManagedObjectSourceNode} to the {@link OfficeNode}.
	 * 
	 * @param officeNode {@link OfficeNode}.
	 * @param issues     {@link CompilerIssues}.
	 */
	void autoWireToOffice(OfficeNode officeNode, CompilerIssues issues);

	/**
	 * Creates the {@link ExecutionManagedFunction}.
	 * 
	 * @param flowType       {@link ManagedObjectFlowType}.
	 * @param compileContext {@link CompileContext}.
	 * @return {@link ExecutionManagedFunction}.
	 */
	ExecutionManagedFunction createExecutionManagedFunction(ManagedObjectFlowType<?> flowType,
			CompileContext compileContext);

	/**
	 * Builds {@link ManagedObjectSource} for this {@link ManagedObjectNode}.
	 *
	 * @param builder               {@link OfficeFloorBuilder}.
	 * @param managingOffice        {@link OfficeNode} of the {@link ManagingOffice}
	 *                              for this {@link ManagedObjectSource}.
	 * @param managingOfficeBuilder {@link OfficeBuilder} for the
	 *                              {@link ManagingOffice}.
	 * @param officeBindings        {@link OfficeBindings}.
	 * @param compileContext        {@link CompileContext}.
	 */
	void buildManagedObject(OfficeFloorBuilder builder, OfficeNode managingOffice, OfficeBuilder managingOfficeBuilder,
			OfficeBindings officeBindings, CompileContext compileContext);

	/**
	 * Obtains the name to build the {@link ManagedObjectSource}.
	 * 
	 * @return Name to build the {@link ManagedObjectSource}.
	 */
	String getManagedObjectSourceName();

	/**
	 * Builds the {@link SupplierThreadLocal} for the {@link InputManagedObjectNode}
	 * from this {@link ManagedObjectSourceNode}.
	 * 
	 * @param optionalThreadLocalReceiver {@link OptionalThreadLocalReceiver}.
	 */
	void buildSupplierThreadLocal(OptionalThreadLocalReceiver optionalThreadLocalReceiver);

}
