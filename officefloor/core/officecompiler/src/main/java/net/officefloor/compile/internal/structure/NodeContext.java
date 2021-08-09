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

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.administration.AdministrationLoader;
import net.officefloor.compile.executive.ExecutiveLoader;
import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.ManagedFunctionLoader;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.office.OfficeLoader;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.pool.ManagedObjectPoolLoader;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.SectionLoader;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.supplier.SupplierLoader;
import net.officefloor.compile.team.TeamLoader;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.executive.ExecutionStrategy;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.pool.ManagedObjectPool;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.source.TeamSource;

/**
 * Context for a {@link Node}.
 * 
 * @author Daniel Sagenschneider
 */
public interface NodeContext {

	/**
	 * Obtains the root {@link SourceContext}.
	 * 
	 * @return Root {@link SourceContext}.
	 */
	SourceContext getRootSourceContext();

	/**
	 * Obtains the {@link CompilerIssues}.
	 * 
	 * @return {@link CompilerIssues}.
	 */
	CompilerIssues getCompilerIssues();

	/**
	 * Obtains the {@link OfficeFrame}.
	 * 
	 * @return {@link OfficeFrame}.
	 */
	OfficeFrame getOfficeFrame();

	/**
	 * Initiates the {@link OfficeFloorBuilder} with the {@link OfficeFloorCompiler}
	 * details.
	 * 
	 * @param builder {@link OfficeFloorBuilder}.
	 */
	void initiateOfficeFloorBuilder(OfficeFloorBuilder builder);

	/**
	 * Creates a new {@link PropertyList}.
	 * 
	 * @return New {@link PropertyList}.
	 */
	PropertyList createPropertyList();

	/**
	 * Creates the {@link CompileContext}.
	 * 
	 * @return {@link CompileContext}.
	 */
	CompileContext createCompileContext();

	/**
	 * Obtains the additional profiles.
	 * 
	 * @param officeNode {@link OfficeNode} providing additional profiles. May be
	 *                   <code>null</code>.
	 * @return Additional profiles.
	 */
	String[] additionalProfiles(OfficeNode officeNode);

	/**
	 * Overrides the {@link PropertyList}.
	 * 
	 * @param node               {@link Node} requiring the overridden
	 *                           {@link PropertyList}.
	 * @param qualifiedName      Qualified name.
	 * @param originalProperties Original {@link PropertyList}.
	 * @return Overridden {@link PropertyList}.
	 */
	PropertyList overrideProperties(Node node, String qualifiedName, PropertyList originalProperties);

	/**
	 * Overrides the {@link PropertyList}.
	 * 
	 * @param node               {@link Node} requiring the overridden
	 *                           {@link PropertyList}.
	 * @param qualifiedName      Qualified name.
	 * @param overrideProperties {@link OverrideProperties}. May be
	 *                           <code>null</code>.
	 * @param originalProperties Original {@link PropertyList}.
	 * @return Overridden {@link PropertyList}.
	 */
	PropertyList overrideProperties(Node node, String qualifiedName, OverrideProperties overrideProperties,
			PropertyList originalProperties);

	/**
	 * Creates a new {@link AutoWirer}.
	 * 
	 * @param <N>       Type of {@link Node}.
	 * @param nodeType  {@link Class} type of {@link Node}.
	 * @param direction {@link AutoWireDirection}.
	 * @return New {@link AutoWirer}.
	 */
	<N extends Node> AutoWirer<N> createAutoWirer(Class<N> nodeType, AutoWireDirection direction);

	/**
	 * Obtains the {@link OfficeFloorSource} class.
	 * 
	 * @param <S>                        {@link OfficeFloorSource} type.
	 * @param officeFloorSourceClassName {@link Class} name of the
	 *                                   {@link OfficeFloorSource}.
	 * @param node                       {@link Node} requiring the
	 *                                   {@link OfficeFloorSource} class.
	 * @return {@link OfficeFloorSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this {@link NodeContext}.
	 */
	<S extends OfficeFloorSource> Class<S> getOfficeFloorSourceClass(String officeFloorSourceClassName,
			OfficeFloorNode node);

	/**
	 * Obtains the {@link OfficeFloorLoader}.
	 * 
	 * @param node {@link Node} requiring the {@link OfficeFloorLoader}.
	 * @return {@link OfficeFloorLoader}.
	 */
	OfficeFloorLoader getOfficeFloorLoader(OfficeFloorNode node);

	/**
	 * Creates the {@link OfficeFloorNode}.
	 * 
	 * @param officeFloorSourceClassName {@link Class} name of the
	 *                                   {@link OfficeFloorSource}.
	 * @param officeFloorSource          Optional instantiated
	 *                                   {@link OfficeFloorSource}. May be
	 *                                   <code>null</code>.
	 * @param officeFloorLocation        Location of the {@link OfficeFloor}.
	 * @return {@link OfficeFloorNode}.
	 */
	OfficeFloorNode createOfficeFloorNode(String officeFloorSourceClassName, OfficeFloorSource officeFloorSource,
			String officeFloorLocation);

	/**
	 * Obtains the {@link OfficeSource} class.
	 * 
	 * @param <S>                   {@link OfficeSource} type.
	 * @param officeSourceClassName {@link OfficeSource} class name or an alias to
	 *                              an {@link OfficeSource} class.
	 * @param node                  {@link Node} requiring the {@link OfficeSource}
	 *                              class.
	 * @return {@link OfficeSource} class, or <code>null</code> with issues reported
	 *         to the {@link CompilerIssues} of this {@link NodeContext}.
	 */
	<S extends OfficeSource> Class<S> getOfficeSourceClass(String officeSourceClassName, OfficeNode node);

	/**
	 * Obtains the {@link OfficeLoader}.
	 * 
	 * @param node {@link Node} requiring the {@link OfficeLoader}.
	 * @return {@link OfficeLoader}.
	 */
	OfficeLoader getOfficeLoader(OfficeNode node);

	/**
	 * Creates the {@link OfficeInputNode}.
	 * 
	 * @param officeInputName Name of the {@link OfficeInputNode}.
	 * @param office          Parent {@link OfficeNode}.
	 * @return {@link OfficeInputNode}.
	 */
	OfficeInputNode createOfficeInputNode(String officeInputName, OfficeNode office);

	/**
	 * Creates the {@link OfficeNode}.
	 * 
	 * @param officeName  Name of the {@link OfficeNode}.
	 * @param officeFloor Parent {@link OfficeFloorNode}.
	 * @return {@link OfficeNode}.
	 */
	OfficeNode createOfficeNode(String officeName, OfficeFloorNode officeFloor);

	/**
	 * Creates the {@link OfficeObjectNode}.
	 * 
	 * @param objectName Name of the {@link OfficeObjectNode}.
	 * @param office     Parent {@link OfficeNode}.
	 * @return {@link OfficeObjectNode}.
	 */
	OfficeObjectNode createOfficeObjectNode(String objectName, OfficeNode office);

	/**
	 * Creates the {@link OfficeOutputNode}.
	 * 
	 * @param name   Name of the {@link OfficeOutputNode}.
	 * @param office Parent {@link OfficeNode}.
	 * @return {@link OfficeOutputNode}.
	 */
	OfficeOutputNode createOfficeOutputNode(String name, OfficeNode office);

	/**
	 * Creates the {@link OfficeStartNode}.
	 * 
	 * @param startName Name of the {@link OfficeStartNode}.
	 * @param office    Parent {@link OfficeNode}.
	 * @return {@link OfficeStartNode}.
	 */
	OfficeStartNode createOfficeStartNode(String startName, OfficeNode office);

	/**
	 * Creates the {@link OfficeTeamNode}.
	 * 
	 * @param officeTeamName Name of the {@link OfficeTeamNode}.
	 * @param office         Parent {@link OfficeNode}.
	 * @return {@link OfficeTeamNode}.
	 */
	OfficeTeamNode createOfficeTeamNode(String officeTeamName, OfficeNode office);

	/**
	 * Obtains the {@link SectionSource} class.
	 * 
	 * @param <S>                    {@link SectionSource} type.
	 * @param sectionSourceClassName {@link SectionSource} class name or an alias to
	 *                               an {@link SectionSource} class.
	 * @param node                   {@link Node} requiring the
	 *                               {@link SectionSource} class.
	 * @return {@link SectionSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this {@link NodeContext}.
	 */
	<S extends SectionSource> Class<S> getSectionSourceClass(String sectionSourceClassName, SectionNode node);

	/**
	 * Obtains the {@link SectionLoader}.
	 * 
	 * @param sectionNode {@link SectionNode} requiring the {@link SectionLoader}.
	 * @return {@link SectionLoader}.
	 */
	SectionLoader getSectionLoader(SectionNode sectionNode);

	/**
	 * Obtains the {@link SectionLoader}.
	 * 
	 * @param officeNode {@link OfficeNode} requiring the {@link SectionLoader}.
	 * @return {@link SectionLoader}.
	 */
	SectionLoader getSectionLoader(OfficeNode officeNode);

	/**
	 * Creates the {@link SectionInputNode}.
	 * 
	 * @param inputName Name of the {@link SectionInputNode}.
	 * @param section   Parent {@link SectionNode}.
	 * @return {@link SectionInputNode}.
	 */
	SectionInputNode createSectionInputNode(String inputName, SectionNode section);

	/**
	 * Creates the {@link SectionObjectNode}.
	 * 
	 * @param objectName Name of the {@link SectionObjectNode}.
	 * @param section    Parent {@link SectionNode}.
	 * @return {@link SectionObjectNode}.
	 */
	SectionObjectNode createSectionObjectNode(String objectName, SectionNode section);

	/**
	 * Creates the {@link SectionOutputNode}.
	 * 
	 * @param outputName Name of the {@link SectionOutputNode}.
	 * @param section    Parent {@link SectionNode}.
	 * @return {@link SectionOutputNode}.
	 */
	SectionOutputNode createSectionOutputNode(String outputName, SectionNode section);

	/**
	 * Creates a top level {@link SectionNode} within the {@link OfficeNode}.
	 * 
	 * @param sectionName Name of the {@link OfficeSection}.
	 * @param office      {@link OfficeNode} containing this {@link OfficeSection}.
	 * @return {@link SectionNode}.
	 */
	SectionNode createSectionNode(String sectionName, OfficeNode office);

	/**
	 * Creates a {@link SectionNode}.
	 * 
	 * @param sectionName   Name of the {@link OfficeSection}.
	 * @param parentSection Parent {@link SectionNode} containing this
	 *                      {@link OfficeSection}.
	 * @return {@link SectionNode}.
	 */
	SectionNode createSectionNode(String sectionName, SectionNode parentSection);

	/**
	 * Obtains the {@link ManagedFunctionSource} class.
	 * 
	 * @param <S>                       {@link ManagedFunctionSource} type.
	 * @param managedFunctionSourceName {@link ManagedFunctionSource} class name or
	 *                                  an alias to a {@link ManagedFunctionSource}
	 *                                  class.
	 * @param node                      {@link Node} requiring the
	 *                                  {@link ManagedFunctionSource} class.
	 * @return {@link ManagedFunctionSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this {@link NodeContext}.
	 */
	<S extends ManagedFunctionSource> Class<S> getManagedFunctionSourceClass(String managedFunctionSourceName,
			FunctionNamespaceNode node);

	/**
	 * Obtains the {@link ManagedFunctionLoader}.
	 * 
	 * @param node          {@link Node} requiring the
	 *                      {@link ManagedFunctionLoader}.
	 * @param isLoadingType Indicates using to load type.
	 * @return {@link ManagedFunctionLoader}.
	 */
	ManagedFunctionLoader getManagedFunctionLoader(FunctionNamespaceNode node, boolean isLoadingType);

	/**
	 * Creates the {@link FunctionNamespaceNode}.
	 * 
	 * @param functionNamespaceName Name of the {@link FunctionNamespaceNode}.
	 * @param section               Parent {@link SectionNode}.
	 * @return {@link FunctionNamespaceNode}.
	 */
	FunctionNamespaceNode createFunctionNamespaceNode(String functionNamespaceName, SectionNode section);

	/**
	 * Creates the {@link FunctionFlowNode}.
	 * 
	 * @param flowName     Name of the {@link FunctionFlowNode}.
	 * @param isEscalation Indicates if is {@link Escalation}.
	 * @param function     Parent {@link ManagedFunctionNode}.
	 * @return {@link FunctionFlowNode}.
	 */
	FunctionFlowNode createFunctionFlowNode(String flowName, boolean isEscalation, ManagedFunctionNode function);

	/**
	 * Creates the {@link ManagedFunctionNode}.
	 * 
	 * @param functionName Name of the {@link ManagedFunctionNode}.
	 * @param section      Parent {@link SectionNode}.
	 * @return {@link ManagedFunctionNode}.
	 */
	ManagedFunctionNode createFunctionNode(String functionName, SectionNode section);

	/**
	 * Creates the {@link FunctionObjectNode}.
	 * 
	 * @param objectName   Name of the {@link FunctionObjectNode}.
	 * @param functionNode Parent {@link ManagedFunctionNode}.
	 * @return {@link FunctionObjectNode}.
	 */
	FunctionObjectNode createFunctionObjectNode(String objectName, ManagedFunctionNode functionNode);

	/**
	 * Creates the {@link ResponsibleTeamNode}.
	 * 
	 * @param teamName Name of the {@link ResponsibleTeamNode}.
	 * @param function Parent {@link ManagedFunctionNode}.
	 * @return {@link ResponsibleTeamNode}.
	 */
	ResponsibleTeamNode createResponsibleTeamNode(String teamName, ManagedFunctionNode function);

	/**
	 * Obtains the {@link ManagedObjectSource} class.
	 * 
	 * @param <S>                     {@link ManagedObjectSource} type.
	 * @param managedObjectSourceName {@link ManagedObjectSource} class name or an
	 *                                alias to a {@link ManagedObjectSource} class.
	 * @param node                    {@link Node} for reporting issues.
	 * @return {@link ManagedObjectSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this {@link NodeContext}.
	 */
	<S extends ManagedObjectSource<?, ?>> Class<S> getManagedObjectSourceClass(String managedObjectSourceName,
			ManagedObjectSourceNode node);

	/**
	 * Obtains the {@link ManagedObjectLoader}.
	 * 
	 * @param node {@link Node} requiring the {@link ManagedObjectLoader}.
	 * @return {@link ManagedObjectLoader}.
	 */
	ManagedObjectLoader getManagedObjectLoader(ManagedObjectSourceNode node);

	/**
	 * Creates an {@link InputManagedObjectNode}.
	 * 
	 * @param inputManagedObjectName Name of the {@link InputManagedObjectNode}.
	 * @param inputObjectType        Input object type.
	 * @param officeFloor            Parent {@link OfficeFloorNode}.
	 * @return {@link InputManagedObjectNode}.
	 */
	InputManagedObjectNode createInputManagedNode(String inputManagedObjectName, String inputObjectType,
			OfficeFloorNode officeFloor);

	/**
	 * Creates a {@link ManagedObjectDependencyNode} for a
	 * {@link ManagedObjectNode}.
	 * 
	 * @param dependencyName Name of the {@link ManagedObjectDependencyNode}.
	 * @param managedObject  Parent {@link ManagedObjectNode}.
	 * @return {@link ManagedObjectDependencyNode}.
	 */
	ManagedObjectDependencyNode createManagedObjectDependencyNode(String dependencyName,
			ManagedObjectNode managedObject);

	/**
	 * Creates a {@link ManagedObjectDependencyNode} for a
	 * {@link InputManagedObjectNode}.
	 * 
	 * @param dependencyName      Name of the {@link ManagedObjectDependencyNode}.
	 * @param managedObjectSource Parent {@link ManagedObjectSourceNode}.
	 * @return {@link ManagedObjectDependencyNode}.
	 */
	ManagedObjectDependencyNode createManagedObjectDependencyNode(String dependencyName,
			ManagedObjectSourceNode managedObjectSource);

	/**
	 * Creates a {@link ManagedObjectFunctionDependencyNode} for a
	 * {@link ManagedObjectSourceNode}.
	 * 
	 * @param dependencyName      Name of the
	 *                            {@link ManagedObjectFunctionDependencyNode}.
	 * @param managedObjectSource Parent {@link ManagedObjectSourceNode}.
	 * @return {@link ManagedObjectFunctionDependencyNode}.
	 */
	ManagedObjectFunctionDependencyNode createManagedObjectFunctionDependencyNode(String dependencyName,
			ManagedObjectSourceNode managedObjectSource);

	/**
	 * Creates the {@link ManagedObjectFlowNode}.
	 * 
	 * @param flowName            Name of the {@link ManagedObjectFlowNode}.
	 * @param managedObjectSource Parent {@link ManagedObjectSourceNode}.
	 * @return {@link ManagedObjectFlowNode}.
	 */
	ManagedObjectFlowNode createManagedObjectFlowNode(String flowName, ManagedObjectSourceNode managedObjectSource);

	/**
	 * Creates the {@link ManagedObjectTeamNode}.
	 * 
	 * @param teamName            Name of the {@link ManagedObjectTeamNode}.
	 * @param managedObjectSource Parent {@link ManagedObjectSourceNode}.
	 * @return {@link ManagedObjectTeamNode}.
	 */
	ManagedObjectTeamNode createManagedObjectTeamNode(String teamName, ManagedObjectSourceNode managedObjectSource);

	/**
	 * Creates the {@link ManagedObjectExecutionStrategyNode}.
	 * 
	 * @param executionStrategyName Name of the
	 *                              {@link ManagedObjectExecutionStrategyNode}.
	 * @param managedObjectSource   Parent {@link ManagedObjectSourceNode}.
	 * @return {@link ManagedObjectExecutionStrategyNode}.
	 */
	ManagedObjectExecutionStrategyNode createManagedObjectExecutionStrategyNode(String executionStrategyName,
			ManagedObjectSourceNode managedObjectSource);

	/**
	 * Creates the {@link ManagingOfficeNode}.
	 * 
	 * @param managedObjectSource Parent {@link ManagedObjectSourceNode}.
	 * @return {@link ManagingOfficeNode}.
	 */
	ManagingOfficeNode createManagingOfficeNode(ManagedObjectSourceNode managedObjectSource);

	/**
	 * Creates the {@link ManagedObjectNode}.
	 * 
	 * @param managedObjectName Name of the {@link ManagedObjectNode}.
	 * @param section           Parent {@link SectionNode}.
	 * @return {@link ManagedObjectNode}.
	 */
	ManagedObjectNode createManagedObjectNode(String managedObjectName, SectionNode section);

	/**
	 * Creates the {@link ManagedObjectNode}.
	 * 
	 * @param managedObjectName Name of the {@link ManagedObjectNode}.
	 * @param office            Parent {@link OfficeNode}.
	 * @return {@link ManagedObjectNode}.
	 */
	ManagedObjectNode createManagedObjectNode(String managedObjectName, OfficeNode office);

	/**
	 * Creates the {@link ManagedObjectNode}.
	 * 
	 * @param managedObjectName Name of the {@link ManagedObjectNode}.
	 * @param officeFloor       Parent {@link OfficeFloorNode}.
	 * @return {@link ManagedObjectNode}.
	 */
	ManagedObjectNode createManagedObjectNode(String managedObjectName, OfficeFloorNode officeFloor);

	/**
	 * Creates a {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSourceNode}.
	 * @param section                 Parent {@link SectionNode}.
	 * @return {@link ManagedObjectSourceNode}.
	 */
	ManagedObjectSourceNode createManagedObjectSourceNode(String managedObjectSourceName, SectionNode section);

	/**
	 * Creates a {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSourceNode}.
	 * @param office                  Parent {@link OfficeNode}.
	 * @return {@link ManagedObjectSourceNode}.
	 */
	ManagedObjectSourceNode createManagedObjectSourceNode(String managedObjectSourceName, OfficeNode office);

	/**
	 * Creates a {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSourceNode}.
	 * @param suppliedManagedObject   Parent
	 *                                {@link SuppliedManagedObjectSourceNode}.
	 * @return {@link ManagedObjectSourceNode}.
	 */
	ManagedObjectSourceNode createManagedObjectSourceNode(String managedObjectSourceName,
			SuppliedManagedObjectSourceNode suppliedManagedObject);

	/**
	 * Creates a {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSourceNode}.
	 * @param officeFloor             Parent {@link OfficeFloorNode}.
	 * @return {@link ManagedObjectSourceNode}.
	 */
	ManagedObjectSourceNode createManagedObjectSourceNode(String managedObjectSourceName, OfficeFloorNode officeFloor);

	/**
	 * Obtains the {@link ManagedObjectPoolSource} class.
	 * 
	 * @param <S>                         {@link ManagedObjectPoolSource} type.
	 * @param managedObjectPoolSourceName {@link ManagedObjectPoolSource} class name
	 *                                    or an alias to a
	 *                                    {@link ManagedObjectPoolSource} class.
	 * @param node                        {@link Node} for reporting issues.
	 * @return {@link ManagedObjectPoolSource} class, or <code>null</code> with
	 *         issues reported to the {@link CompilerIssues} of this
	 *         {@link NodeContext}.
	 */
	<S extends ManagedObjectPoolSource> Class<S> getManagedObjectPoolSourceClass(String managedObjectPoolSourceName,
			ManagedObjectPoolNode node);

	/**
	 * Obtains the {@link ManagedObjectPoolLoader}.
	 * 
	 * @param node          {@link Node} requiring the
	 *                      {@link ManagedObjectPoolLoader}.
	 * @param officeNode    {@link OfficeNode} containing the
	 *                      {@link ManagedObjectPool}. May be <code>null</code> if
	 *                      not contained within an {@link OfficeNode}.
	 * @param isLoadingType Indicates whether using to load type.
	 * @return {@link ManagedObjectPoolLoader}.
	 */
	ManagedObjectPoolLoader getManagedObjectPoolLoader(ManagedObjectPoolNode node, OfficeNode officeNode,
			boolean isLoadingType);

	/**
	 * Creates the {@link ManagedObjectPoolNode}.
	 * 
	 * @param managedObjectPoolName Name of the {@link ManagedObjectPoolNode}.
	 * @param officeFloorNode       Parent {@link OfficeFloorNode}.
	 * @return {@link ManagedObjectPoolNode}.
	 */
	ManagedObjectPoolNode createManagedObjectPoolNode(String managedObjectPoolName, OfficeFloorNode officeFloorNode);

	/**
	 * Creates the {@link ManagedObjectPoolNode}.
	 * 
	 * @param managedObjectPoolName Name of the {@link ManagedObjectPoolNode}.
	 * @param officeNode            Parent {@link OfficeNode}.
	 * @return {@link ManagedObjectPoolNode}.
	 */
	ManagedObjectPoolNode createManagedObjectPoolNode(String managedObjectPoolName, OfficeNode officeNode);

	/**
	 * Creates the {@link ManagedObjectPoolNode}.
	 * 
	 * @param managedObjectPoolName Name of the {@link ManagedObjectPoolNode}.
	 * @param sectionNode           Parent {@link SectionNode}.
	 * @return {@link ManagedObjectPoolNode}.
	 */
	ManagedObjectPoolNode createManagedObjectPoolNode(String managedObjectPoolName, SectionNode sectionNode);

	/**
	 * Obtains the {@link SupplierSource} class.
	 * 
	 * @param <S>                     {@link SupplierSource} type.
	 * @param supplierSourceClassName {@link SupplierSource} class name or an alias
	 *                                to a {@link SupplierSource} class.
	 * @param node                    {@link Node} requiring the
	 *                                {@link SupplierSource} class.
	 * @return {@link SupplierSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this {@link NodeContext}.
	 */
	<S extends SupplierSource> Class<S> getSupplierSourceClass(String supplierSourceClassName, SupplierNode node);

	/**
	 * Obtains the {@link SupplierLoader}.
	 * 
	 * @param node          {@link Node} requiring the {@link SupplierLoader}.
	 * @param isLoadingType Indicates if using to load type.
	 * @return {@link SupplierLoader}.
	 */
	SupplierLoader getSupplierLoader(SupplierNode node, boolean isLoadingType);

	/**
	 * Creates the {@link SupplierThreadLocalNode}.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code> if no qualifier.
	 * @param type      Type.
	 * @param supplier  Parent {@link SupplierNode}.
	 * @return {@link SupplierThreadLocalNode}.
	 */
	SupplierThreadLocalNode createSupplierThreadLocalNode(String qualifier, String type, SupplierNode supplier);

	/**
	 * Creates the {@link SuppliedManagedObjectSourceNode}.
	 * 
	 * @param qualifier Qualifier. May be <code>null</code> if no qualifier.
	 * @param type      Type.
	 * @param supplier  Parent {@link SupplierNode}.
	 * @return {@link SuppliedManagedObjectSourceNode}.
	 */
	SuppliedManagedObjectSourceNode createSuppliedManagedObjectSourceNode(String qualifier, String type,
			SupplierNode supplier);

	/**
	 * Creates the {@link SupplierNode}.
	 * 
	 * @param supplierName Name of the {@link SupplierNode}.
	 * @param officeFloor  Parent {@link OfficeFloorNode}.
	 * @return {@link SupplierNode}.
	 */
	SupplierNode createSupplierNode(String supplierName, OfficeFloorNode officeFloor);

	/**
	 * Creates the {@link SupplierNode}.
	 * 
	 * @param supplierName Name of the {@link SupplierNode}.
	 * @param office       Parent {@link Office}.
	 * @return {@link SupplierNode}.
	 */
	SupplierNode createSupplierNode(String supplierName, OfficeNode office);

	/**
	 * Obtains the {@link AdministrationSource} class.
	 * 
	 * @param <S>                           {@link AdministrationSource} type.
	 * @param administrationSourceClassName {@link AdministrationSource} class name
	 *                                      or an alias to an
	 *                                      {@link AdministrationSource} class.
	 * @param node                          {@link Node} requiring the
	 *                                      {@link AdministrationSource} class.
	 * @return {@link AdministrationSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this {@link NodeContext}.
	 */
	<S extends AdministrationSource<?, ?, ?>> Class<S> getAdministrationSourceClass(
			String administrationSourceClassName, AdministrationNode node);

	/**
	 * Obtains the {@link AdministrationLoader}.
	 * 
	 * @param node          {@link Node} requiring the {@link AdministrationLoader}.
	 * @param isLoadingType Indicates if using to load type.
	 * @return {@link AdministrationLoader}.
	 */
	AdministrationLoader getAdministrationLoader(AdministrationNode node, boolean isLoadingType);

	/**
	 * Creates a {@link AdministrationNode}.
	 * 
	 * @param administratorName Name of the {@link Administration}.
	 * @param office            {@link OfficeNode} containing this
	 *                          {@link Administration}.
	 * @return {@link AdministrationNode}.
	 */
	AdministrationNode createAdministrationNode(String administratorName, OfficeNode office);

	/**
	 * Obtains the {@link GovernanceSource} class.
	 * 
	 * @param <S>                  {@link GovernanceSource} type.
	 * @param governanceSourceName {@link GovernanceSource} class name or an alias
	 *                             to an {@link GovernanceSource} class.
	 * @param node                 {@link Node} requiring the
	 *                             {@link GovernanceSource} class.
	 * @return {@link GovernanceSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this {@link NodeContext}.
	 */
	<S extends GovernanceSource<?, ?>> Class<S> getGovernanceSourceClass(String governanceSourceName,
			GovernanceNode node);

	/**
	 * Obtains the {@link GovernanceLoader}.
	 * 
	 * @param node          {@link Node} requiring the {@link GovernanceLoader}.
	 * @param isLoadingType Indicates using for loading type.
	 * @return {@link GovernanceLoader}.
	 */
	GovernanceLoader getGovernanceLoader(GovernanceNode node, boolean isLoadingType);

	/**
	 * Creates a {@link GovernanceNode}.
	 * 
	 * @param governanceName Name of the {@link Governance}.
	 * @param office         {@link OfficeNode} containing this {@link Governance}.
	 * @return {@link GovernanceNode}.
	 */
	GovernanceNode createGovernanceNode(String governanceName, OfficeNode office);

	/**
	 * Obtains the {@link TeamSource} class.
	 * 
	 * @param <S>                 {@link TeamSource} type.
	 * @param teamSourceClassName {@link TeamSource} class name or an alias to a
	 *                            {@link TeamSource} class.
	 * @param node                {@link Node} requiring the {@link TeamSource}
	 *                            class.
	 * @return {@link TeamSource} class, or <code>null</code> with issues reported
	 *         to the {@link CompilerIssues} of this {@link NodeContext}.
	 */
	<S extends TeamSource> Class<S> getTeamSourceClass(String teamSourceClassName, TeamNode node);

	/**
	 * Obtains the {@link TeamLoader}.
	 * 
	 * @param node {@link Node} requiring the {@link TeamLoader}.
	 * @return {@link TeamLoader}.
	 */
	TeamLoader getTeamLoader(TeamNode node);

	/**
	 * Creates the {@link TeamNode}.
	 * 
	 * @param teamName    Name of the {@link TeamNode}.
	 * @param officeFloor Parent {@link OfficeFloorNode}.
	 * @return {@link TeamNode}.
	 */
	TeamNode createTeamNode(String teamName, OfficeFloorNode officeFloor);

	/**
	 * Obtains the {@link ExecutiveSource} class.
	 * 
	 * @param <S>                      {@link ExecutiveSource} type.
	 * @param executiveSourceClassName {@link ExecutiveSource} class name or an
	 *                                 alias to a {@link ExecutiveSource} class.
	 * @param node                     {@link Node} requiring the
	 *                                 {@link ExecutiveSource} class.
	 * @return {@link ExecutiveSource} class, or <code>null</code> with issues
	 *         reported to the {@link CompilerIssues} of this {@link NodeContext}.
	 */
	<S extends ExecutiveSource> Class<S> getExecutiveSourceClass(String executiveSourceClassName, ExecutiveNode node);

	/**
	 * Obtains the {@link ExecutiveNode}.
	 * 
	 * @param node {@link Node} requiring the {@link ExecutiveLoader}.
	 * @return {@link ExecutiveLoader}.
	 */
	ExecutiveLoader getExecutiveLoader(ExecutiveNode node);

	/**
	 * Creates the {@link ExecutiveNode}.
	 * 
	 * @param officeFloor Parent {@link OfficeFloorNode}.
	 * @return {@link ExecutiveNode}.
	 */
	ExecutiveNode createExecutiveNode(OfficeFloorNode officeFloor);

	/**
	 * Creates the {@link ExecutionStrategyNode}.
	 * 
	 * @param executionStrategyName Name of the {@link ExecutionStrategy}.
	 * @param executive             Parent {@link ExecutiveNode}.
	 * @return {@link ExecutionStrategyNode}.
	 */
	ExecutionStrategyNode createExecutionStrategyNode(String executionStrategyName, ExecutiveNode executive);

	/**
	 * Creates an {@link EscalationNode}.
	 * 
	 * @param escalationType {@link Escalation} type.
	 * @param officeNode     {@link OfficeNode} containing this {@link Escalation}.
	 * @return {@link EscalationNode}.
	 */
	EscalationNode createEscalationNode(String escalationType, OfficeNode officeNode);

}
