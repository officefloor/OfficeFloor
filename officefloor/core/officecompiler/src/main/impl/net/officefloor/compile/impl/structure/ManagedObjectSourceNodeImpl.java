/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.structure;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.section.OfficeSectionManagedObjectSourceTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireLink;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.FunctionNamespaceNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkPoolNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedFunctionNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectPoolNode;
import net.officefloor.compile.internal.structure.ManagedObjectRegistry;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagedObjectTeamNode;
import net.officefloor.compile.internal.structure.ManagingOfficeNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeBindings;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.officefloor.OfficeFloorManagedObjectSourceType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.section.OfficeSectionManagedObjectSourceType;
import net.officefloor.compile.section.OfficeSectionManagedObjectTeamType;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.managedobject.ManagedObjectFlow;
import net.officefloor.compile.spi.managedobject.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
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
		 * {@link ManagedObjectSource} instance to use. If this is specified its
		 * use overrides the {@link Class}. Will be <code>null</code> if not to
		 * override.
		 */
		@SuppressWarnings("rawtypes")
		private final ManagedObjectSource managedObjectSource;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectSourceClassName
		 *            {@link ManagedObjectSource} {@link Class} name.
		 * @param managedObjectSource
		 *            {@link ManagedObjectSource}.
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
	 * {@link ManagedObjectFlowNode} instances by their
	 * {@link ManagedObjectFlow} names.
	 */
	private final Map<String, ManagedObjectFlowNode> flows = new HashMap<String, ManagedObjectFlowNode>();

	/**
	 * {@link ManagedObjectTeamNode} instances by their
	 * {@link ManagedObjectTeam} names.
	 */
	private final Map<String, ManagedObjectTeamNode> teams = new HashMap<String, ManagedObjectTeamNode>();

	/**
	 * {@link ManagedObjectDependencyNode} instances by their
	 * {@link ManagedObjectDependency} names. This is for the Input
	 * {@link ManagedObject} dependencies.
	 */
	private final Map<String, ManagedObjectDependencyNode> inputDependencies = new HashMap<String, ManagedObjectDependencyNode>();

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceName
	 *            Name of this {@link ManagedObjectSource}.
	 * @param containingSectionNode
	 *            {@link SectionNode} containing this
	 *            {@link ManagedObjectSourceNode}. <code>null</code> if
	 *            contained in the {@link Office} or {@link OfficeFloor}.
	 * @param containingOfficeNode
	 *            {@link OfficeNode} containing this
	 *            {@link ManagedObjectSourceNode}. <code>null</code> if
	 *            contained in the {@link OfficeFloor}.
	 * @param containingSuppliedManagedObjectNode
	 *            {@link SuppliedManagedObjectSourceNode} containing this
	 *            {@link ManagedObjectSource}. <code>null</code> if not provided
	 *            from {@link SupplierSource}.
	 * @param containingSuppliedManagedObjectNode
	 *            {@link SuppliedManagedObjectSourceNode} should this be a
	 *            supplied {@link ManagedObjectSource}. Will be
	 *            <code>null</code> if not supplied.
	 * @param containingOfficeFloorNode
	 *            {@link OfficeFloorNode} containing this
	 *            {@link ManagedObjectSourceNode}.
	 * @param context
	 *            {@link NodeContext}.
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
		 * @param managedObjectSource
		 *            {@link ManagedObjectSource}.
		 * @param properties
		 *            {@link PropertyList} to configure the
		 *            {@link ManagedObjectSource}.
		 * @param managedObjectLoader
		 *            {@link ManagedObjectLoader}.
		 * @return Specific type.
		 */
		T loadType(ManagedObjectSource<?, ?> managedObjectSource, PropertyList properties,
				ManagedObjectLoader managedObjectLoader);
	}

	/**
	 * Loads the type.
	 * 
	 * @param compileContext
	 *            {@link CompileContext}.
	 * @param loader
	 *            {@link TypeLoader} to load specific type.
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
	 * @param seedProperties
	 *            Seed {@link PropertyList}. May be <code>null</code> if no seed
	 *            {@link PropertyList}.
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
		return this.context.overrideProperties(this, this.getManagedObjectSourceName(), properties);
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
				: (this.suppliedManagedObjectNode != null ? this.suppliedManagedObjectNode
						: (this.containingOfficeNode != null ? this.containingOfficeNode
								: this.containingOfficeFloorNode)));
	}

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes(this.flows, this.teams, this.inputDependencies);
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
	public boolean sourceManagedObjectSource(CompileContext compileContext) {

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

		// Initialise the input dependencies
		for (ManagedObjectDependencyType<?> dependencyType : managedObjectType.getDependencyTypes()) {
			String dependencyName = dependencyType.getDependencyName();
			NodeUtil.getInitialisedNode(dependencyName, this.inputDependencies, this.context,
					() -> this.context.createManagedObjectDependencyNode(dependencyName, this),
					(dependency) -> dependency.initialise());
		}

		// Successfully sourced
		return true;
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
	public String getManagedObjectSourceName() {
		// Obtain the name based on location
		if (this.containingSectionNode != null) {
			// Use name qualified with both office and section
			return this.containingSectionNode.getQualifiedName(this.managedObjectSourceName);

		} else if (this.containingOfficeNode != null) {
			// Use name qualified with office name
			return this.containingOfficeNode.getQualifiedName(this.managedObjectSourceName);

		} else {
			// Use name unqualified
			return this.managedObjectSourceName;
		}
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
	public void autoWireTeams(AutoWirer<LinkTeamNode> autoWirer, CompileContext compileContext) {

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = compileContext.getOrLoadManagedObjectType(this);
		if (managedObjectType == null) {
			return; // must have managed object type
		}

		// Obtain the managing office
		OfficeNode managingOffice = (this.containingOfficeNode != null ? this.containingOfficeNode
				: this.getManagingOfficeNode());

		// Obtain the object type
		String objectType = managedObjectType.getObjectClass().getName();

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
			AutoWireLink<LinkTeamNode>[] links = autoWirer.findAutoWireLinks(teamNode, sourceAutoWire);
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

			// Add properties for Managed Object Source
			for (Property property : properties) {
				moBuilder.addProperty(property.getName(), property.getValue());
			}

			// Provide timeout
			moBuilder.setTimeout(this.timeout);

			// Specify the managing office
			ManagingOfficeBuilder managingOfficeBuilder = moBuilder
					.setManagingOffice(managingOffice.getDeployedOfficeName());

			// Provide process bound name if input managed object
			if (managedObjectType.isInput()) {

				// Ensure have Input ManagedObject name
				String inputBoundManagedObjectName = null;
				if ((this.containingSectionNode != null) || (this.containingOfficeNode != null)) {
					// Can not link to managed object initiating flow.
					// Use name of managed object source.
					inputBoundManagedObjectName = managedObjectSourceName;

				} else {
					// Must configure as Input Managed Object (as shared)
					if (this.inputManagedObjectNode != null) {
						inputBoundManagedObjectName = this.inputManagedObjectNode.getBoundManagedObjectName();
					}
				}

				if (CompileUtil.isBlank(inputBoundManagedObjectName)) {
					// Provide issue as should be input
					this.context.getCompilerIssues().addIssue(this,
							"Must provide input managed object for managed object source "
									+ this.managedObjectSourceName + " as managed object source has flows/teams");
				} else {
					// Bind managed object to process state of managing office
					DependencyMappingBuilder inputDependencyMappings = managingOfficeBuilder
							.setInputManagedObjectName(inputBoundManagedObjectName);

					// Provide governance for office floor input managed object.
					// For others, should be configured through Office (for
					// flows).
					if (this.inputManagedObjectNode != null) {
						// Get governances for input managed object of office
						GovernanceNode[] governances = this.inputManagedObjectNode.getGovernances(managingOffice);

						// Map in the governances
						for (GovernanceNode governance : governances) {
							inputDependencyMappings.mapGovernance(governance.getOfficeGovernanceName());
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
				}
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
					continue; // must have task node
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
				String officeTeamName = managedObjectSourceName + "." + teamName;
				officeBuilder.registerTeam(officeTeamName, team.getOfficeFloorTeamName());
			}

			// Determine if pool the managed object
			ManagedObjectPoolNode poolNode = LinkUtil.findTarget(this, ManagedObjectPoolNode.class,
					this.context.getCompilerIssues());
			if (poolNode != null) {
				poolNode.buildManagedObjectPool(moBuilder, managedObjectType, compileContext);
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
			this.getManagedObjectTeam(teamType.getTeamName());
		}
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

	@Override
	public ManagedObjectFlow getManagedObjectFlow(String managedObjectSourceFlowName) {
		return NodeUtil.getNode(managedObjectSourceFlowName, this.flows,
				() -> this.context.createManagedObjectFlowNode(managedObjectSourceFlowName, this));
	}

	@Override
	public ManagedObjectTeam getManagedObjectTeam(String managedObjectSourceTeamName) {
		return NodeUtil.getNode(managedObjectSourceTeamName, this.teams,
				() -> this.context.createManagedObjectTeamNode(managedObjectSourceTeamName, this));
	}

	@Override
	public ManagedObjectDependency getInputManagedObjectDependency(String managedObjectDependencyName) {
		return NodeUtil.getNode(managedObjectDependencyName, this.inputDependencies,
				() -> this.context.createManagedObjectDependencyNode(managedObjectDependencyName, this));
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

	/*
	 * ============== OfficeSectionManagedObjectSource ========================
	 */

	@Override
	public String getOfficeSectionManagedObjectSourceName() {
		return (this.containingSectionNode != null ? this.managedObjectSourceName : null);
	}

	@Override
	public ManagedObjectTeam getOfficeSectionManagedObjectTeam(String teamName) {
		return NodeUtil.getNode(teamName, this.teams, () -> this.context.createManagedObjectTeamNode(teamName, this));
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

}