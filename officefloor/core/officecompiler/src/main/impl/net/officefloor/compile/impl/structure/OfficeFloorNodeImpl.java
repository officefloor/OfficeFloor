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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import net.officefloor.compile.impl.officefloor.OfficeFloorSourceContextImpl;
import net.officefloor.compile.impl.officefloor.OfficeFloorTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireDirection;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.AutoWirerVisitor;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.ExecutiveNode;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedObjectExtensionNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectPoolNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceVisitor;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeBindings;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.internal.structure.OfficeTeamRegistry;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.internal.structure.TeamVisitor;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.officefloor.OfficeFloorLoader;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.officefloor.OfficeFloorPropertyType;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.officefloor.OfficeFloorType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectExecutionStrategy;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectFlow;
import net.officefloor.compile.spi.officefloor.AugmentedManagedObjectTeam;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ManagedObjectSourceAugmentor;
import net.officefloor.compile.spi.officefloor.ManagedObjectSourceAugmentorContext;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDependencyObjectNode;
import net.officefloor.compile.spi.officefloor.OfficeFloorDependencyRequireNode;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutionStrategy;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutive;
import net.officefloor.compile.spi.officefloor.OfficeFloorFlowSinkNode;
import net.officefloor.compile.spi.officefloor.OfficeFloorFlowSourceNode;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectExecutionStrategy;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectPool;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorResponsibility;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.TeamAugmentor;
import net.officefloor.compile.spi.officefloor.TeamAugmentorContext;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionService;
import net.officefloor.compile.spi.officefloor.extension.OfficeFloorExtensionServiceFactory;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSource;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.team.TeamType;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.executive.source.ExecutiveSource;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.manage.UnknownOfficeException;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.api.source.AbstractSourceError;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.api.team.source.TeamSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link OfficeFloorNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorNodeImpl implements OfficeFloorNode, ManagedObjectSourceVisitor, TeamVisitor {

	/**
	 * {@link Class} name of the {@link OfficeFloorSource}.
	 */
	private final String officeFloorSourceClassName;

	/**
	 * Optionally provided instantiated {@link OfficeFloorSource}.
	 */
	private final OfficeFloorSource officeFloorSource;

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String officeFloorLocation;

	/**
	 * {@link PropertyList} to configure the {@link OfficeFloor}.
	 */
	private final PropertyList properties;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initialised state.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {
	}

	/**
	 * Mapping of {@link Profiler} by their {@link Office} name.
	 */
	private final Map<String, Profiler> profilers;

	/**
	 * {@link ManagedObjectSourceNode} instances by their
	 * {@link OfficeFloorManagedObjectSource} name.
	 */
	private final Map<String, ManagedObjectSourceNode> managedObjectSources = new HashMap<String, ManagedObjectSourceNode>();

	/**
	 * {@link InputManagedObjectNode} instances by their
	 * {@link OfficeFloorInputManagedObject} name.
	 */
	private final Map<String, InputManagedObjectNode> inputManagedObjects = new HashMap<String, InputManagedObjectNode>();

	/**
	 * {@link ManagedObjectNode} instances by their {@link OfficeFloorManagedObject}
	 * name.
	 */
	private final Map<String, ManagedObjectNode> managedObjects = new HashMap<String, ManagedObjectNode>();

	/**
	 * {@link ManagedObjectPoolNode} instances by their
	 * {@link OfficeFloorManagedObjectPool} name.
	 */
	private final Map<String, ManagedObjectPoolNode> managedObjectPools = new HashMap<>();

	/**
	 * {@link SupplierNode} instances by their {@link OfficeFloorSupplier} name.
	 */
	private final Map<String, SupplierNode> suppliers = new HashMap<String, SupplierNode>();

	/**
	 * {@link ExecutiveNode} for the {@link OfficeFloorExecutive}.
	 */
	private ExecutiveNode executive = null;

	/**
	 * {@link TeamNode} instances by their {@link OfficeFloorTeam} name.
	 */
	private final Map<String, TeamNode> teams = new HashMap<String, TeamNode>();

	/**
	 * {@link OfficeNode} instances by their {@link DeployedOffice} name.
	 */
	private final Map<String, OfficeNode> offices = new HashMap<String, OfficeNode>();

	/**
	 * Indicates whether to {@link AutoWire} the objects.
	 */
	private boolean isAutoWireObjects = false;

	/**
	 * Indicates whether to {@link AutoWire} the {@link Team} instances.
	 */
	private boolean isAutoWireTeams = false;

	/**
	 * {@link OfficeFloorListener} instances.
	 */
	private final List<OfficeFloorListener> listeners = new LinkedList<>();

	/**
	 * {@link ManagedObjectSourceAugmentor} instances.
	 */
	private final List<ManagedObjectSourceAugmentor> managedObjectSourceAugmentors = new LinkedList<>();

	/**
	 * {@link TeamAugmentor} instances.
	 */
	private final List<TeamAugmentor> teamAugmentors = new LinkedList<>();

	/**
	 * Initiate.
	 * 
	 * @param officeFloorSourceClassName {@link OfficeFloorSource} class name.
	 * @param officeFloorSource          Optional instantiated
	 *                                   {@link OfficeFloorSource}. May be
	 *                                   <code>null</code>.
	 * @param officeFloorLocation        Location of the {@link OfficeFloor}.
	 * @param context                    {@link NodeContext}.
	 * @param profilers                  Mapping of {@link Profiler} by their
	 *                                   {@link Office} name.
	 */
	public OfficeFloorNodeImpl(String officeFloorSourceClassName, OfficeFloorSource officeFloorSource,
			String officeFloorLocation, NodeContext context, Map<String, Profiler> profilers) {
		this.officeFloorSourceClassName = officeFloorSourceClassName;
		this.officeFloorSource = officeFloorSource;
		this.officeFloorLocation = officeFloorLocation;
		this.context = context;
		this.profilers = profilers;

		// Create the additional objects
		this.properties = this.context.createPropertyList();
	}

	/**
	 * Creates the {@link OfficeFloorSourceContext}.
	 * 
	 * @return {@link OfficeFloorSourceContext}.
	 */
	private OfficeFloorSourceContextImpl createOfficeFloorSourceContext() {
		return new OfficeFloorSourceContextImpl(false, this.officeFloorLocation, null, this.properties, this,
				this.context);
	}

	/*
	 * =========================== Node =====================================
	 */

	@Override
	public String getNodeName() {
		return OFFICE_FLOOR_NAME;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return NodeUtil.getLocation(this.officeFloorSourceClassName, this.officeFloorSource, this.officeFloorLocation);
	}

	@Override
	public Node getParentNode() {
		return null;
	}

	@Override
	public String getQualifiedName(String name) {
		return name; // do not qualify
	}

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes(this.teams, this.managedObjectSources, this.inputManagedObjects, this.suppliers,
				this.managedObjects, this.offices);
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise() {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState());
	}

	/*
	 * =================== OverrideProperties ===============================
	 */

	@Override
	public PropertyList getOverridePropertyList() {
		return this.properties;
	}

	/*
	 * ===================== ManagedObjectSourceVisitor =====================
	 */

	@Override
	public void visit(ManagedObjectType<?> managedObjectType, ManagedObjectSourceNode managedObjectSourceNode,
			CompileContext compileContext) {

		// Create the managed object source augment context
		ManagedObjectSourceAugmentorContext context = new ManagedObjectSourceAugmentorContext() {

			@Override
			public String getManagedObjectSourceName() {
				return managedObjectSourceNode.getQualifiedName();
			}

			@Override
			public ManagedObjectType<?> getManagedObjectType() {
				return managedObjectType;
			}

			@Override
			public AugmentedManagedObjectFlow getManagedObjectFlow(String managedObjectSourceFlowName) {
				return managedObjectSourceNode.getAugmentedManagedObjectFlow(managedObjectSourceFlowName);
			}

			@Override
			public AugmentedManagedObjectTeam getManagedObjectTeam(String managedObjectSourceTeamName) {
				return managedObjectSourceNode.getAugmentedManagedObjectTeam(managedObjectSourceTeamName);
			}

			@Override
			public AugmentedManagedObjectExecutionStrategy getManagedObjectExecutionStrategy(
					String managedObjectSourceExecutionStrategyName) {
				return managedObjectSourceNode
						.getAugmentedManagedObjectExecutionStrategy(managedObjectSourceExecutionStrategyName);
			}

			@Override
			public void link(AugmentedManagedObjectFlow flow, DeployedOfficeInput officeInput) {
				LinkUtil.linkFlow(flow, officeInput, OfficeFloorNodeImpl.this.context.getCompilerIssues(),
						managedObjectSourceNode);
			}

			@Override
			public void link(AugmentedManagedObjectTeam responsibility, OfficeFloorTeam team) {
				LinkUtil.linkTeam(responsibility, team, OfficeFloorNodeImpl.this.context.getCompilerIssues(),
						managedObjectSourceNode);
			}

			@Override
			public void link(AugmentedManagedObjectExecutionStrategy requiredStrategy,
					OfficeFloorExecutionStrategy executionStrategy) {
				LinkUtil.linkExecutionStrategy(requiredStrategy, executionStrategy,
						OfficeFloorNodeImpl.this.context.getCompilerIssues(), managedObjectSourceNode);
			}

			@Override
			public CompileError addIssue(String issueDescription) {
				return OfficeFloorNodeImpl.this.addIssue(issueDescription);
			}

			@Override
			public CompileError addIssue(String issueDescription, Throwable cause) {
				return OfficeFloorNodeImpl.this.addIssue(issueDescription, cause);
			}
		};

		// Augment the managed object source
		for (ManagedObjectSourceAugmentor augmentor : this.managedObjectSourceAugmentors) {
			augmentor.augmentManagedObjectSource(context);
		}
	}

	/*
	 * ========================== TeamVisitor ==============================
	 */

	@Override
	public void visit(TeamType teamType, TeamNode teamNode, CompileContext compileContext) {

		// Create the team augment context
		TeamAugmentorContext context = new TeamAugmentorContext() {

			@Override
			public String getTeamName() {
				return teamNode.getOfficeFloorTeamName();
			}

			@Override
			public TeamType getTeamType() {
				return teamType;
			}

			@Override
			public void requestNoTeamOversight() {
				teamNode.requestNoTeamOversight();
			}

			@Override
			public CompileError addIssue(String issueDescription) {
				return OfficeFloorNodeImpl.this.addIssue(issueDescription);
			}

			@Override
			public CompileError addIssue(String issueDescription, Throwable cause) {
				return OfficeFloorNodeImpl.this.addIssue(issueDescription, cause);
			}
		};

		// Augment the team
		for (TeamAugmentor augmentor : this.teamAugmentors) {
			augmentor.augmentTeam(context);
		}
	}

	/*
	 * ===================== ManagedObjectRegistry =============================
	 */

	@Override
	public ManagedObjectNode getManagedObjectNode(String managedObjectName) {
		return NodeUtil.getNode(managedObjectName, this.managedObjects,
				() -> this.context.createManagedObjectNode(managedObjectName, this));
	}

	@Override
	public ManagedObjectNode addManagedObjectNode(String managedObjectName, ManagedObjectScope managedObjectScope,
			ManagedObjectSourceNode managedObjectSourceNode) {
		return NodeUtil.getInitialisedNode(managedObjectName, this.managedObjects, this.context,
				() -> this.context.createManagedObjectNode(managedObjectName, this),
				(managedObject) -> managedObject.initialise(managedObjectScope, managedObjectSourceNode));
	}

	/*
	 * ===================== OfficeFloorDeployer =============================
	 */

	@Override
	public void enableAutoWireObjects() {
		this.isAutoWireObjects = true;
	}

	@Override
	public void enableAutoWireTeams() {
		this.isAutoWireTeams = true;
	}

	@Override
	public void addOfficeFloorListener(OfficeFloorListener listener) {
		this.listeners.add(listener);
	}

	@Override
	public void addManagedObjectSourceAugmentor(ManagedObjectSourceAugmentor managedObjectSourceAugmentor) {
		this.managedObjectSourceAugmentors.add(managedObjectSourceAugmentor);
	}

	@Override
	public void addTeamAugmentor(TeamAugmentor teamAugmentor) {
		this.teamAugmentors.add(teamAugmentor);
	}

	@Override
	public OfficeFloorManagedObjectSource addManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName, this.managedObjectSources, this.context,
				() -> this.context.createManagedObjectSourceNode(managedObjectSourceName, this),
				(managedObjectSource) -> managedObjectSource.initialise(managedObjectSourceClassName, null));
	}

	@Override
	public OfficeFloorManagedObjectSource addManagedObjectSource(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName, this.managedObjectSources, this.context,
				() -> this.context.createManagedObjectSourceNode(managedObjectSourceName, this),
				(managedObjectSourceNode) -> managedObjectSourceNode
						.initialise(managedObjectSource.getClass().getName(), managedObjectSource));
	}

	@Override
	public OfficeFloorInputManagedObject addInputManagedObject(String inputManagedObjectName, String inputObjectType) {
		return NodeUtil.getInitialisedNode(inputManagedObjectName, this.inputManagedObjects, this.context,
				() -> this.context.createInputManagedNode(inputManagedObjectName, inputObjectType, this),
				(inputManagedObject) -> inputManagedObject.initialise());
	}

	@Override
	public OfficeFloorManagedObjectPool addManagedObjectPool(String managedObjectPoolName,
			String managedObjectPoolSourceClassName) {
		return NodeUtil.getInitialisedNode(managedObjectPoolName, this.managedObjectPools, this.context,
				() -> this.context.createManagedObjectPoolNode(managedObjectPoolName, this),
				(managedObjectPool) -> managedObjectPool.initialise(managedObjectPoolSourceClassName, null));
	}

	@Override
	public OfficeFloorManagedObjectPool addManagedObjectPool(String managedObjectPoolName,
			ManagedObjectPoolSource managedObjectPoolSource) {
		return NodeUtil.getInitialisedNode(managedObjectPoolName, this.managedObjectPools, this.context,
				() -> this.context.createManagedObjectPoolNode(managedObjectPoolName, this),
				(managedObjectPool) -> managedObjectPool.initialise(managedObjectPoolSource.getClass().getName(),
						managedObjectPoolSource));
	}

	@Override
	public OfficeFloorSupplier addSupplier(String supplierName, String supplierSourceClassName) {
		return NodeUtil.getInitialisedNode(supplierName, this.suppliers, this.context,
				() -> this.context.createSupplierNode(supplierName, this),
				(supplier) -> supplier.initialise(supplierSourceClassName, null));
	}

	@Override
	public OfficeFloorSupplier addSupplier(String supplierName, SupplierSource supplierSource) {
		return NodeUtil.getInitialisedNode(supplierName, this.suppliers, this.context,
				() -> this.context.createSupplierNode(supplierName, this),
				(supplier) -> supplier.initialise(supplierSource.getClass().getName(), supplierSource));
	}

	@Override
	public OfficeFloorTeam addTeam(String teamName, String teamSourceClassName) {
		return NodeUtil.getInitialisedNode(teamName, this.teams, this.context,
				() -> this.context.createTeamNode(teamName, this),
				(team) -> team.initialise(teamSourceClassName, null));
	}

	@Override
	public OfficeFloorTeam addTeam(String teamName, TeamSource teamSource) {
		return NodeUtil.getInitialisedNode(teamName, this.teams, this.context,
				() -> this.context.createTeamNode(teamName, this),
				(team) -> team.initialise(teamSource.getClass().getName(), teamSource));
	}

	@Override
	public OfficeFloorExecutive setExecutive(String executiveSourceClassName) {
		this.executive = NodeUtil.getInitialisedNode(this.executive, this.context,
				() -> this.context.createExecutiveNode(this),
				(executive) -> executive.initialise(executiveSourceClassName, null));
		return this.executive;
	}

	@Override
	public OfficeFloorExecutive setExecutive(ExecutiveSource executiveSource) {
		this.executive = NodeUtil.getInitialisedNode(this.executive, this.context,
				() -> this.context.createExecutiveNode(this),
				(executive) -> executive.initialise(executiveSource.getClass().getName(), executiveSource));
		return this.executive;
	}

	@Override
	public DeployedOffice addDeployedOffice(String officeName, OfficeSource officeSource, String officeLocation) {
		return NodeUtil.getInitialisedNode(officeName, this.offices, this.context,
				() -> this.context.createOfficeNode(officeName, this),
				(office) -> office.initialise(officeSource.getClass().getName(), officeSource, officeLocation));
	}

	@Override
	public DeployedOffice addDeployedOffice(String officeName, String officeSourceClassName, String officeLocation) {
		return NodeUtil.getInitialisedNode(officeName, this.offices, this.context,
				() -> this.context.createOfficeNode(officeName, this),
				(office) -> office.initialise(officeSourceClassName, null, officeLocation));
	}

	@Override
	public DeployedOffice getDeployedOffice(String officeName) {
		return NodeUtil.getNode(officeName, this.offices, () -> this.context.createOfficeNode(officeName, this));
	}

	@Override
	public DeployedOffice[] getDeployedOffices() {
		return this.offices.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getDeployedOfficeName(), b.getDeployedOfficeName()))
				.collect(Collectors.toList()).toArray(new DeployedOffice[0]);
	}

	@Override
	public void link(OfficeFloorManagedObjectSource managedObjectSource,
			OfficeFloorInputManagedObject inputManagedObject) {
		LinkUtil.linkManagedObjectSourceInput(managedObjectSource, inputManagedObject, this.context.getCompilerIssues(),
				this);
	}

	@Override
	public void link(OfficeFloorManagedObjectSource managedObjectSource,
			OfficeFloorManagedObjectPool managedObjectPool) {
		LinkUtil.linkPool(managedObjectSource, managedObjectPool, this.context.getCompilerIssues(), this);
	}

	@Override
	public void link(OfficeFloorFlowSourceNode flowSourceNode, OfficeFloorFlowSinkNode flowSinkNode) {
		LinkUtil.linkFlow(flowSourceNode, flowSinkNode, this.context.getCompilerIssues(), this);
	}

	@Override
	public void link(OfficeFloorDependencyRequireNode dependencyRequireNode,
			OfficeFloorDependencyObjectNode dependencyObjectNode) {
		LinkUtil.linkObject(dependencyRequireNode, dependencyObjectNode, this.context.getCompilerIssues(), this);
	}

	@Override
	public void link(OfficeFloorResponsibility responsibility, OfficeFloorTeam officeFloorTeam) {
		LinkUtil.linkTeam(responsibility, officeFloorTeam, this.context.getCompilerIssues(), this);
	}

	@Override
	public void link(OfficeFloorManagedObjectExecutionStrategy managedObjectExecutionStrategy,
			OfficeFloorExecutionStrategy executionStrategy) {
		LinkUtil.linkExecutionStrategy(managedObjectExecutionStrategy, executionStrategy,
				this.context.getCompilerIssues(), this);
	}

	@Override
	public void link(ManagingOffice managingOffice, DeployedOffice office) {
		LinkUtil.linkOffice(managingOffice, office, this.context.getCompilerIssues(), this);
	}

	@Override
	public void startBefore(OfficeFloorManagedObjectSource startEarlier, OfficeFloorManagedObjectSource startLater) {
		LinkUtil.linkStartBefore(startEarlier, startLater, this.context.getCompilerIssues(), this);
	}

	@Override
	public void startBefore(OfficeFloorManagedObjectSource managedObjectSource, String managedObjectTypeName) {
		LinkUtil.linkAutoWireStartBefore(managedObjectSource, managedObjectTypeName, this.context.getCompilerIssues(),
				this);
	}

	@Override
	public void startAfter(OfficeFloorManagedObjectSource startLater, OfficeFloorManagedObjectSource startEarlier) {
		LinkUtil.linkStartAfter(startLater, startEarlier, this.context.getCompilerIssues(), this);
	}

	@Override
	public void startAfter(OfficeFloorManagedObjectSource managedObjectSource, String managedObjectTypeName) {
		LinkUtil.linkAutoWireStartAfter(managedObjectSource, managedObjectTypeName, this.context.getCompilerIssues(),
				this);
	}

	@Override
	public CompileError addIssue(String issueDescription) {
		return this.context.getCompilerIssues().addIssue(this, issueDescription);
	}

	@Override
	public CompileError addIssue(String issueDescription, Throwable cause) {
		return this.context.getCompilerIssues().addIssue(this, issueDescription, cause);
	}

	/*
	 * ===================== PropertyConfigurable =============================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	/*
	 * ===================== OfficeFloorNode ==================================
	 */

	@Override
	public OfficeFloorManagedObjectSource addManagedObjectSource(String managedObjectSourceName,
			SuppliedManagedObjectSourceNode suppliedManagedObject) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName, this.managedObjectSources, this.context,
				() -> this.context.createManagedObjectSourceNode(managedObjectSourceName, suppliedManagedObject),
				(managedObjectSource) -> managedObjectSource.initialise(null, null));
	}

	@Override
	public boolean sourceOfficeFloor(CompileContext compileContext) {

		// Determine if must instantiate
		OfficeFloorSource source = this.officeFloorSource;
		if (source == null) {

			// Obtain the OfficeFloor source class
			Class<? extends OfficeFloorSource> officeFloorSourceClass = this.context
					.getOfficeFloorSourceClass(this.officeFloorSourceClassName, this);
			if (officeFloorSourceClass == null) {
				return false; // must have OfficeFloor source class
			}

			// Instantiate the office floor source
			source = CompileUtil.newInstance(officeFloorSourceClass, OfficeFloorSource.class, this,
					this.context.getCompilerIssues());
			if (officeFloorSource == null) {
				return false; // failed to instantiate
			}
		}

		// Create the OfficeFloor source context
		OfficeFloorSourceContextImpl sourceContext = this.createOfficeFloorSourceContext();

		// Obtain the extension services (ensuring all are available)
		List<OfficeFloorExtensionService> extensionServices = new ArrayList<>();
		for (OfficeFloorExtensionService extensionService : sourceContext
				.loadOptionalServices(OfficeFloorExtensionServiceFactory.class)) {
			extensionServices.add(extensionService);
		}

		try {
			// Source the OfficeFloor
			source.sourceOfficeFloor(this, sourceContext);

			// Extend the OfficeFloor
			for (OfficeFloorExtensionService extensionService : extensionServices) {
				extensionService.extendOfficeFloor(this, sourceContext);
			}

		} catch (AbstractSourceError ex) {
			ex.addIssue(new SourceIssuesIssueTarget(this));
			return false; // can not carry on

		} catch (LoadTypeError ex) {
			ex.addLoadTypeIssue(this, this.context.getCompilerIssues());
			return false; // must not fail in loading types

		} catch (CompileError ex) {
			return false; // issue already reported

		} catch (Throwable ex) {
			this.addIssue("Failed to source " + OfficeFloor.class.getSimpleName() + " from "
					+ OfficeFloorSource.class.getSimpleName() + " (source=" + source.getClass().getName()
					+ ", location=" + officeFloorLocation + ")", ex);
			return false; // must be successful
		}

		// As here, successful
		return true;
	}

	@Override
	public boolean sourceOfficeFloorTree(AutoWirerVisitor autoWirerVisitor, CompileContext compileContext) {

		// Source the OfficeFloor
		boolean isSourced = this.sourceOfficeFloor(compileContext);
		if (!isSourced) {
			return false;
		}

		// Ensure all the suppliers are sourced
		isSourced = CompileUtil.source(this.suppliers, (supplier) -> supplier.getOfficeFloorSupplierName(),
				(supplier) -> supplier.sourceSupplier(compileContext));
		if (!isSourced) {
			return false;
		}

		// Ensure the executive is loaded
		if (this.executive != null) {
			isSourced = this.executive.sourceExecutive(compileContext);
			if (!isSourced) {
				return false;
			}
		}

		// Ensure all teams are sourced
		isSourced = CompileUtil.source(this.teams, (team) -> team.getOfficeFloorTeamName(),
				(team) -> team.sourceTeam(this, compileContext));
		if (!isSourced) {
			return false;
		}

		// Ensure all non-supplied managed object sources are sourced
		isSourced = CompileUtil.source(this.managedObjectSources,
				(managedObjectSource) -> managedObjectSource.getSectionManagedObjectSourceName(),
				(managedObjectSource) -> {
					if (managedObjectSource.isSupplied()) {
						return true; // successfully not sourced
					}

					// Source the managed object source
					return managedObjectSource.sourceManagedObjectSource(this, compileContext);
				});
		if (!isSourced) {
			return false;
		}

		// Ensure all non-supplied managed objects are sourced
		isSourced = CompileUtil.source(this.managedObjects,
				(managedObject) -> managedObject.getSectionManagedObjectName(), (managedObject) -> {
					if (managedObject.getManagedObjectSourceNode().isSupplied()) {
						return true; // successfully not sourced
					}

					// Source the managed object
					return managedObject.sourceManagedObject(compileContext);
				});
		if (!isSourced) {
			return false;
		}

		// Ensure all the managed object pools are sourced
		isSourced = CompileUtil.source(this.managedObjectPools, (pool) -> pool.getOfficeFloorManagedObjectPoolName(),
				(pool) -> pool.sourceManagedObjectPool(compileContext));
		if (!isSourced) {
			return false;
		}

		// Load the office override properties
		CompileUtil.source(this.offices, (office) -> office.getDeployedOfficeName(), (office) -> {
			String officeName = office.getDeployedOfficeName();
			String officePrefix = officeName + ".";
			for (Property property : this.getOverridePropertyList()) {
				String propertyName = property.getName();
				if (propertyName.startsWith(officePrefix)) {
					String overridePropertyName = propertyName.substring(officePrefix.length());
					String propertyValue = property.getValue();
					office.addOverrideProperty(overridePropertyName, propertyValue);
				}
			}
			return true;
		});

		// Source all the offices
		isSourced = CompileUtil.source(this.offices, (office) -> office.getDeployedOfficeName(),
				(office) -> office.sourceOfficeTree(this, autoWirerVisitor, compileContext));
		if (!isSourced) {
			return false;
		}

		// Ensure all the suppliers are completed
		isSourced = CompileUtil.source(this.suppliers, (supplier) -> supplier.getOfficeFloorSupplierName(),
				(supplier) -> supplier.sourceComplete(compileContext));
		if (!isSourced) {
			return false;
		}

		// Iterate over suppliers (ensuring no thread locals)
		isSourced = CompileUtil.source(this.suppliers, (supplier) -> supplier.getOfficeFloorSupplierName(),
				(supplier) -> supplier.ensureNoThreadLocals(compileContext));
		if (!isSourced) {
			return false;
		}

		// Ensure the OfficeFloor tree is initialised
		this.initialise();
		if (!NodeUtil.isNodeTreeInitialised(this, this.context.getCompilerIssues())) {
			return false; // must have fully initialised tree
		}

		// Undertake auto-wire of objects
		if (this.isAutoWireObjects) {

			// Create the auto wirer
			final AutoWirer<LinkObjectNode> officeFloorAutoWirer = this.context.createAutoWirer(LinkObjectNode.class,
					AutoWireDirection.SOURCE_REQUIRES_TARGET);
			final AutoWirer<LinkObjectNode> autoWirer = this.loadAutoWireObjectTargets(officeFloorAutoWirer,
					compileContext);

			// Iterate over offices (auto-wiring unlinked dependencies)
			this.offices.values().stream()
					.sorted((a, b) -> CompileUtil.sortCompare(a.getDeployedOfficeName(), b.getDeployedOfficeName()))
					.forEachOrdered((office) -> office.autoWireObjects(autoWirer, compileContext));

			// Iterate over managed objects (auto-wiring unlinked dependencies)
			this.managedObjects.values().stream().sorted((a, b) -> CompileUtil
					.sortCompare(a.getOfficeFloorManagedObjectName(), b.getOfficeFloorManagedObjectName()))
					.forEachOrdered((managedObject) -> {
						// Obtain the managing office for the managed object
						ManagedObjectSourceNode managedObjectSource = managedObject.getManagedObjectSourceNode();
						OfficeNode officeNode = managedObjectSource.getManagingOfficeNode();

						// Load the dependencies for the managed object
						managedObject.autoWireDependencies(autoWirer, officeNode, compileContext);
					});

			// Iterate over mo sources (for dependencies and start up)
			this.managedObjectSources.values().stream()
					.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeFloorManagedObjectSourceName(),
							b.getOfficeFloorManagedObjectSourceName()))
					.forEachOrdered((managedObjectSource) -> {

						// Obtain the managing office for the managed object
						OfficeNode officeNode = managedObjectSource.getManagingOfficeNode();

						// Load input dependencies for managed object source
						managedObjectSource.autoWireInputDependencies(autoWirer, officeNode, compileContext);

						// Load the function dependencies for managed object source
						managedObjectSource.autoWireFunctionDependencies(autoWirer, officeNode, compileContext);
					});
		}

		// Determine if auto-wired start up ordering
		boolean isAutoWireStartupOrdering = this.managedObjectSources.values().stream()
				.anyMatch((mos) -> mos.isAutoWireStartupOrdering());
		if (isAutoWireStartupOrdering) {

			// Create the auto wirer
			final AutoWirer<ManagedObjectSourceNode> officeFloorAutoWirer = this.context
					.createAutoWirer(ManagedObjectSourceNode.class, AutoWireDirection.SOURCE_REQUIRES_TARGET);
			final AutoWirer<ManagedObjectSourceNode> autoWirer = this
					.loadAutoWireManagedObjectSourceTargets(officeFloorAutoWirer, compileContext);

			// Auto-wire the start up ordering
			this.managedObjectSources.values().stream()
					.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeFloorManagedObjectSourceName(),
							b.getOfficeFloorManagedObjectSourceName()))
					.forEachOrdered((managedObjectSource) -> {

						// Obtain the managing office for the managed object
						OfficeNode officeNode = managedObjectSource.getManagingOfficeNode();

						// Load the start up ordering
						managedObjectSource.autoWireStartupOrdering(autoWirer, officeNode, compileContext);
					});
		}

		// Undertake auto-wire of teams
		if (this.isAutoWireTeams) {

			// Create the auto-wirer
			final AutoWirer<LinkTeamNode> autoWirer = this.context.createAutoWirer(LinkTeamNode.class,
					AutoWireDirection.TARGET_CATEGORISES_SOURCE);
			this.teams.values().forEach((team) -> {

				// Create the target auto-wires
				AutoWire[] targetAutoWires = Arrays.stream(team.getTypeQualifications())
						.map((type) -> new AutoWire(type.getQualifier(), type.getType())).toArray(AutoWire[]::new);

				// Add the target
				autoWirer.addAutoWireTarget(team, targetAutoWires);
			});

			// Iterate over offices (auto-wiring teams)
			this.offices.values().stream()
					.sorted((a, b) -> CompileUtil.sortCompare(a.getDeployedOfficeName(), b.getDeployedOfficeName()))
					.forEachOrdered((office) -> office.autoWireTeams(autoWirer, compileContext));

			// Auto-wire managed object source teams
			this.managedObjectSources.values().stream()
					.sorted((a, b) -> CompileUtil.sortCompare(a.getQualifiedName(), b.getQualifiedName()))
					.forEachOrdered((mos) -> mos.autoWireTeams(autoWirer, compileContext));
		}

		// Run the execution explorers (as now fully configured)
		boolean isExplored = this.offices.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getDeployedOfficeName(), b.getDeployedOfficeName()))
				.allMatch((office) -> office.runExecutionExplorers(compileContext));
		if (!isExplored) {
			return false;
		}

		// As here, successfully sourced
		return true;
	}

	@Override
	public AvailableType[] getAvailableTypes(CompileContext compileContext) {
		OfficeFloorSourceContext sourceContext = this.createOfficeFloorSourceContext();
		return AvailableTypeImpl.extractAvailableTypes(this.managedObjects, compileContext, sourceContext);
	}

	@Override
	public AutoWirer<LinkObjectNode> loadAutoWireObjectTargets(AutoWirer<LinkObjectNode> autoWirer,
			CompileContext compileContext) {

		// Load the input managed objects (last to auto-wire)
		this.inputManagedObjects.values().forEach((inputMo) -> {

			// Create the auto-wires
			AutoWire[] targetAutoWires;
			TypeQualification[] typeQualifications = inputMo.getTypeQualifications(compileContext);
			if (typeQualifications.length == 0) {
				// Use the input type (if available)
				String inputObjectType = inputMo.getInputObjectType();
				if (inputObjectType == null) {
					return; // no input type
				}
				targetAutoWires = new AutoWire[] { new AutoWire(inputObjectType) };
			} else {
				// Use the type qualifications
				targetAutoWires = Arrays.stream(typeQualifications)
						.map((type) -> new AutoWire(type.getQualifier(), type.getType())).toArray(AutoWire[]::new);
			}

			// Add the target
			autoWirer.addAutoWireTarget(inputMo, targetAutoWires);
		});

		// Load the supplied managed objects
		final AutoWirer<LinkObjectNode> supplierAutoWirer = autoWirer.createScopeAutoWirer();
		this.suppliers.values().stream()
				.forEach((supplier) -> supplier.loadAutoWireObjects(supplierAutoWirer, this, compileContext));

		// Load the managed objects
		final AutoWirer<LinkObjectNode> managedObjectAutoWirer = supplierAutoWirer.createScopeAutoWirer();
		this.managedObjects.values().forEach((mo) -> {

			// Create the auto-wires
			AutoWire[] targetAutoWires = Arrays.stream(mo.getTypeQualifications(compileContext))
					.map((type) -> new AutoWire(type.getQualifier(), type.getType())).toArray(AutoWire[]::new);

			// Add the target
			managedObjectAutoWirer.addAutoWireTarget(mo, targetAutoWires);
		});

		// Return the auto wirer
		return managedObjectAutoWirer;
	}

	@Override
	public AutoWirer<ManagedObjectSourceNode> loadAutoWireManagedObjectSourceTargets(
			AutoWirer<ManagedObjectSourceNode> autoWirer, CompileContext compileContext) {

		// Load the managed object sources
		this.managedObjectSources.values().forEach((mos) -> {

			// Load the type
			ManagedObjectType<?> moType = mos.loadManagedObjectType(compileContext);
			if (moType != null) {

				// Register the auto wire
				Class<?> objectType = moType.getObjectType();
				autoWirer.addAutoWireTarget(mos, new AutoWire(objectType));
			}
		});

		// Return the auto wirer
		return autoWirer.createScopeAutoWirer();
	}

	@Override
	public AutoWirer<ManagedObjectExtensionNode> loadAutoWireExtensionTargets(
			AutoWirer<ManagedObjectExtensionNode> autoWirer, CompileContext compileContext) {

		// Load the managed objects
		final AutoWirer<ManagedObjectExtensionNode> managedObjectAutoWirer = autoWirer.createScopeAutoWirer();
		this.managedObjects.values().forEach((mo) -> {

			// Load the managed object type
			ManagedObjectType<?> moType = mo.getManagedObjectSourceNode().loadManagedObjectType(compileContext);
			if (moType == null) {
				return; // must have type
			}

			// Load the auto-wiring for the extensions
			for (Class<?> extensionType : moType.getExtensionTypes()) {
				managedObjectAutoWirer.addAutoWireTarget(mo, new AutoWire(extensionType));
			}
		});

		// Load the supplied managed objects
		final AutoWirer<ManagedObjectExtensionNode> supplierAutoWirer = managedObjectAutoWirer.createScopeAutoWirer();
		this.suppliers.values().stream()
				.forEach((supplier) -> supplier.loadAutoWireExtensions(supplierAutoWirer, this, compileContext));

		// Return the auto wirer
		return supplierAutoWirer;
	}

	@Override
	public void loadAutoWireTeamTargets(AutoWirer<LinkTeamNode> autoWirer, OfficeTeamRegistry officeTeamRegistry,
			CompileContext compileContext) {
		this.teams.values().forEach((team) -> {

			// Create the auto-wires
			AutoWire[] targetAutoWires = Arrays.stream(team.getTypeQualifications())
					.map((type) -> new AutoWire(type.getQualifier(), type.getType())).toArray(AutoWire[]::new);

			// Add the target
			autoWirer.addAutoWireTarget((office) -> {

				// Determine if team already linked to office
				for (OfficeTeamNode officeTeam : officeTeamRegistry.getOfficeTeams()) {
					TeamNode linkedTeam = LinkUtil.findTarget(officeTeam, TeamNode.class,
							this.context.getCompilerIssues());
					if (linkedTeam == team) {
						// Already linked team, so use
						return officeTeam;
					}
				}

				// As here, the team is not link to office (so link)
				OfficeTeamNode officeTeam = officeTeamRegistry.createOfficeTeam(team.getOfficeFloorTeamName());
				LinkUtil.linkTeamNode(officeTeam, team, this.context.getCompilerIssues(),
						(link) -> officeTeam.linkTeamNode(link));
				return officeTeam;

			}, targetAutoWires);
		});
	}

	@Override
	public OfficeFloorType loadOfficeFloorType(CompileContext compileContext) {

		// Obtain the OfficeFloor source class
		Class<? extends OfficeFloorSource> officeFloorSourceClass = this.context
				.getOfficeFloorSourceClass(this.officeFloorSourceClassName, this);

		// Obtain the loader to load properties
		OfficeFloorLoader loader = this.context.getOfficeFloorLoader(this);

		// Load the specification properties
		PropertyList properties = loader.loadSpecification(officeFloorSourceClass);

		// Load the required properties
		PropertyList requiredProperties = loader.loadRequiredProperties(officeFloorSourceClass, officeFloorLocation,
				this.properties);
		for (Property property : requiredProperties) {
			String propertyName = property.getName();
			if (properties.getProperty(propertyName) == null) {
				properties.addProperty(propertyName, property.getLabel());
			}
		}

		// Load optional properties
		for (Property property : this.properties) {
			properties.getOrAddProperty(property.getName()).setValue(property.getValue());
		}

		// Load the properties
		OfficeFloorPropertyType[] propertyTypes = PropertyNode.constructPropertyNodes(properties);

		// Load the managed object source types (in deterministic order)
		OfficeFloorManagedObjectSourceType[] managedObjectSourceTypes = CompileUtil.loadTypes(this.managedObjectSources,
				(managedObjectSource) -> managedObjectSource.getOfficeFloorManagedObjectSourceName(),
				(managedObjectSource) -> managedObjectSource.loadOfficeFloorManagedObjectSourceType(compileContext),
				OfficeFloorManagedObjectSourceType[]::new);
		if (managedObjectSourceTypes == null) {
			return null;
		}

		// Load the team sources (in deterministic order)
		OfficeFloorTeamSourceType[] teamTypes = CompileUtil.loadTypes(this.teams,
				(team) -> team.getOfficeFloorTeamName(), (team) -> team.loadOfficeFloorTeamSourceType(compileContext),
				OfficeFloorTeamSourceType[]::new);
		if (teamTypes == null) {
			return null;
		}

		// Load and return the type
		return new OfficeFloorTypeImpl(propertyTypes, managedObjectSourceTypes, teamTypes);
	}

	@Override
	public OfficeFloorListener[] getOfficeFloorListeners() {
		return this.listeners.toArray(new OfficeFloorListener[this.listeners.size()]);
	}

	@Override
	public boolean isDefaultExecutionStrategy() {
		return (this.executive == null);
	}

	@Override
	public OfficeFloor deployOfficeFloor(String officeFloorName, OfficeFloorBuilder builder,
			CompileContext compileContext) {

		// Initiate the OfficeFloor builder with compiler details
		this.context.initiateOfficeFloorBuilder(builder);

		// Register the OfficeFloor source for possible MBean
		compileContext.registerPossibleMBean(OfficeFloorSource.class, officeFloorName, this.officeFloorSource);

		// Build the executive
		if (this.executive != null) {
			this.executive.buildExecutive(builder, compileContext);
		}

		// Build the teams (in deterministic order)
		this.teams.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeFloorTeamName(), b.getOfficeFloorTeamName()))
				.forEachOrdered((team) -> team.buildTeam(builder, compileContext));

		// Build the offices (in deterministic order)
		Map<OfficeNode, OfficeBindings> officeBindings = new HashMap<OfficeNode, OfficeBindings>();
		this.offices.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getDeployedOfficeName(), b.getDeployedOfficeName()))
				.forEachOrdered((office) -> {

					// Obtain possible profiler to office
					String officeName = office.getDeployedOfficeName();
					Profiler profiler = this.profilers.get(officeName);

					// Build the office
					OfficeBindings bindings = office.buildOffice(builder, compileContext, profiler);

					// Keep track of the offices
					officeBindings.put(office, bindings);
				});

		// Obtains the Office bindings for the managed object source
		Function<ManagedObjectSourceNode, OfficeBindings> getOfficeBindings = (managedObjectSource) -> {
			OfficeNode managingOffice = managedObjectSource.getManagingOfficeNode();
			return officeBindings.get(managingOffice);
		};

		// Build the managed object sources (in deterministic order)
		this.managedObjectSources.values().stream().sorted((a, b) -> CompileUtil
				.sortCompare(a.getOfficeManagedObjectSourceName(), b.getOfficeManagedObjectSourceName()))
				.forEachOrdered((managedObjectSource) -> {

					// Obtain the managing office for managed object
					OfficeBindings bindings = getOfficeBindings.apply(managedObjectSource);
					if (bindings == null) {
						return; // must have office
					}

					// Build the managed object source into the office
					bindings.buildManagedObjectSourceIntoOffice(managedObjectSource);
				});

		// Build the managed objects (in deterministic order)
		this.managedObjects.values().stream().sorted((a, b) -> CompileUtil
				.sortCompare(a.getOfficeFloorManagedObjectName(), b.getOfficeFloorManagedObjectName()))
				.forEachOrdered((managedObject) -> {
					// Obtain the managed object source
					ManagedObjectSourceNode managedObjectSource = managedObject.getManagedObjectSourceNode();

					// Obtain the managing office for managed object
					OfficeBindings bindings = getOfficeBindings.apply(managedObjectSource);
					if (bindings == null) {
						return; // must have office
					}

					// Build the managed object into the office
					bindings.buildManagedObjectIntoOffice(managedObject);
				});

		// Build the input managed objects (in deterministic order)
		this.inputManagedObjects.values().stream().sorted((a, b) -> CompileUtil
				.sortCompare(a.getOfficeFloorInputManagedObjectName(), b.getOfficeFloorInputManagedObjectName()))
				.forEachOrdered((inputManagedObject) -> {
					// Obtain the managed object source
					ManagedObjectSourceNode managedObjectSource = inputManagedObject.getBoundManagedObjectSourceNode();
					if (managedObjectSource == null) {
						return; // must have managed object source
					}

					// Obtain the managing office for managed object
					OfficeBindings bindings = getOfficeBindings.apply(managedObjectSource);
					if (bindings == null) {
						return; // must have office
					}

					// Build the input managed object into the office
					bindings.buildInputManagedObjectIntoOffice(inputManagedObject);
				});

		/*
		 * Suppliers only to provide managed objects at OfficeFloor level. There is no
		 * threading to be built for the supplier at this level.
		 */

		// Return the built OfficeFloor
		return builder.buildOfficeFloor(new CompilerOfficeFloorIssues());
	}

	@Override
	public void loadExternalServicing(OfficeFloor officeFloor) throws UnknownOfficeException, UnknownFunctionException {

		// Load external servicing for each office
		OfficeNode[] officeNodes = this.offices.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getDeployedOfficeName(), b.getDeployedOfficeName()))
				.toArray(OfficeNode[]::new);
		for (OfficeNode officeNode : officeNodes) {

			// Obtain the office
			String officeName = officeNode.getDeployedOfficeName();
			Office office = officeFloor.getOffice(officeName);

			// Load external servicing
			officeNode.loadExternalServicing(office);
		}
	}

	@Override
	public InternalSupplier[] getInternalSuppliers() {

		// Obtain the OfficeFloor internal suppliers
		InternalSupplier[] internalSuppliers = this.suppliers.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeSupplierName(), b.getOfficeSupplierName()))
				.flatMap(supplier -> Arrays.stream(supplier.getInternalSuppliers())).toArray(InternalSupplier[]::new);

		// REturn the internal suppliers
		return internalSuppliers;
	}

	/**
	 * Compiler {@link OfficeFloorIssues}.
	 */
	private class CompilerOfficeFloorIssues implements OfficeFloorIssues {

		/*
		 * ================ OfficeFloorIssues ==============================
		 */

		@Override
		public void addIssue(AssetType assetType, String assetName, String issueDescription) {
			OfficeFloorNodeImpl.this.context.getCompilerIssues().addIssue(OfficeFloorNodeImpl.this, issueDescription);
		}

		@Override
		public void addIssue(AssetType assetType, String assetName, String issueDescription, Throwable cause) {
			OfficeFloorNodeImpl.this.context.getCompilerIssues().addIssue(OfficeFloorNodeImpl.this, issueDescription,
					cause);
		}
	}

}
