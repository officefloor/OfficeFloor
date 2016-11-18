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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.supplier.SuppliedManagedObject;
import net.officefloor.autowire.supplier.SuppliedManagedObjectTeam;
import net.officefloor.compile.impl.section.OfficeSectionManagedObjectSourceTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
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
import net.officefloor.compile.internal.structure.SuppliedManagedObjectNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.internal.structure.WorkNode;
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
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.TeamBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.construct.office.OfficeBuilderImpl;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

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
		public InitialisedState(String managedObjectSourceClassName,
				ManagedObjectSource managedObjectSource) {
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
	 * {@link SuppliedManagedObjectNode} should this be a supplied
	 * {@link ManagedObjectSource}. Will be <code>null</code> if not supplied.
	 */
	private final SuppliedManagedObjectNode suppliedManagedObjectNode;

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
	 * {@link ManagedObjectNode} instances by their {@link ManagedObject} name.
	 */
	private final Map<String, ManagedObjectNode> managedObjects = new HashMap<String, ManagedObjectNode>();

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
	 *            {@link SuppliedManagedObjectNode} containing this
	 *            {@link ManagedObjectSource}. <code>null</code> if not provided
	 *            from {@link SupplierSource}.
	 * @param suppliedManagedObjectNode
	 *            {@link SuppliedManagedObjectNode} should this be a supplied
	 *            {@link ManagedObjectSource}. Will be <code>null</code> if not
	 *            supplied.
	 * @param containingOfficeFloorNode
	 *            {@link OfficeFloorNode} containing this
	 *            {@link ManagedObjectSourceNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedObjectSourceNodeImpl(String managedObjectSourceName,
			SectionNode containingSectionNode, OfficeNode containingOfficeNode,
			SuppliedManagedObjectNode suppliedManagedObjectNode,
			OfficeFloorNode containingOfficeFloorNode, NodeContext context) {
		this.managedObjectSourceName = managedObjectSourceName;
		this.containingSectionNode = containingSectionNode;
		this.containingOfficeNode = containingOfficeNode;
		this.suppliedManagedObjectNode = suppliedManagedObjectNode;
		this.containingOfficeFloorNode = containingOfficeFloorNode;
		this.context = context;

		// Specify the registry
		this.managedObjectRegistry = (this.containingSectionNode != null ? this.containingSectionNode
				: (this.containingOfficeNode != null ? this.containingOfficeNode
						: this.containingOfficeFloorNode));

		// Create the additional objects
		this.propertyList = this.context.createPropertyList();
		this.managingOffice = this.context.createManagingOfficeNode(this);
	}

	/**
	 * Adds a {@link ManagedObjectNode}.
	 * 
	 * @param managedObjectName
	 *            Name of the {@link ManagedObjectNode}.
	 * @param managedObjectScope
	 *            {@link ManagedObjectScope} for the {@link ManagedObject}.
	 * @return Added {@link ManagedObjectNode}.
	 */
	private ManagedObjectNode addManagedObject(String managedObjectName,
			ManagedObjectScope managedObjectScope) {
		ManagedObjectNode managedObject = this.managedObjectRegistry
				.getOrCreateManagedObjectNode(managedObjectName,
						managedObjectScope, this);
		this.managedObjects.put(managedObjectName, managedObject);
		return managedObject;
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
				: (this.containingOfficeNode != null ? this.containingOfficeNode
						: (this.suppliedManagedObjectNode != null ? this.suppliedManagedObjectNode
								: this.containingOfficeFloorNode)));
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);

	}

	@Override
	public void initialise(String managedObjectSourceClassName,
			ManagedObjectSource<?, ?> managedObjectSource) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(managedObjectSourceClassName,
						managedObjectSource));
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
	public boolean hasManagedObjectSource() {
		return (this.suppliedManagedObjectNode != null)
				|| (this.state.managedObjectSource != null)
				|| (!CompileUtil
						.isBlank(this.state.managedObjectSourceClassName));
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedObjectType<?> loadManagedObjectType() {

		// Determine if supplied
		if (this.suppliedManagedObjectNode != null) {
			// Obtain and return the supplied managed object type
			SuppliedManagedObject<?, ?> suppliedManagedObject = this.suppliedManagedObjectNode
					.loadSuppliedManagedObject();
			return suppliedManagedObject.getManagedObjectType();

		} else {

			// Create the loader to obtain the managed object type
			ManagedObjectLoader loader = this.context
					.getManagedObjectLoader(this);

			// Load the managed object type
			if (this.state.managedObjectSource != null) {
				// Load and return the managed object type from instance
				return loader.loadManagedObjectType(
						this.state.managedObjectSource, this.propertyList);

			} else {
				// Obtain the managed object source class
				Class managedObjectSourceClass = this.context
						.getManagedObjectSourceClass(
								this.state.managedObjectSourceClassName, this);
				if (managedObjectSourceClass == null) {
					return null; // must have managed object source class
				}

				// Load and return the managed object type from class
				return loader.loadManagedObjectType(managedObjectSourceClass,
						this.propertyList);
			}
		}
	}

	@Override
	public OfficeSectionManagedObjectSourceType loadOfficeSectionManagedObjectSourceType(
			TypeContext typeContext) {

		// Load the managed object type
		ManagedObjectType<?> managedObjectType = typeContext
				.getOrLoadManagedObjectType(this);
		if (managedObjectType == null) {
			return null;
		}

		// Load the managed object types
		OfficeSectionManagedObjectType[] managedObjectTypes = CompileUtil
				.loadTypes(
						this.managedObjects,
						(managedObject) -> managedObject
								.getOfficeSectionManagedObjectName(),
						(managedObject) -> managedObject
								.loadOfficeSectionManagedObjectType(typeContext),
						OfficeSectionManagedObjectType[]::new);
		if (managedObjectTypes == null) {
			return null;
		}

		// Load the teams types
		OfficeSectionManagedObjectTeamType[] teamTypes = CompileUtil.loadTypes(
				this.teams, (team) -> team.getManagedObjectTeamName(),
				(team) -> team
						.loadOfficeSectionManagedObjectTeamType(typeContext),
				OfficeSectionManagedObjectTeamType[]::new);
		if (teamTypes == null) {
			return null;
		}

		// Create and return the type
		return new OfficeSectionManagedObjectSourceTypeImpl(
				this.managedObjectSourceName, teamTypes, managedObjectTypes);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public OfficeFloorManagedObjectSourceType loadOfficeFloorManagedObjectSourceType(
			TypeContext typeContext) {

		// Ensure have the managed object source name
		if (CompileUtil.isBlank(this.managedObjectSourceName)) {
			this.context.getCompilerIssues().addIssue(this,
					"Null name for " + TYPE);
			return null; // must have name
		}

		// Ensure have the managed object source
		if (!this.hasManagedObjectSource()) {
			this.context.getCompilerIssues().addIssue(
					this,
					"Null source for " + TYPE + " "
							+ this.managedObjectSourceName);
			return null; // must have source
		}

		// Create the loader to obtain the managed object type
		ManagedObjectLoader loader = this.context.getManagedObjectLoader(this);

		// Load the managed object type
		if (this.state.managedObjectSource != null) {
			// Load and return the managed object type from instance
			return loader.loadOfficeFloorManagedObjectSourceType(
					this.managedObjectSourceName,
					this.state.managedObjectSource, this.propertyList);

		} else {
			// Obtain the managed object source class
			Class managedObjectSourceClass = this.context
					.getManagedObjectSourceClass(
							this.state.managedObjectSourceClassName, this);
			if (managedObjectSourceClass == null) {
				return null; // must have managed object source class
			}

			// Load and return the managed object type from class
			return loader.loadOfficeFloorManagedObjectSourceType(
					this.managedObjectSourceName, managedObjectSourceClass,
					this.propertyList);
		}
	}

	@Override
	public String getManagedObjectSourceName() {
		// Obtain the name based on location
		if (this.containingSectionNode != null) {
			// Use name qualified with both office and section
			return this.containingOfficeNode.getDeployedOfficeName()
					+ "."
					+ this.containingSectionNode
							.getSectionQualifiedName(this.managedObjectSourceName);

		} else if (this.containingOfficeNode != null) {
			// Use name qualified with office name
			return this.containingOfficeNode.getDeployedOfficeName() + "."
					+ this.managedObjectSourceName;

		} else {
			// Use name unqualified
			return this.managedObjectSourceName;
		}
	}

	@Override
	public OfficeNode getManagingOfficeNode() {

		// Obtain the managing office
		OfficeNode managingOffice = LinkUtil.retrieveTarget(
				this.managingOffice, OfficeNode.class,
				this.context.getCompilerIssues());

		// Return the managing office
		return managingOffice;
	}

	@Override
	public boolean linkInputManagedObjectNode(
			InputManagedObjectNode inputManagedObject) {

		// Determine if already linked
		if (this.inputManagedObjectNode != null) {
			// Already linked
			this.context.getCompilerIssues().addIssue(
					this,
					"Managed object source " + this.managedObjectSourceName
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
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void buildManagedObject(OfficeFloorBuilder builder,
			OfficeNode managingOffice, OfficeBuilder officeBuilder,
			OfficeBindings officeBindings, TypeContext typeContext) {

		// Obtain the name to add this managed object source
		final String managedObjectSourceName = this
				.getManagedObjectSourceName();

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = typeContext
				.getOrLoadManagedObjectType(this);
		if (managedObjectType == null) {
			return; // must have managed object type
		}

		// Ensure all the teams are made available
		for (ManagedObjectTeamType teamType : managedObjectType.getTeamTypes()) {
			// Obtain team (ensures any missing teams are added)
			this.getManagedObjectTeam(teamType.getTeamName());
		}

		// Obtain the Managed Object Builder
		ManagedObjectBuilder moBuilder;
		Set<String> suppliedTeamNames = new HashSet<String>();
		if (this.suppliedManagedObjectNode != null) {
			// Obtain the supplied managed object for managed object builder
			SuppliedManagedObject<?, ?> suppliedManagedObject = this.suppliedManagedObjectNode
					.loadSuppliedManagedObject();
			if (suppliedManagedObject == null) {
				// Must have supplied managed object
				this.context.getCompilerIssues().addIssue(
						this,
						"No " + SuppliedManagedObject.class.getSimpleName()
								+ " available for managed object source "
								+ managedObjectSourceName);
				return; // must have supplied managed object
			}

			// Build the managed object source from supplied managed object
			moBuilder = builder.addManagedObject(managedObjectSourceName,
					suppliedManagedObject.getManagedObjectSource());

			// Provide properties from supplied managed object
			for (Property property : suppliedManagedObject.getProperties()) {
				moBuilder.addProperty(property.getName(), property.getValue());
			}

			// Provide timeout from supplied managed object
			moBuilder.setTimeout(suppliedManagedObject.getTimeout());

			// Add the necessary supplied teams
			for (SuppliedManagedObjectTeam suppliedTeam : suppliedManagedObject
					.getSuppliedTeams()) {

				// Obtain the team name and flag that being supplied
				String teamName = suppliedTeam.getTeamName();
				suppliedTeamNames.add(teamName);

				// Obtain the qualified team name
				String qualifiedTeamName = managedObjectSourceName + "-"
						+ teamName;

				// Obtain the team source class
				Class teamSourceClass = this.context.getTeamSourceClass(
						suppliedTeam.getTeamSourceClassName(), this);
				if (teamSourceClass == null) {
					continue; // must have team source class
				}

				// Build the supplied team
				TeamBuilder<?> teamBuilder = builder.addTeam(qualifiedTeamName,
						teamSourceClass);
				for (Property property : suppliedTeam.getProperties()) {
					teamBuilder.addProperty(property.getName(),
							property.getValue());
				}

				// Register the supplied team to the office
				officeBuilder
						.registerTeam(qualifiedTeamName, qualifiedTeamName);
			}

		} else if (this.state.managedObjectSource != null) {
			// Build the managed object source from instance
			moBuilder = builder.addManagedObject(managedObjectSourceName,
					this.state.managedObjectSource);

		} else {
			// No instance, so obtain by managed object source class
			Class managedObjectSourceClass = this.context
					.getManagedObjectSourceClass(
							this.state.managedObjectSourceClassName, this);
			if (managedObjectSourceClass == null) {
				return; // must have managed object source class
			}

			// Build the managed object source from class
			moBuilder = builder.addManagedObject(managedObjectSourceName,
					managedObjectSourceClass);
		}

		// Add properties for Managed Object Source
		for (Property property : this.propertyList) {
			moBuilder.addProperty(property.getName(), property.getValue());
		}

		// Provide timeout (only if override potential supplied timeout)
		if (this.timeout > 0) {
			moBuilder.setTimeout(this.timeout);
		}

		// Specify the managing office
		ManagingOfficeBuilder managingOfficeBuilder = moBuilder
				.setManagingOffice(managingOffice.getDeployedOfficeName());

		// Obtain the flow types and team types
		ManagedObjectFlowType<?>[] flowTypes = managedObjectType.getFlowTypes();
		ManagedObjectTeamType[] teamTypes = managedObjectType.getTeamTypes();

		// Provide process bound name if input managed object
		if ((flowTypes.length > 0) || (teamTypes.length > 0)) {

			// Ensure have Input ManagedObject name
			String inputBoundManagedObjectName = null;
			if ((this.containingSectionNode != null)
					|| (this.containingOfficeNode != null)) {
				// Can not link to managed object initiating flow.
				// Use name of managed object source.
				inputBoundManagedObjectName = managedObjectSourceName;

			} else {
				// Determine if require configuring as Input Managed Object
				ManagedObjectLoader loader = this.context
						.getManagedObjectLoader(this);
				if (loader.isInputManagedObject(managedObjectType)) {
					// Must configure as Input Managed Object (as shared)
					if (this.inputManagedObjectNode != null) {
						inputBoundManagedObjectName = this.inputManagedObjectNode
								.getBoundManagedObjectName();
					}
				} else {
					// Never shared (tasks specific to Managed Object Source)
					inputBoundManagedObjectName = managedObjectSourceName;
				}
			}

			if (CompileUtil.isBlank(inputBoundManagedObjectName)) {
				// Provide issue as should be input
				this.context.getCompilerIssues().addIssue(
						this,
						"Must provide input managed object for managed object source "
								+ this.managedObjectSourceName
								+ " as managed object source has flows/teams");
			} else {
				// Bind the managed object to process state of managing office
				DependencyMappingBuilder inputDependencyMappings = managingOfficeBuilder
						.setInputManagedObjectName(inputBoundManagedObjectName);

				// Provide governance for office floor input managed object.
				// For others, should be configured through Office (for flows).
				if (this.inputManagedObjectNode != null) {
					// Get governances for input managed object of office
					GovernanceNode[] governances = this.inputManagedObjectNode
							.getGovernances(managingOffice);

					// Map in the governances
					for (GovernanceNode governance : governances) {
						inputDependencyMappings.mapGovernance(governance
								.getOfficeGovernanceName());
					}
				}

				// Determine if dependencies
				ManagedObjectDependencyType<?>[] dependencyTypes = managedObjectType
						.getDependencyTypes();
				if (dependencyTypes.length > 0) {

					// Load the dependencies for the input managed object
					for (ManagedObjectDependencyType<?> dependencyType : managedObjectType
							.getDependencyTypes()) {

						// Obtain the dependency type details
						String dependencyName = dependencyType
								.getDependencyName();
						Enum dependencyKey = dependencyType.getKey();
						int dependencyIndex = dependencyType.getIndex();

						// Obtain the dependency
						ManagedObjectDependencyNode dependencyNode = this.inputDependencies
								.get(dependencyName);
						BoundManagedObjectNode dependency = LinkUtil
								.retrieveTarget(dependencyNode,
										BoundManagedObjectNode.class,
										this.context.getCompilerIssues());
						if (dependency == null) {
							continue; // must have dependency
						}

						// Ensure dependent managed object is built into office
						officeBindings.buildManagedObjectIntoOffice(dependency);

						// Link the dependency
						String dependentManagedObjectName = dependency
								.getBoundManagedObjectName();
						if (dependencyKey != null) {
							inputDependencyMappings.mapDependency(
									dependencyKey, dependentManagedObjectName);
						} else {
							inputDependencyMappings
									.mapDependency(dependencyIndex,
											dependentManagedObjectName);
						}
					}
				}
			}
		}

		// Link in the flows for the managed object source
		for (final ManagedObjectFlowType<?> flowType : flowTypes) {

			// Obtain the flow type details
			String flowName = flowType.getFlowName();
			Enum<?> flowKey = flowType.getKey();
			int flowIndex = flowType.getIndex();

			// Obtain the task for the flow
			ManagedObjectFlowNode flowNode = this.flows.get(flowName);
			TaskNode taskNode = LinkUtil.retrieveTarget(flowNode,
					TaskNode.class, this.context.getCompilerIssues());
			if (taskNode == null) {
				continue; // must have task node
			}

			// Ensure the task is contained in the managing office
			WorkNode workNode = taskNode.getWorkNode();
			SectionNode section = workNode.getSectionNode();
			OfficeNode taskOffice = section.getOfficeNode();
			if (taskOffice != managingOffice) {
				this.context.getCompilerIssues().addIssue(
						this,
						"Linked task of flow " + flowName
								+ " from managed object source "
								+ this.managedObjectSourceName
								+ " must be within the managing office");
				continue; // task must be within managing office
			}

			// Obtain the details of task to link flow
			final String workName = workNode.getQualifiedWorkName();
			final String taskName = taskNode.getOfficeTaskName();

			// Determine if flow from task
			String flowTaskName = flowType.getTaskName();
			if (CompileUtil.isBlank(flowTaskName)) {
				// Link the flow directly from managed object source to the task
				if (flowKey != null) {
					managingOfficeBuilder.linkProcess(flowKey, workName,
							taskName);
				} else {
					managingOfficeBuilder.linkProcess(flowIndex, workName,
							taskName);
				}

			} else {

				// TODO test office enhancing for managed object source

				// Link flow from task to its task
				officeBuilder.addOfficeEnhancer(new OfficeEnhancer() {
					@Override
					public void enhanceOffice(OfficeEnhancerContext context) {

						// Obtain the flow builder for the task
						String flowWorkName = OfficeBuilderImpl
								.getNamespacedName(managedObjectSourceName,
										flowType.getWorkName());
						String flowTaskName = flowType.getTaskName();
						FlowNodeBuilder flowBuilder = context
								.getFlowNodeBuilder(flowWorkName, flowTaskName);

						// Link in the flow
						Enum<?> flowKey = flowType.getKey();
						Class<?> argumentType = flowType.getArgumentType();
						if (flowKey != null) {
							flowBuilder.linkFlow(flowKey, workName, taskName,
									FlowInstigationStrategyEnum.SEQUENTIAL,
									argumentType);
						} else {
							int flowIndex = flowType.getIndex();
							flowBuilder.linkFlow(flowIndex, workName, taskName,
									FlowInstigationStrategyEnum.SEQUENTIAL,
									argumentType);
						}
					}
				});
			}
		}

		// Link in the teams for the managed object source
		for (ManagedObjectTeamType teamType : teamTypes) {

			// Obtain the team type details
			String teamName = teamType.getTeamName();

			// Ignore if already supplied
			if (suppliedTeamNames.contains(teamName)) {
				continue; // team supplied
			}

			// Obtain the team
			ManagedObjectTeamNode managedObjectTeam = this.teams.get(teamName);
			TeamNode team = LinkUtil.retrieveTarget(managedObjectTeam,
					TeamNode.class, this.context.getCompilerIssues());
			if (team == null) {
				continue; // must have the team
			}

			// Register the team to the office
			String officeTeamName = managedObjectSourceName + "." + teamName;
			officeBuilder.registerTeam(officeTeamName,
					team.getOfficeFloorTeamName());
		}

		// Build the managed objects from this (in deterministic order)
		this.managedObjects
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getBoundManagedObjectName(),
						b.getBoundManagedObjectName()))
				.forEach(
						(managedObject) -> officeBindings
								.buildManagedObjectIntoOffice(managedObject));
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
	public ManagedObjectFlow getManagedObjectFlow(
			String managedObjectSourceFlowName) {
		return NodeUtil.getNode(managedObjectSourceFlowName, this.flows,
				() -> this.context.createManagedObjectFlowNode(
						managedObjectSourceFlowName, this));
	}

	@Override
	public ManagedObjectTeam getManagedObjectTeam(
			String managedObjectSourceTeamName) {
		return NodeUtil.getNode(managedObjectSourceTeamName, this.teams,
				() -> this.context.createManagedObjectTeamNode(
						managedObjectSourceTeamName, this));
	}

	@Override
	public ManagedObjectDependency getInputManagedObjectDependency(
			String managedObjectDependencyName) {
		return NodeUtil.getNode(managedObjectDependencyName,
				this.inputDependencies, () -> this.context
						.createManagedObjectDependencyNode(
								managedObjectDependencyName, this));
	}

	/*
	 * ==================== SectionManagedObjectSource =========================
	 */

	@Override
	public String getSectionManagedObjectSourceName() {
		return (this.containingSectionNode != null ? this.managedObjectSourceName
				: null);
	}

	@Override
	public SectionManagedObject addSectionManagedObject(
			String managedObjectName, ManagedObjectScope managedObjectScope) {
		return this.addManagedObject(managedObjectName, managedObjectScope);
	}

	/*
	 * ============== OfficeSectionManagedObjectSource ========================
	 */

	@Override
	public String getOfficeSectionManagedObjectSourceName() {
		return (this.containingSectionNode != null ? this.managedObjectSourceName
				: null);
	}

	@Override
	public ManagedObjectTeam getOfficeSectionManagedObjectTeam(String teamName) {
		return NodeUtil.getNode(teamName, this.teams,
				() -> this.context.createManagedObjectTeamNode(teamName, this));
	}

	@Override
	public OfficeSectionManagedObject getOfficeSectionManagedObject(
			String managedObjectName) {
		return NodeUtil.getNode(managedObjectName, this.managedObjects,
				() -> this.context.createManagedObjectNode(managedObjectName,
						this));
	}

	/*
	 * ================== OfficeManagedObjectSource ============================
	 */

	@Override
	public String getOfficeManagedObjectSourceName() {
		return (this.containingOfficeNode != null ? this.managedObjectSourceName
				: null);
	}

	@Override
	public OfficeManagedObject addOfficeManagedObject(String managedObjectName,
			ManagedObjectScope managedObjectScope) {
		return this.addManagedObject(managedObjectName, managedObjectScope);
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
	public OfficeFloorManagedObject addOfficeFloorManagedObject(
			String managedObjectName, ManagedObjectScope managedObjectScope) {
		return this.addManagedObject(managedObjectName, managedObjectScope);
	}

}