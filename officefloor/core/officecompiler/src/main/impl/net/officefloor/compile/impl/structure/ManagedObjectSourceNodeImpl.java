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

package net.officefloor.compile.impl.structure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import net.officefloor.compile.impl.section.OfficeSectionManagedObjectSourceTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.AdministrationNode;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireLink;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.ExecutionStrategyNode;
import net.officefloor.compile.internal.structure.FunctionNamespaceNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkPoolNode;
import net.officefloor.compile.internal.structure.LinkStartAfterNode;
import net.officefloor.compile.internal.structure.LinkStartBeforeNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedFunctionNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectExecutionStrategyNode;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectFunctionDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectPoolNode;
import net.officefloor.compile.internal.structure.ManagedObjectRegistry;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceVisitor;
import net.officefloor.compile.internal.structure.ManagedObjectTeamNode;
import net.officefloor.compile.internal.structure.ManagingOfficeNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeBindings;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OptionalThreadLocalReceiver;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectExecutionStrategyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectFunctionDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionManagedObjectSourceType;
import net.officefloor.compile.section.OfficeSectionManagedObjectTeamType;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.managedobject.ManagedObjectExecutionStrategy;
import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectDependency;
import net.officefloor.compile.spi.office.OfficeManagedObjectFlow;
import net.officefloor.compile.spi.office.OfficeManagedObjectFunctionDependency;
import net.officefloor.compile.spi.office.OfficeManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObjectTeam;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectExecutionStrategy;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectFlow;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectTeam;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectDependency;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectExecutionStrategy;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectFlow;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectFunctionDependency;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectTeam;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionManagedObjectFlow;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.ThreadDependencyMappingBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectFunctionDependency;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link ManagedObjectSourceNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectSourceNodeImpl implements ManagedObjectSourceNode {

	/**
	 * Name of this {@link ManagedObjectSource}.
	 */
	private final String managedObjectSourceName;

	/**
	 * {@link PropertyList} to load the {@link ManagedObjectSource}.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link ManagingOffice}.
	 */
	private final ManagingOfficeNode managingOffice;

	/**
	 * {@link ManagedObjectRegistry}.
	 */
	private final ManagedObjectRegistry managedObjectRegistry;

	/**
	 * Timeout for the {@link ManagedObject}.
	 */
	private long timeout = 0;

	/**
	 * {@link InitialisedState}.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {

		/**
		 * Class name of the {@link ManagedObjectSource}.
		 */
		private final String managedObjectSourceClassName;

		/**
		 * {@link ManagedObjectSource} instance to use. If this is specified its use
		 * overrides the {@link Class}. Will be <code>null</code> if not to override.
		 */
		@SuppressWarnings("rawtypes")
		private final ManagedObjectSource managedObjectSource;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectSourceClassName {@link ManagedObjectSource} {@link Class}
		 *                                     name.
		 * @param managedObjectSource          {@link ManagedObjectSource}.
		 */
		@SuppressWarnings("rawtypes")
		public InitialisedState(String managedObjectSourceClassName, ManagedObjectSource managedObjectSource) {
			this.managedObjectSourceClassName = managedObjectSourceClassName;
			this.managedObjectSource = managedObjectSource;
		}
	}

	/**
	 * Containing {@link SectionNode}. <code>null</code> if contained in the
	 * {@link Office} or {@link OfficeFloor}.
	 */
	private final SectionNode containingSectionNode;

	/**
	 * Containing {@link OfficeNode}. <code>null</code> if contained in the
	 * {@link OfficeFloor}.
	 */
	private final OfficeNode containingOfficeNode;

	/**
	 * {@link SuppliedManagedObjectSourceNode} should this be a supplied
	 * {@link ManagedObjectSource}. Will be <code>null</code> if not supplied.
	 */
	private final SuppliedManagedObjectSourceNode suppliedManagedObjectNode;

	/**
	 * Containing {@link OfficeFloorNode}.
	 */
	private final OfficeFloorNode containingOfficeFloorNode;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link InputManagedObjectNode}.
	 */
	private InputManagedObjectNode inputManagedObjectNode = null;

	/**
	 * {@link ManagedObjectFlowNode} instances by their {@link ManagedObjectFlow}
	 * names.
	 */
	private final Map<String, ManagedObjectFlowNode> flows = new HashMap<>();

	/**
	 * {@link ManagedObjectTeamNode} instances by their {@link ManagedObjectTeam}
	 * names.
	 */
	private final Map<String, ManagedObjectTeamNode> teams = new HashMap<>();

	/**
	 * {@link ManagedObjectExecutionStrategyNode} instances by their
	 * {@link ManagedObjectExecutionStrategy} names.
	 */
	private final Map<String, ManagedObjectExecutionStrategyNode> executionStrategies = new HashMap<>();

	/**
	 * {@link ManagedObjectDependencyNode} instances by their
	 * {@link ManagedObjectDependency} names. This is for the Input
	 * {@link ManagedObject} dependencies.
	 */
	private final Map<String, ManagedObjectDependencyNode> inputDependencies = new HashMap<>();

	/**
	 * {@link ManagedObjectFunctionDependencyNode} instances by their
	 * {@link ManagedObjectFunctionDependency} names.
	 */
	private final Map<String, ManagedObjectFunctionDependencyNode> functionDependencies = new HashMap<>();

	/**
	 * {@link OptionalThreadLocalLinker}.
	 */
	private final OptionalThreadLocalLinker optionalThreadLocalLinker = new OptionalThreadLocalLinker();

	/**
	 * Start before {@link ManagedObject} object type names.
	 */
	private final List<String> startBeforeManagedObjectTypes = new LinkedList<>();

	/**
	 * Start after {@link ManagedObject} object type names.
	 */
	private final List<String> startAfterManagedObjectTypes = new LinkedList<>();

	/**
	 * {@link AutoWirer} for the responsible {@link Team} instances.
	 */
	private AutoWirer<LinkTeamNode> teamAutoWirer = null;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceName             Name of this
	 *                                            {@link ManagedObjectSource}.
	 * @param containingSectionNode               {@link SectionNode} containing
	 *                                            this
	 *                                            {@link ManagedObjectSourceNode}.
	 *                                            <code>null</code> if contained in
	 *                                            the {@link Office} or
	 *                                            {@link OfficeFloor}.
	 * @param containingOfficeNode                {@link OfficeNode} containing this
	 *                                            {@link ManagedObjectSourceNode}.
	 *                                            <code>null</code> if contained in
	 *                                            the {@link OfficeFloor}.
	 * @param containingSuppliedManagedObjectNode {@link SuppliedManagedObjectSourceNode}
	 *                                            containing this
	 *                                            {@link ManagedObjectSource}.
	 *                                            <code>null</code> if not provided
	 *                                            from {@link SupplierSource}.
	 * @param containingOfficeFloorNode           {@link OfficeFloorNode} containing
	 *                                            this
	 *                                            {@link ManagedObjectSourceNode}.
	 * @param context                             {@link NodeContext}.
	 */
	public ManagedObjectSourceNodeImpl(String managedObjectSourceName, SectionNode containingSectionNode,
			OfficeNode containingOfficeNode, SuppliedManagedObjectSourceNode containingSuppliedManagedObjectNode,
			OfficeFloorNode containingOfficeFloorNode, NodeContext context) {
		this.managedObjectSourceName = managedObjectSourceName;
		this.containingSectionNode = containingSectionNode;
		this.containingOfficeNode = containingOfficeNode;
		this.suppliedManagedObjectNode = containingSuppliedManagedObjectNode;
		this.containingOfficeFloorNode = containingOfficeFloorNode;
		this.context = context;

		// Specify the registry (wrapping to keep track of managed objects)
		this.managedObjectRegistry = (this.containingSectionNode != null ? this.containingSectionNode
				: (this.containingOfficeNode != null ? this.containingOfficeNode : this.containingOfficeFloorNode));

		// Create the additional objects
		this.propertyList = this.context.createPropertyList();
		this.managingOffice = this.context.createManagingOfficeNode(this);
	}

	/**
	 * Specify type loading.
	 */
	private static interface TypeLoader<T> {

		/**
		 * Loads the specific type.
		 * 
		 * @param managedObjectSource {@link ManagedObjectSource}.
		 * @param properties          {@link PropertyList} to configure the
		 *                            {@link ManagedObjectSource}.
		 * @param managedObjectLoader {@link ManagedObjectLoader}.
		 * @return Specific type.
		 */
		T loadType(ManagedObjectSource<?, ?> managedObjectSource, PropertyList properties,
				ManagedObjectLoader managedObjectLoader);
	}

	/**
	 * Loads the type.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @param loader         {@link TypeLoader} to load specific type.
	 * @return Type or <code>null</code> if issues with issues reported to the
	 *         {@link CompilerIssues}.
	 */
	private <T> T loadType(CompileContext compileContext, TypeLoader<T> loader) {

		// Obtain the managed object source and properties
		ManagedObjectSource<?, ?> managedObjectSource = this.state.managedObjectSource;
		PropertyList seedProperties = null;
		if (this.suppliedManagedObjectNode != null) {
			// Obtain the supplied managed object source
			SuppliedManagedObjectSourceType suppliedManagedObjectSource = compileContext
					.getOrLoadSuppliedManagedObjectSourceType(this.suppliedManagedObjectNode);
			managedObjectSource = suppliedManagedObjectSource.getManagedObjectSource();
			seedProperties = suppliedManagedObjectSource.getPropertyList();

		} else if (managedObjectSource == null) {
			// Obtain the managed object source class
			Class<? extends ManagedObjectSource<?, ?>> managedObjectSourceClass = this.context
					.getManagedObjectSourceClass(this.state.managedObjectSourceClassName, this);
			if (managedObjectSourceClass == null) {
				return null; // must have managed object source class
			}

			// Obtain the managed object source
			managedObjectSource = CompileUtil.newInstance(managedObjectSourceClass, ManagedObjectSource.class, this,
					this.context.getCompilerIssues());
			if (managedObjectSource == null) {
				return null; // must have managed object source
			}
		}
		PropertyList properties = this.getPropertyList(seedProperties);

		// Load and return the type
		ManagedObjectLoader managedObjectLoader = this.context.getManagedObjectLoader(this);
		return loader.loadType(managedObjectSource, properties, managedObjectLoader);
	}

	/**
	 * Obtains the {@link PropertyList} for configuring this
	 * {@link ManagedObjectSourceNode}.
	 * 
	 * @param seedProperties Seed {@link PropertyList}. May be <code>null</code> if
	 *                       no seed {@link PropertyList}.
	 * @return {@link PropertyList} for configuring this
	 *         {@link ManagedObjectSourceNode}.
	 */
	private PropertyList getPropertyList(PropertyList seedProperties) {

		// Obtain the properties
		PropertyList properties = this.context.createPropertyList();

		// Load the seed properties
		if (seedProperties != null) {
			for (Property property : seedProperties) {
				properties.getOrAddProperty(property.getName()).setValue(property.getValue());
			}
		}

		// Allow to override with configured properties
		for (Property property : this.propertyList) {
			properties.getOrAddProperty(property.getName()).setValue(property.getValue());
		}

		// Return the overridden properties
		return this.context.overrideProperties(this, this.getManagedObjectSourceName(), this.containingOfficeNode,
				properties);
	}

	/**
	 * Obtains the {@link ManagedObjectDependencyNode}.
	 * 
	 * @param managedObjectDependencyName Name of the
	 *                                    {@link ManagedObjectDependencyNode}.
	 * @return {@link ManagedObjectDependencyNode}.
	 */
	private ManagedObjectDependencyNode getManagedObjectDependencyNode(String managedObjectDependencyName) {
		return NodeUtil.getNode(managedObjectDependencyName, this.inputDependencies,
				() -> this.context.createManagedObjectDependencyNode(managedObjectDependencyName, this));
	}

	/**
	 * Obtains the {@link ManagedObjectDependencyNode}.
	 * 
	 * @param managedObjectDependencyName Name of the
	 *                                    {@link ManagedObjectDependencyNode}.
	 * @return {@link ManagedObjectDependencyNode}.
	 */
	private ManagedObjectFunctionDependencyNode getManagedObjectFunctionDependencyNode(
			String managedObjectFunctionDependencyName) {
		return NodeUtil.getNode(managedObjectFunctionDependencyName, this.functionDependencies, () -> this.context
				.createManagedObjectFunctionDependencyNode(managedObjectFunctionDependencyName, this));
	}

	/**
	 * Obtains the {@link ManagedObjectFlowNode}.
	 * 
	 * @param managedObjectSourceFlowName Name of the {@link ManagedObjectFlowNode}.
	 * @return {@link ManagedObjectFlowNode}.
	 */
	private ManagedObjectFlowNode getManagedObjectFlowNode(String managedObjectSourceFlowName) {
		return NodeUtil.getNode(managedObjectSourceFlowName, this.flows,
				() -> this.context.createManagedObjectFlowNode(managedObjectSourceFlowName, this));
	}

	/**
	 * Obtains the {@link ManagedObjectTeamNode}.
	 * 
	 * @param managedObjectSourceTeamName Name of the {@link ManagedObjectTeamNode}.
	 * @return {@link ManagedObjectTeamNode}.
	 */
	private ManagedObjectTeamNode getManagedObjectTeamNode(String managedObjectSourceTeamName) {
		return NodeUtil.getNode(managedObjectSourceTeamName, this.teams,
				() -> this.context.createManagedObjectTeamNode(managedObjectSourceTeamName, this));
	}

	/**
	 * Obtains the {@link ManagedObjectExecutionStrategyNode}.
	 * 
	 * @param managedObjectSourceExecutionStrategyName Name of the
	 *                                                 {@link ManagedObjectExecutionStrategyNode}.
	 * @return {@link ManagedObjectExecutionStrategyNode}.
	 */
	private ManagedObjectExecutionStrategyNode getManagedObjectExecutionStrategyNode(
			String managedObjectSourceExecutionStrategyName) {
		return NodeUtil.getNode(managedObjectSourceExecutionStrategyName, this.executionStrategies, () -> this.context
				.createManagedObjectExecutionStrategyNode(managedObjectSourceExecutionStrategyName, this));
	}

	/*
	 * =========================== Node ===================================
	 */

	@Override
	public String getNodeName() {
		return this.managedObjectSourceName;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return null;
	}

	@Override
	public Node getParentNode() {
		return (this.containingSectionNode != null ? this.containingSectionNode
				: (this.containingOfficeNode != null ? this.containingOfficeNode : this.containingOfficeFloorNode));
	}

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes(this.flows, this.teams, this.inputDependencies, this.functionDependencies);
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);

	}

	@Override
	public void initialise(String managedObjectSourceClassName, ManagedObjectSource<?, ?> managedObjectSource) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(managedObjectSourceClassName, managedObjectSource));
	}

	/*
	 * ================ ManagedObjectSourceNode ===============================
	 */

	@Override
	public boolean isSupplied() {
		return this.suppliedManagedObjectNode != null;
	}

	@Override
	public SectionNode getSectionNode() {
		return this.containingSectionNode;
	}

	@Override
	public OfficeNode getOfficeNode() {
		return this.containingOfficeNode;
	}

	@Override
	public OfficeFloorNode getOfficeFloorNode() {
		return this.containingOfficeFloorNode;
	}

	@Override
	public boolean sourceManagedObjectSource(ManagedObjectSourceVisitor visitor, CompileContext compileContext) {

		// Load the managed object type
		ManagedObjectType<?> managedObjectType = compileContext.getOrLoadManagedObjectType(this);
		if (managedObjectType == null) {
			return false;
		}

		// Initialise the flows
		for (ManagedObjectFlowType<?> flowType : managedObjectType.getFlowTypes()) {
			String flowName = flowType.getFlowName();
			NodeUtil.getInitialisedNode(flowName, this.flows, this.context,
					() -> this.context.createManagedObjectFlowNode(flowName, this), (flow) -> flow.initialise());
		}

		// Initialise the teams
		for (ManagedObjectTeamType teamType : managedObjectType.getTeamTypes()) {
			String teamName = teamType.getTeamName();
			NodeUtil.getInitialisedNode(teamName, this.teams, this.context,
					() -> this.context.createManagedObjectTeamNode(teamName, this), (team) -> team.initialise());
		}

		// Initialise the execution strategies
		for (ManagedObjectExecutionStrategyType executionStrategyType : managedObjectType.getExecutionStrategyTypes()) {
			String executionStrategyName = executionStrategyType.getExecutionStrategyName();
			NodeUtil.getInitialisedNode(executionStrategyName, this.executionStrategies, this.context,
					() -> this.context.createManagedObjectExecutionStrategyNode(executionStrategyName, this),
					(executionStrategy) -> executionStrategy.initialise());
		}

		// Initialise the input dependencies (if input)
		if (managedObjectType.isInput()) {
			for (ManagedObjectDependencyType<?> dependencyType : managedObjectType.getDependencyTypes()) {
				String dependencyName = dependencyType.getDependencyName();
				NodeUtil.getInitialisedNode(dependencyName, this.inputDependencies, this.context,
						() -> this.context.createManagedObjectDependencyNode(dependencyName, this),
						(dependency) -> dependency.initialise());
			}
		}

		// Initialise the function dependencies
		for (ManagedObjectFunctionDependencyType functionDependencyType : managedObjectType
				.getFunctionDependencyTypes()) {
			String functionDependencyName = functionDependencyType.getFunctionObjectName();
			NodeUtil.getInitialisedNode(functionDependencyName, this.functionDependencies, this.context,
					() -> this.context.createManagedObjectFunctionDependencyNode(functionDependencyName, this),
					(dependency) -> dependency.initialise());
		}

		// Visit this managed object source
		if (visitor != null) {
			try {
				visitor.visit(managedObjectType, this, compileContext);
			} catch (CompileError error) {
				// Issue should already be provided
				return false;
			}
		}

		// Successfully sourced
		return true;
	}

	@Override
	public AugmentedManagedObjectFlow getAugmentedManagedObjectFlow(String flowName) {
		return this.getManagedObjectFlowNode(flowName);
	}

	@Override
	public AugmentedManagedObjectTeam getAugmentedManagedObjectTeam(String teamName) {
		return this.getManagedObjectTeamNode(teamName);
	}

	@Override
	public AugmentedManagedObjectExecutionStrategy getAugmentedManagedObjectExecutionStrategy(
			String executionStrategyName) {
		return this.getManagedObjectExecutionStrategyNode(executionStrategyName);
	}

	@Override
	public ManagedObjectType<?> loadManagedObjectType(CompileContext compileContext) {

		// Load and return the managed object type
		return this.loadType(compileContext,
				(mos, properties, loader) -> loader.loadManagedObjectType(mos, properties));
	}

	@Override
	public OfficeSectionManagedObjectSourceType loadOfficeSectionManagedObjectSourceType(
			CompileContext compileContext) {

		// Load the managed object type
		ManagedObjectType<?> managedObjectType = compileContext.getOrLoadManagedObjectType(this);
		if (managedObjectType == null) {
			return null;
		}

		// Load the teams types
		OfficeSectionManagedObjectTeamType[] teamTypes = CompileUtil.loadTypes(this.teams,
				(team) -> team.getManagedObjectTeamName(),
				(team) -> team.loadOfficeSectionManagedObjectTeamType(compileContext),
				OfficeSectionManagedObjectTeamType[]::new);
		if (teamTypes == null) {
			return null;
		}

		// Create and return the type
		return new OfficeSectionManagedObjectSourceTypeImpl(this.managedObjectSourceName, teamTypes);
	}

	@Override
	public OfficeFloorManagedObjectSourceType loadOfficeFloorManagedObjectSourceType(CompileContext compileContext) {

		// Ensure have the managed object source name
		if (CompileUtil.isBlank(this.managedObjectSourceName)) {
			this.context.getCompilerIssues().addIssue(this, "Null name for " + TYPE);
			return null; // must have name
		}

		// Ensure have the managed object source
		boolean hasManagedObjectSource = (this.suppliedManagedObjectNode != null)
				|| (this.state.managedObjectSource != null)
				|| (!CompileUtil.isBlank(this.state.managedObjectSourceClassName));
		if (!hasManagedObjectSource) {
			this.context.getCompilerIssues().addIssue(this,
					"Null source for " + TYPE + " " + this.managedObjectSourceName);
			return null; // must have source
		}

		// Load and return the type
		return this.loadType(compileContext, (mos, properties, loader) -> loader
				.loadOfficeFloorManagedObjectSourceType(this.managedObjectSourceName, mos, properties));
	}

	@Override
	public OfficeNode getManagingOfficeNode() {

		// Obtain the managing office
		OfficeNode managingOffice = LinkUtil.retrieveTarget(this.managingOffice, OfficeNode.class,
				this.context.getCompilerIssues());

		// Return the managing office
		return managingOffice;
	}

	@Override
	public boolean linkInputManagedObjectNode(InputManagedObjectNode inputManagedObject) {

		// Determine if already linked
		if (this.inputManagedObjectNode != null) {
			// Already linked
			this.context.getCompilerIssues().addIssue(this, "Managed object source " + this.managedObjectSourceName
					+ " already linked to an input managed object");
			return false;
		}

		// Link
		this.inputManagedObjectNode = inputManagedObject;
		return true;
	}

	@Override
	public InputManagedObjectNode getInputManagedObjectNode() {
		return this.inputManagedObjectNode;
	}

	@Override
	public void autoWireInputDependencies(AutoWirer<LinkObjectNode> autoWirer, OfficeNode office,
			CompileContext compileContext) {

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = compileContext.getOrLoadManagedObjectType(this);
		if (managedObjectType == null) {
			return; // must have type
		}

		// Create the map of dependency types by names
		Map<String, ManagedObjectDependencyType<?>> dependencyTypes = new HashMap<>();
		Arrays.stream(managedObjectType.getDependencyTypes())
				.forEach((dependencyType) -> dependencyTypes.put(dependencyType.getDependencyName(), dependencyType));

		// Auto-wire input dependencies
		this.inputDependencies.values().stream().sorted((a, b) -> CompileUtil
				.sortCompare(a.getManagedObjectDependencyName(), b.getManagedObjectDependencyName()))
				.forEachOrdered((dependency) -> {

					// Ignore if already configured
					if (dependency.getLinkedObjectNode() != null) {
						return;
					}

					// Obtain the dependency type
					ManagedObjectDependencyType<?> dependencyType = dependencyTypes
							.get(dependency.getManagedObjectDependencyName());
					if (dependencyType == null) {
						return; // must have type
					}

					// Auto-wire the dependency
					AutoWireLink<ManagedObjectDependencyNode, LinkObjectNode>[] links = autoWirer.getAutoWireLinks(
							dependency,
							new AutoWire(dependencyType.getTypeQualifier(), dependencyType.getDependencyType()));
					if (links.length == 1) {
						LinkUtil.linkAutoWireObjectNode(dependency, links[0].getTargetNode(office), office, autoWirer,
								compileContext, this.context.getCompilerIssues(),
								(link) -> dependency.linkObjectNode(link));
					}
				});
	}

	@Override
	public void autoWireFunctionDependencies(AutoWirer<LinkObjectNode> autoWirer, OfficeNode office,
			CompileContext compileContext) {

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = compileContext.getOrLoadManagedObjectType(this);
		if (managedObjectType == null) {
			return; // must have type
		}

		// Create the map of function dependency types by names
		Map<String, ManagedObjectFunctionDependencyType> functionDependencyTypes = new HashMap<>();
		Arrays.stream(managedObjectType.getFunctionDependencyTypes())
				.forEach((functionDependencyType) -> functionDependencyTypes
						.put(functionDependencyType.getFunctionObjectName(), functionDependencyType));

		// Auto-wire function dependencies
		this.functionDependencies.values().stream().sorted((a, b) -> CompileUtil
				.sortCompare(a.getManagedObjectDependencyName(), b.getManagedObjectDependencyName()))
				.forEachOrdered((functionDependency) -> {

					// Ignore if already configured
					if (functionDependency.getLinkedObjectNode() != null) {
						return;
					}

					// Obtain the function dependency type
					ManagedObjectFunctionDependencyType functionDependencyType = functionDependencyTypes
							.get(functionDependency.getManagedObjectDependencyName());
					if (functionDependencyType == null) {
						return; // must have type
					}

					// Auto-wire the dependency
					AutoWireLink<ManagedObjectFunctionDependencyNode, LinkObjectNode>[] links = autoWirer
							.getAutoWireLinks(functionDependency,
									new AutoWire(functionDependencyType.getFunctionObjectType()));
					if (links.length == 1) {
						LinkUtil.linkAutoWireObjectNode(functionDependency, links[0].getTargetNode(office), office,
								autoWirer, compileContext, this.context.getCompilerIssues(),
								(link) -> functionDependency.linkObjectNode(link));
					}
				});
	}

	@Override
	public boolean linkAutoWireStartBefore(String managedObjectType) {
		this.startBeforeManagedObjectTypes.add(managedObjectType);
		return true;
	}

	@Override
	public boolean linkAutoWireStartAfter(String managedObjectType) {
		this.startAfterManagedObjectTypes.add(managedObjectType);
		return true;
	}

	@Override
	public boolean isAutoWireStartupOrdering() {
		return (this.startBeforeManagedObjectTypes.size() > 0) || (this.startAfterManagedObjectTypes.size() > 0);
	}

	@Override
	public void autoWireStartupOrdering(AutoWirer<ManagedObjectSourceNode> autoWirer, OfficeNode office,
			CompileContext compileContext) {

		// Undertake before start up ordering
		this.autoWireStartupOrdering(this.startBeforeManagedObjectTypes, autoWirer, office, compileContext,
				(mos) -> this.linkStartBeforeNode(mos));

		// Undertake after start up ordering
		this.autoWireStartupOrdering(this.startAfterManagedObjectTypes, autoWirer, office, compileContext,
				(mos) -> this.linkStartAfterNode(mos));
	}

	/**
	 * Undertakes auto-wiring the start up ordering.
	 * 
	 * @param managedObjectTypes  {@link ManagedObject} object types for start up
	 *                            ordering.
	 * @param autoWirer           {@link AutoWirer}.
	 * @param office              {@link OfficeNode}.
	 * @param compileContext      {@link CompileContext}.
	 * @param loadStartupOrdering Loads the start up ordering.
	 */
	private void autoWireStartupOrdering(List<String> managedObjectTypes, AutoWirer<ManagedObjectSourceNode> autoWirer,
			OfficeNode office, CompileContext compileContext, Consumer<ManagedObjectSourceNode> loadStartupOrdering) {

		// Provide start up ordering on all managed object types
		for (String managedObjectType : managedObjectTypes) {

			// Obtain the start before auto-wires
			for (AutoWireLink<ManagedObjectSourceNodeImpl, ManagedObjectSourceNode> link : autoWirer
					.findAutoWireLinks(this, new AutoWire(managedObjectType))) {
				ManagedObjectSourceNode mosNode = link.getTargetNode(office);
				if (mosNode != null) {

					// Set up start up ordering
					loadStartupOrdering.accept(mosNode);
				}
			}
		}
	}

	@Override
	public void autoWireTeams(AutoWirer<LinkTeamNode> autoWirer, CompileContext compileContext) {

		// Capture the auto wirer for later building
		this.teamAutoWirer = autoWirer;

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = compileContext.getOrLoadManagedObjectType(this);
		if (managedObjectType == null) {
			return; // must have managed object type
		}

		// Obtain the managing office
		OfficeNode managingOffice = (this.containingOfficeNode != null ? this.containingOfficeNode
				: this.getManagingOfficeNode());

		// Obtain the object type
		Class<?> objectType = managedObjectType.getObjectType();

		// Auto-wire the teams
		for (ManagedObjectTeamType teamType : managedObjectType.getTeamTypes()) {

			// Obtain the team name
			String teamName = teamType.getTeamName();

			// Obtain the team
			ManagedObjectTeamNode teamNode = NodeUtil.getNode(teamName, this.teams,
					() -> this.context.createManagedObjectTeamNode(teamName, this));

			// Ensure the team is not already configured
			if (teamNode.getLinkedTeamNode() != null) {
				continue; // team already configured
			}

			// Create the source auto-wire (type qualified by team name)
			AutoWire sourceAutoWire = new AutoWire(teamName, objectType);

			// Attempt to auto-wire the team
			AutoWireLink<ManagedObjectTeamNode, LinkTeamNode>[] links = autoWirer.findAutoWireLinks(teamNode,
					sourceAutoWire);
			if (links.length == 1) {
				LinkUtil.linkTeamNode(teamNode, links[0].getTargetNode(managingOffice),
						this.context.getCompilerIssues(), (link) -> teamNode.linkTeamNode(link));
			}
		}
	}

	@Override
	public void autoWireToOffice(OfficeNode officeNode, CompilerIssues issues) {

		// Determine if already managed by an office
		if (this.managingOffice.getLinkedOfficeNode() != null) {
			return; // already managed
		}

		// Not managed by office, so link to the office
		LinkUtil.linkOfficeNode(this.managingOffice, officeNode, issues,
				(link) -> this.managingOffice.linkOfficeNode(link));
	}

	@Override
	public ExecutionManagedFunction createExecutionManagedFunction(ManagedObjectFlowType<?> flowType,
			CompileContext compileContext) {

		// Obtain the flow
		String flowName = flowType.getFlowName();
		ManagedObjectFlowNode flow = this.flows.get(flowName);
		if (flow == null) {
			return null;
		}

		// Obtain the managed function
		ManagedFunctionNode function = LinkUtil.findTarget(flow, ManagedFunctionNode.class,
				this.context.getCompilerIssues());
		if (function == null) {
			return null;
		}

		// Create and return the execution function
		return function.createExecutionManagedFunction(compileContext);
	}

	@Override
	public String getManagedObjectSourceName() {
		return this.getQualifiedName();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void buildManagedObject(OfficeFloorBuilder builder, OfficeNode managingOffice, OfficeBuilder officeBuilder,
			OfficeBindings officeBindings, CompileContext compileContext) {

		// Load the managed object source and properties
		this.loadType(compileContext, (managedObjectSource, properties, loader) -> {

			// Load the managed object type
			ManagedObjectType<?> managedObjectType = loader.loadManagedObjectType(managedObjectSource, properties);
			if (managedObjectType == null) {
				return null; // must load type
			}

			// Obtain the name to add this managed object source
			final String managedObjectSourceName = this.getManagedObjectSourceName();

			// Register as possible MBean
			compileContext.registerPossibleMBean(ManagedObjectSource.class, managedObjectSourceName,
					managedObjectSource);

			// Register supplier as possible MBean
			if (this.suppliedManagedObjectNode != null) {
				this.suppliedManagedObjectNode.getSupplierNode().registerAsPossibleMBean(compileContext);
			}

			// Build the managed object source from supplied managed object
			ManagedObjectBuilder moBuilder = builder.addManagedObject(managedObjectSourceName, managedObjectSource);

			// Load the profiles
			String[] profiles = this.context.additionalProfiles(this.containingOfficeNode);
			for (String profile : profiles) {
				moBuilder.addAdditionalProfile(profile);
			}

			// Add properties for Managed Object Source
			for (Property property : properties) {
				moBuilder.addProperty(property.getName(), property.getValue());
			}

			// Provide timeout
			moBuilder.setTimeout(this.timeout);

			// Specify the managing office
			ManagingOfficeBuilder managingOfficeBuilder = moBuilder
					.setManagingOffice(managingOffice.getDeployedOfficeName());

			// Check if input managed object source
			if (!managedObjectType.isInput()) {
				// Not input, so ensure not configure as input
				if (this.inputManagedObjectNode != null) {
					// Provide issue as should be input
					this.context.getCompilerIssues().addIssue(this,
							"Attempting to configure managed object source " + this.managedObjectSourceName
									+ " as input managed object, when does not input into the application");
				}

			} else {
				// Input, so ensure have Input ManagedObject name
				String inputBoundManagedObjectName = null;
				if ((this.containingSectionNode != null) || (this.containingOfficeNode != null)) {
					// Can not link to managed object initiating flow.
					// Use name of managed object source.
					inputBoundManagedObjectName = managedObjectSourceName;

				} else {
					// Must configure as Input Managed Object (as shared)
					if (this.inputManagedObjectNode != null) {
						inputBoundManagedObjectName = this.inputManagedObjectNode.getBoundManagedObjectName();

						// Obtain the input object type
						String inputObjectTypeName = this.inputManagedObjectNode.getInputObjectType();
						if (inputObjectTypeName != null) {

							// Obtain the input type
							Class<?> inputObjectType = CompileUtil.obtainClass(inputObjectTypeName, Object.class, null,
									this.context.getRootSourceContext(), this, this.context.getCompilerIssues());

							// Ensure compatible type
							Class<?> objectType = managedObjectType.getObjectType();
							if (!inputObjectType.isAssignableFrom(objectType)) {
								// MOS object type not compatible to input type
								this.context.getCompilerIssues().addIssue(this,
										"Managed Object Source object " + objectType.getName()
												+ " is not compatible with input managed object "
												+ inputBoundManagedObjectName + " (input object type "
												+ inputObjectType.getName() + ")");
								return null; // invalid type for input
							}
						}
					}
				}

				if (CompileUtil.isBlank(inputBoundManagedObjectName)) {
					// Provide issue as should be input
					this.context.getCompilerIssues().addIssue(this,
							"Must provide input managed object for managed object source "
									+ this.managedObjectSourceName + " as managed object source has flows/teams");
				} else {
					// Bind managed object to process state of managing office
					ThreadDependencyMappingBuilder inputDependencyMappings = managingOfficeBuilder
							.setInputManagedObjectName(inputBoundManagedObjectName);

					// Provide governance for office floor input managed object.
					// For others, should be configured through Office (flows)
					if (this.inputManagedObjectNode != null) {

						// Map in the governances
						GovernanceNode[] governances = this.inputManagedObjectNode.getGovernances(managingOffice);
						for (GovernanceNode governance : governances) {
							inputDependencyMappings.mapGovernance(governance.getOfficeGovernanceName());
						}

						// Map in the pre-load administrations
						AdministrationNode[] preLoadAdmins = this.inputManagedObjectNode
								.getPreLoadAdministrations(managingOffice);
						for (AdministrationNode preLoadAdmin : preLoadAdmins) {
							preLoadAdmin.buildPreLoadManagedObjectAdministration(inputDependencyMappings,
									compileContext);
						}
					}

					// Determine if dependencies
					ManagedObjectDependencyType<?>[] dependencyTypes = managedObjectType.getDependencyTypes();
					if (dependencyTypes.length > 0) {

						// Load the dependencies for the input managed object
						for (ManagedObjectDependencyType<?> dependencyType : managedObjectType.getDependencyTypes()) {

							// Obtain the dependency type details
							String dependencyName = dependencyType.getDependencyName();
							Enum dependencyKey = dependencyType.getKey();
							int dependencyIndex = dependencyType.getIndex();

							// Obtain the dependency
							ManagedObjectDependencyNode dependencyNode = this.inputDependencies.get(dependencyName);
							BoundManagedObjectNode dependency = LinkUtil.retrieveTarget(dependencyNode,
									BoundManagedObjectNode.class, this.context.getCompilerIssues());
							if (dependency == null) {
								continue; // must have dependency
							}

							// Ensure dependent managed object built into office
							officeBindings.buildManagedObjectIntoOffice(dependency);

							// Link the dependency
							String dependentManagedObjectName = dependency.getBoundManagedObjectName();
							if (dependencyKey != null) {
								inputDependencyMappings.mapDependency(dependencyKey, dependentManagedObjectName);
							} else {
								inputDependencyMappings.mapDependency(dependencyIndex, dependentManagedObjectName);
							}
						}
					}

					// Allow linking the supplier thread locals
					this.optionalThreadLocalLinker.setThreadDependencyMappingBuilder(inputDependencyMappings);
				}
			}

			// Link the function dependencies
			ManagedObjectFunctionDependencyType[] functionDependencyTypes = managedObjectType
					.getFunctionDependencyTypes();
			for (ManagedObjectFunctionDependencyType functionDependencyType : functionDependencyTypes) {

				// Obtain the dependency details
				String functionDependencyName = functionDependencyType.getFunctionObjectName();

				// Obtain the function dependency
				ManagedObjectFunctionDependencyNode dependencyNode = this.functionDependencies
						.get(functionDependencyName);
				BoundManagedObjectNode dependency = LinkUtil.retrieveTarget(dependencyNode,
						BoundManagedObjectNode.class, this.context.getCompilerIssues());
				if (dependency == null) {
					continue; // must have dependency
				}

				// Ensure dependent managed object built into office
				officeBindings.buildManagedObjectIntoOffice(dependency);

				// Link the function dependency
				String dependentManagedObjectName = dependency.getBoundManagedObjectName();
				managingOfficeBuilder.mapFunctionDependency(functionDependencyName, dependentManagedObjectName);
			}

			// Enhance functions for auto wire responsible team
			Set<String> registeredTeamNames = new HashSet<>();
			if (this.teamAutoWirer != null) {
				moBuilder.addFunctionEnhancer((functionContext) -> {

					// Do nothing if already responsible team
					if (functionContext.getResponsibleTeam() != null) {
						return;
					}

					// Resolve the function dependencies to auto wires
					Set<AutoWire> autoWires = new HashSet<>();
					for (ManagedObjectFunctionDependency functionDependency : functionContext
							.getFunctionDependencies()) {

						// Obtain the function dependency node
						ManagedObjectFunctionDependencyNode functionDependencyNode = this.functionDependencies
								.get(functionDependency.getFunctionDependencyName());
						if (functionDependencyNode == null) {
							return; // must have node
						}

						// Load the auto-wires for the function dependency
						LinkUtil.loadAllObjectAutoWires(functionDependencyNode, autoWires, compileContext,
								this.context.getCompilerIssues());
					}

					// Create the listing of source auto wires
					AutoWire[] sourceAutoWires = autoWires.stream().toArray(AutoWire[]::new);

					// No auto-wires then no auto-wire team
					if (sourceAutoWires.length == 0) {
						return;
					}

					// Attempt to obtain the responsible team
					AutoWireLink<?, LinkTeamNode>[] links = this.teamAutoWirer.findAutoWireLinks(this, sourceAutoWires);
					if (links.length == 1) {

						// Obtain the responsible team
						LinkTeamNode linkTeam = links[0].getTargetNode(managingOffice);
						TeamNode teamNode = linkTeam instanceof TeamNode ? (TeamNode) linkTeam
								: LinkUtil.findTarget(linkTeam, TeamNode.class, this.context.getCompilerIssues());
						if (teamNode == null) {
							return;
						}

						// Obtain the team name
						String teamName = teamNode.getQualifiedName();

						// Register the team to the office
						if (!registeredTeamNames.contains(teamName)) {
							String officeTeamName = managedObjectSourceName + "." + teamName;
							officeBuilder.registerTeam(officeTeamName, teamName);
						}

						// Make team responsible for function
						functionContext.setResponsibleTeam(teamName);
					}
				});
			}

			// Link in the flows for the managed object source
			ManagedObjectFlowType<?>[] flowTypes = managedObjectType.getFlowTypes();
			for (final ManagedObjectFlowType<?> flowType : flowTypes) {

				// Obtain the flow type details
				String flowName = flowType.getFlowName();
				Enum<?> flowKey = flowType.getKey();
				int flowIndex = flowType.getIndex();

				// Obtain the function for the flow
				ManagedObjectFlowNode flowNode = this.flows.get(flowName);
				ManagedFunctionNode functionNode = LinkUtil.retrieveTarget(flowNode, ManagedFunctionNode.class,
						this.context.getCompilerIssues());
				if (functionNode == null) {
					continue; // must have function node
				}

				// Ensure the function is contained in the managing office
				FunctionNamespaceNode namespaceNode = functionNode.getFunctionNamespaceNode();
				SectionNode section = namespaceNode.getSectionNode();
				OfficeNode functionOffice = section.getOfficeNode();
				if (functionOffice != managingOffice) {
					this.context.getCompilerIssues().addIssue(this,
							"Linked function of flow " + flowName + " from managed object source "
									+ this.managedObjectSourceName + " must be within the managing office");
					continue; // function must be within managing office
				}

				// Obtain the details of function to link flow
				final String functionName = functionNode.getQualifiedFunctionName();

				// Link flow from managed object source to function
				if (flowKey != null) {
					managingOfficeBuilder.linkFlow(flowKey, functionName);
				} else {
					managingOfficeBuilder.linkFlow(flowIndex, functionName);
				}
			}

			// Link in the teams for the managed object source
			ManagedObjectTeamType[] teamTypes = managedObjectType.getTeamTypes();
			for (ManagedObjectTeamType teamType : teamTypes) {

				// Obtain the team type details
				String teamName = teamType.getTeamName();

				// Obtain the team
				ManagedObjectTeamNode managedObjectTeam = this.teams.get(teamName);
				TeamNode team = LinkUtil.findTarget(managedObjectTeam, TeamNode.class,
						this.context.getCompilerIssues());
				if (team == null) {
					continue; // must have the team
				}

				// Register the team to the office
				if (!registeredTeamNames.contains(teamName)) {
					String officeTeamName = managedObjectSourceName + "." + teamName;
					officeBuilder.registerTeam(officeTeamName, team.getOfficeFloorTeamName());
				}
			}

			// Determine if default execution strategy
			if (!(managingOffice.getOfficeFloorNode().isDefaultExecutionStrategy())) {

				// Link in the execution strategies for the managed object source
				ManagedObjectExecutionStrategyType[] strategyTypes = managedObjectType.getExecutionStrategyTypes();
				for (int i = 0; i < strategyTypes.length; i++) {
					ManagedObjectExecutionStrategyType strategyType = strategyTypes[i];

					// Obtain the execution strategy type details
					String strategyName = strategyType.getExecutionStrategyName();

					// Obtain the execution strategy
					ManagedObjectExecutionStrategyNode managedObjectExecution = this.executionStrategies
							.get(strategyName);
					ExecutionStrategyNode executionStrategy = LinkUtil.retrieveTarget(managedObjectExecution,
							ExecutionStrategyNode.class, this.context.getCompilerIssues());
					if (executionStrategy == null) {
						continue; // must have execution strategy
					}

					// Register the execution strategy
					String executionStrategyName = executionStrategy.getOfficeFloorExecutionStratgyName();
					managingOfficeBuilder.linkExecutionStrategy(i, executionStrategyName);
				}
			}

			// Determine if pool the managed object
			ManagedObjectPoolNode poolNode = LinkUtil.findTarget(this, ManagedObjectPoolNode.class,
					this.context.getCompilerIssues());
			if (poolNode != null) {
				poolNode.buildManagedObjectPool(moBuilder, managedObjectType, compileContext);
			}

			// Set up the start befores
			for (ManagedObjectSourceNode startLater : LinkUtil.findTargets((LinkStartBeforeNode) this,
					ManagedObjectSourceNode.class, this.context.getCompilerIssues())) {
				moBuilder.startupBefore(startLater.getQualifiedName());
			}

			// Set up the start afters
			for (ManagedObjectSourceNode startEarlier : LinkUtil.findTargets((LinkStartAfterNode) this,
					ManagedObjectSourceNode.class, this.context.getCompilerIssues())) {
				moBuilder.startupAfter(startEarlier.getQualifiedName());
			}

			// No type as built
			return null;
		});

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = compileContext.getOrLoadManagedObjectType(this);
		if (managedObjectType == null) {
			return; // must have managed object type
		}

		// Ensure all the teams are made available
		for (ManagedObjectTeamType teamType : managedObjectType.getTeamTypes()) {
			// Obtain team (ensures any missing teams are added)
			this.getOfficeFloorManagedObjectTeam(teamType.getTeamName());
		}
	}

	@Override
	public void buildSupplierThreadLocal(OptionalThreadLocalReceiver optionalThreadLocalReceiver) {
		this.optionalThreadLocalLinker.addOptionalThreadLocalReceiver(optionalThreadLocalReceiver);
	}

	/*
	 * =========================== (Common) =================================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.propertyList.addProperty(name).setValue(value);
	}

	@Override
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	/*
	 * ==================== SectionManagedObjectSource =========================
	 */

	@Override
	public String getSectionManagedObjectSourceName() {
		return (this.containingSectionNode != null ? this.managedObjectSourceName : null);
	}

	@Override
	public SectionManagedObject addSectionManagedObject(String managedObjectName,
			ManagedObjectScope managedObjectScope) {
		return this.managedObjectRegistry.addManagedObjectNode(managedObjectName, managedObjectScope, this);
	}

	@Override
	public SectionManagedObjectFlow getSectionManagedObjectFlow(String managedObjectSourceFlowName) {
		return this.getManagedObjectFlowNode(managedObjectSourceFlowName);
	}

	@Override
	public SectionManagedObjectDependency getInputSectionManagedObjectDependency(String managedObjectDependencyName) {
		return this.getManagedObjectDependencyNode(managedObjectDependencyName);
	}

	/*
	 * ============== OfficeSectionManagedObjectSource ========================
	 */

	@Override
	public String getOfficeSectionManagedObjectSourceName() {
		return (this.containingSectionNode != null ? this.managedObjectSourceName : null);
	}

	@Override
	public OfficeSectionManagedObjectTeam getOfficeSectionManagedObjectTeam(String teamName) {
		return this.getManagedObjectTeamNode(teamName);
	}

	@Override
	public OfficeSectionManagedObject getOfficeSectionManagedObject(String managedObjectName) {
		return this.managedObjectRegistry.getManagedObjectNode(managedObjectName);
	}

	/*
	 * ================== OfficeManagedObjectSource ============================
	 */

	@Override
	public String getOfficeManagedObjectSourceName() {
		return (this.containingOfficeNode != null ? this.managedObjectSourceName : null);
	}

	@Override
	public OfficeManagedObject addOfficeManagedObject(String managedObjectName, ManagedObjectScope managedObjectScope) {
		return this.managedObjectRegistry.addManagedObjectNode(managedObjectName, managedObjectScope, this);
	}

	@Override
	public OfficeManagedObjectFlow getOfficeManagedObjectFlow(String managedObjectSourceFlowName) {
		return this.getManagedObjectFlowNode(managedObjectSourceFlowName);
	}

	@Override
	public OfficeManagedObjectDependency getInputOfficeManagedObjectDependency(String managedObjectDependencyName) {
		return this.getManagedObjectDependencyNode(managedObjectDependencyName);
	}

	@Override
	public OfficeManagedObjectFunctionDependency getOfficeManagedObjectFunctionDependency(
			String managedObjectFunctionDependencyName) {
		return this.getManagedObjectFunctionDependencyNode(managedObjectFunctionDependencyName);
	}

	@Override
	public OfficeManagedObjectTeam getOfficeManagedObjectTeam(String managedObjectSourceTeamName) {
		return this.getManagedObjectTeamNode(managedObjectSourceTeamName);
	}

	/*
	 * =================== OfficeFloorManagedObjectSource ======================
	 */

	@Override
	public String getOfficeFloorManagedObjectSourceName() {
		return this.managedObjectSourceName;
	}

	@Override
	public ManagingOffice getManagingOffice() {
		return this.managingOffice;
	}

	@Override
	public OfficeFloorManagedObject addOfficeFloorManagedObject(String managedObjectName,
			ManagedObjectScope managedObjectScope) {
		return this.managedObjectRegistry.addManagedObjectNode(managedObjectName, managedObjectScope, this);
	}

	@Override
	public OfficeFloorManagedObjectFlow getOfficeFloorManagedObjectFlow(String managedObjectSourceFlowName) {
		return this.getManagedObjectFlowNode(managedObjectSourceFlowName);
	}

	@Override
	public OfficeFloorManagedObjectDependency getInputOfficeFloorManagedObjectDependency(
			String managedObjectDependencyName) {
		return this.getManagedObjectDependencyNode(managedObjectDependencyName);
	}

	@Override
	public OfficeFloorManagedObjectFunctionDependency getOfficeFloorManagedObjectFunctionDependency(
			String managedObjectFunctionDependencyName) {
		return this.getManagedObjectFunctionDependencyNode(managedObjectFunctionDependencyName);
	}

	@Override
	public OfficeFloorManagedObjectTeam getOfficeFloorManagedObjectTeam(String managedObjectSourceTeamName) {
		return this.getManagedObjectTeamNode(managedObjectSourceTeamName);
	}

	@Override
	public OfficeFloorManagedObjectExecutionStrategy getOfficeFloorManagedObjectExecutionStrategy(
			String managedObjectExecutionStrategyName) {
		return this.getManagedObjectExecutionStrategyNode(managedObjectExecutionStrategyName);
	}

	/*
	 * =================== LinkPoolNode ======================================
	 */

	/**
	 * Linked {@link LinkPoolNode}.
	 */
	private LinkPoolNode linkedPoolNode = null;

	@Override
	public boolean linkPoolNode(LinkPoolNode node) {
		return LinkUtil.linkPoolNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedPoolNode = link);
	}

	@Override
	public LinkPoolNode getLinkedPoolNode() {
		return this.linkedPoolNode;
	}

	/*
	 * ================ LinkStartBeforeNode ====================================
	 */

	private final List<LinkStartBeforeNode> startBefores = new LinkedList<>();

	@Override
	public boolean linkStartBeforeNode(LinkStartBeforeNode node) {
		this.startBefores.add(node);
		return true;
	}

	@Override
	public LinkStartBeforeNode[] getLinkedStartBeforeNodes() {
		return this.startBefores.toArray(new LinkStartBeforeNode[this.startBefores.size()]);
	}

	/*
	 * ================ LinkStartAfterNode ====================================
	 */

	private final List<LinkStartAfterNode> startAfters = new LinkedList<>();

	@Override
	public boolean linkStartAfterNode(LinkStartAfterNode node) {
		this.startAfters.add(node);
		return true;
	}

	@Override
	public LinkStartAfterNode[] getLinkedStartAfterNodes() {
		return this.startAfters.toArray(new LinkStartAfterNode[this.startAfters.size()]);
	}

}
