/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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

import net.officefloor.compile.impl.managedobject.ManagedObjectLoaderImpl;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.impl.util.StringExtractor;
import net.officefloor.compile.internal.structure.InputManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagingOfficeNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.TaskNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.internal.structure.WorkNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.FlowNodeBuilder;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeEnhancer;
import net.officefloor.frame.api.build.OfficeEnhancerContext;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
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
	 * Class name of the {@link ManagedObjectSource}.
	 */
	private final String managedObjectSourceClassName;

	/**
	 * {@link PropertyList} to load the {@link ManagedObjectSource}.
	 */
	private final PropertyList propertyList = new PropertyListImpl();

	/**
	 * {@link ManagedObjectFlowNode} instances by their
	 * {@link ManagedObjectFlow} names.
	 */
	private final Map<String, ManagedObjectFlowNode> flows = new HashMap<String, ManagedObjectFlowNode>();

	/**
	 * {@link OfficeTeamNode} instances by their {@link ManagedObjectTeam}
	 * names.
	 */
	private final Map<String, OfficeTeamNode> teams = new HashMap<String, OfficeTeamNode>();

	/**
	 * Registry of all {@link ManagedObjectNode} instances within a location.
	 * This allows to check that no other {@link ManagedObjectNode} instances
	 * are added by other {@link ManagedObjectSourceNode} with the same name.
	 */
	private final Map<String, ManagedObjectNode> locationManagedObjects;

	/**
	 * {@link ManagedObjectNode} instances by their {@link ManagedObject} name.
	 */
	private final Map<String, ManagedObjectNode> managedObjects = new HashMap<String, ManagedObjectNode>();

	/**
	 * {@link LocationType} of the location containing this
	 * {@link ManagedObjectSource}.
	 */
	private final LocationType locationType;

	/**
	 * Location containing this {@link ManagedObjectSource}.
	 */
	private final String location;

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
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Indicates if the {@link ManagedObjectType} is loaded.
	 */
	private boolean isManagedObjectTypeLoaded = false;

	/**
	 * Loaded {@link ManagedObjectType}.
	 */
	private ManagedObjectType<?> managedObjectType;

	/**
	 * {@link ManagingOffice}.
	 */
	private ManagingOfficeNode managingOffice;

	/**
	 * {@link InputManagedObjectNode}.
	 */
	private InputManagedObjectNode inputManagedObjectNode = null;

	/**
	 * Flags whether within the {@link Office} context.
	 */
	private boolean isInOfficeContext = false;

	/**
	 * Location of the {@link Office} containing this
	 * {@link OfficeManagedObject}.
	 */
	private String officeLocation;

	/**
	 * Flags whether within the {@link OfficeFloor} context.
	 */
	private boolean isInOfficeFloorContext = false;

	/**
	 * Location of the {@link OfficeFloor} containing this
	 * {@link OfficeFloorManagedObject}.
	 */
	private String officeFloorLocation;

	/**
	 * Initiate.
	 *
	 * @param managedObjectSourceName
	 *            Name of this {@link ManagedObjectSource}.
	 * @param managedObjectSourceClassName
	 *            Class name of the {@link ManagedObjectSource}.
	 * @param locationType
	 *            {@link LocationType} of the location containing this
	 *            {@link ManagedObjectSource}.
	 * @param location
	 *            Location containing this {@link ManagedObjectSource}.
	 * @param containingOfficeNode
	 *            {@link OfficeNode} containing this
	 *            {@link ManagedObjectSourceNode}. <code>null</code> if
	 *            contained in the {@link OfficeFloor}.
	 * @param containingSectionNode
	 *            {@link SectionNode} containing this
	 *            {@link ManagedObjectSourceNode}. <code>null</code> if
	 *            contained in the {@link Office} or {@link OfficeFloor}.
	 * @param locationManagedObjects
	 *            Registry of all {@link ManagedObjectNode} instances within a
	 *            location. This allows to check that no other
	 *            {@link ManagedObjectNode} instances are added by other
	 *            {@link ManagedObjectSourceNode} with the same name.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedObjectSourceNodeImpl(String managedObjectSourceName,
			String managedObjectSourceClassName, LocationType locationType,
			String location, SectionNode containingSectionNode,
			OfficeNode containingOfficeNode,
			Map<String, ManagedObjectNode> locationManagedObjects,
			NodeContext context) {
		this.managedObjectSourceName = managedObjectSourceName;
		this.managedObjectSourceClassName = managedObjectSourceClassName;
		this.locationType = locationType;
		this.location = location;
		this.containingSectionNode = containingSectionNode;
		this.containingOfficeNode = containingOfficeNode;
		this.locationManagedObjects = locationManagedObjects;
		this.context = context;
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
	public ManagedObjectNode addManagedObject(String managedObjectName,
			ManagedObjectScope managedObjectScope) {
		// Obtain and return the managed object for the name
		ManagedObjectNode managedObject = this.locationManagedObjects
				.get(managedObjectName);
		if (managedObject == null) {
			// Add the managed object and register it
			managedObject = new ManagedObjectNodeImpl(managedObjectName,
					managedObjectScope, this.locationType, this.location, this,
					this.containingSectionNode, this.containingOfficeNode,
					this.context);
			this.locationManagedObjects.put(managedObjectName, managedObject);
			this.managedObjects.put(managedObjectName, managedObject);
		} else {
			// Obtain managed object location type description
			String locationTypeDesc;
			switch (this.locationType) {
			case OFFICE_FLOOR:
				locationTypeDesc = "Office floor";
				break;
			case OFFICE:
				locationTypeDesc = "Office";
				break;
			case SECTION:
				locationTypeDesc = "Section";
				break;
			default:
				throw new IllegalStateException("Unknown location type "
						+ this.locationType);
			}

			// Managed object already added
			this.context.getCompilerIssues().addIssue(
					this.locationType,
					this.location,
					AssetType.MANAGED_OBJECT,
					managedObjectName,
					locationTypeDesc + " managed object " + managedObjectName
							+ " already added");
		}
		return managedObject;
	}

	/*
	 * ================ ManagedObjectSourceNode ===============================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public void loadManagedObjectType() {

		// Only load the managed object type once (whether successful or not)
		if (this.isManagedObjectTypeLoaded) {
			return;
		}
		this.isManagedObjectTypeLoaded = true;

		// Obtain the managed object source class
		Class<? extends ManagedObjectSource> managedObjectSourceClass = this.context
				.getManagedObjectSourceClass(this.managedObjectSourceClassName,
						this.locationType, this.location,
						this.managedObjectSourceName);
		if (managedObjectSourceClass == null) {
			return; // must have managed object source class
		}

		// Create the loader to obtain the managed object type
		ManagedObjectLoader loader = new ManagedObjectLoaderImpl(
				this.locationType, this.location, this.managedObjectSourceName,
				this.context);

		// Load the managed object type
		this.managedObjectType = loader.loadManagedObjectType(
				managedObjectSourceClass, this.propertyList);

		// Ensure all the teams are made available
		if (this.managedObjectType != null) {
			for (ManagedObjectTeamType teamType : this.managedObjectType
					.getTeamTypes()) {
				// Obtain team (ensures any missing teams are added)
				this.getManagedObjectTeam(teamType.getTeamName());
			}
		}
	}

	@Override
	public ManagedObjectType<?> getManagedObjectType() {

		// Ensure the managed object type is loaded
		if (!this.isManagedObjectTypeLoaded) {
			throw new IllegalStateException(
					"Managed object type must be loaded");
		}

		// Return the loaded managed object type
		return this.managedObjectType;
	}

	@Override
	public void addOfficeContext(String officeLocation) {
		this.officeLocation = officeLocation;
		this.isInOfficeContext = true;
	}

	@Override
	public void addOfficeFloorContext(String officeFloorLocation) {
		this.officeFloorLocation = officeFloorLocation;

		// Load the managing office
		this.managingOffice = new ManagingOfficeNodeImpl(
				this.managedObjectSourceName, this.officeFloorLocation,
				this.context);

		// Flag all existing teams within office floor context
		for (OfficeTeamNode team : this.teams.values()) {
			team.addOfficeFloorContext(this.officeFloorLocation);
		}

		// Now in office floor context
		this.isInOfficeFloorContext = true;
	}

	@Override
	public String getManagedObjectSourceName() {
		// Obtain the name based on location
		switch (this.locationType) {
		case OFFICE_FLOOR:
			// Use name unqualified
			return this.managedObjectSourceName;

		case OFFICE:
			// Use name qualified with office name
			return this.containingOfficeNode.getDeployedOfficeName() + "."
					+ this.managedObjectSourceName;

		case SECTION:
			// Use name qualified with both office and section
			return this.containingOfficeNode.getDeployedOfficeName()
					+ "."
					+ this.containingSectionNode
							.getSectionQualifiedName(this.managedObjectSourceName);

		default:
			throw new IllegalStateException("Unknown location type");
		}
	}

	@Override
	public OfficeNode getManagingOfficeNode() {

		// Obtain the managing office
		OfficeNode managingOffice = LinkUtil.retrieveTarget(
				this.managingOffice, OfficeNode.class, "Managed Object Source "
						+ this.managedObjectSourceName,
				LocationType.OFFICE_FLOOR, this.officeFloorLocation,
				AssetType.MANAGED_OBJECT, this.managedObjectSourceName,
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
					this.locationType,
					this.location,
					null,
					null,
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
	@SuppressWarnings("unchecked")
	public void buildManagedObject(OfficeFloorBuilder builder,
			OfficeNode managingOffice, OfficeBuilder officeBuilder) {

		// Obtain the name to add this managed object source
		final String managedObjectSourceName = this
				.getManagedObjectSourceName();

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = this.getManagedObjectType();

		// Obtain the managed object source class
		Class<? extends ManagedObjectSource> managedObjectSourceClass = this.context
				.getManagedObjectSourceClass(this.managedObjectSourceClassName,
						LocationType.OFFICE_FLOOR, this.officeFloorLocation,
						managedObjectSourceName);
		if (managedObjectSourceClass == null) {
			return; // must have managed object source class
		}

		// Build the managed object source
		ManagedObjectBuilder<?> moBuilder = builder.addManagedObject(
				managedObjectSourceName, managedObjectSourceClass);
		for (Property property : this.propertyList) {
			moBuilder.addProperty(property.getName(), property.getValue());
		}

		// Specify the managing office
		ManagingOfficeBuilder managingOfficeBuilder = moBuilder
				.setManagingOffice(managingOffice.getDeployedOfficeName());

		// Obtain the flow types for the managed object source
		ManagedObjectFlowType<?>[] flowTypes = managedObjectType.getFlowTypes();

		// Provide process bound name if have flows
		if (flowTypes.length > 0) {

			// Ensure have Input ManagedObject name
			String inputBoundManagedObjectName = null;
			switch (this.locationType) {
			case OFFICE_FLOOR:
				if (this.inputManagedObjectNode != null) {
					inputBoundManagedObjectName = this.inputManagedObjectNode
							.getBoundManagedObjectName();
				}
				break;
			case OFFICE:
			case SECTION:
				// Can not link to managed object initiating flow.
				// Use name of managed object source.
				inputBoundManagedObjectName = managedObjectSourceName;
				break;
			default:
				throw new IllegalStateException("Unknown location type: "
						+ this.locationType);
			}
			if (CompileUtil.isBlank(inputBoundManagedObjectName)) {
				// Provide issue as should be input
				this.context
						.getCompilerIssues()
						.addIssue(LocationType.OFFICE_FLOOR,
								this.officeFloorLocation,
								AssetType.MANAGED_OBJECT,
								this.managedObjectSourceName,
								"Must provide input managed object as managed object source has flows");
			} else {
				// Bind the managed object to process state of managing office
				DependencyMappingBuilder inputDependencyMappings = managingOfficeBuilder
						.setInputManagedObjectName(inputBoundManagedObjectName);

				// TODO load input dependencies
				System.err
						.println("TODO implement loading input dependency mappings: "
								+ inputDependencyMappings);
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
					TaskNode.class, "Managed object flow " + flowName,
					this.locationType, this.location, AssetType.MANAGED_OBJECT,
					this.managedObjectSourceName, this.context
							.getCompilerIssues());
			if (taskNode == null) {
				continue; // must have task node
			}

			// Ensure the task is contained in the managing office
			WorkNode workNode = taskNode.getWorkNode();
			SectionNode section = workNode.getSectionNode();
			OfficeNode taskOffice = section.getOfficeNode();
			if (taskOffice != managingOffice) {
				this.context
						.getCompilerIssues()
						.addIssue(
								this.locationType,
								this.location,
								AssetType.MANAGED_OBJECT,
								this.managedObjectSourceName,
								"Flow "
										+ flowName
										+ " linked task must be within the managing office");
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
		for (ManagedObjectTeamType teamType : managedObjectType.getTeamTypes()) {

			// Obtain the team type details
			String teamName = teamType.getTeamName();

			// Obtain the team
			OfficeTeamNode managedObjectTeam = this.teams.get(teamName);
			TeamNode team = LinkUtil.retrieveTarget(managedObjectTeam,
					TeamNode.class, "Managed object team " + teamName,
					this.locationType, this.location, AssetType.MANAGED_OBJECT,
					this.managedObjectSourceName, this.context
							.getCompilerIssues());
			if (team == null) {
				continue; // must have the team
			}

			// Register the team to the office
			String officeTeamName = this.managedObjectSourceName + "."
					+ teamName;
			officeBuilder.registerTeam(officeTeamName, team
					.getOfficeFloorTeamName());
		}

		// Build the managed objects from this (in deterministic order)
		ManagedObjectNode[] managedObjectNodes = CompileUtil.toSortedArray(
				this.managedObjects.values(), new ManagedObjectNode[0],
				new StringExtractor<ManagedObjectNode>() {
					@Override
					public String toString(ManagedObjectNode object) {
						return object.getBoundManagedObjectName();
					}
				});
		for (ManagedObjectNode mo : managedObjectNodes) {
			mo.buildOfficeManagedObject(managingOffice, officeBuilder);
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
	public ManagedObjectFlow getManagedObjectFlow(
			String managedObjectSourceFlowName) {
		// Obtain and return the flow for the name
		ManagedObjectFlowNode flow = this.flows
				.get(managedObjectSourceFlowName);
		if (flow == null) {
			// Create the managed object flow
			flow = new ManagedObjectFlowNodeImpl(managedObjectSourceFlowName,
					this.locationType, this.location, this.context);

			// Add the managed object flow
			this.flows.put(managedObjectSourceFlowName, flow);
		}
		return flow;
	}

	@Override
	public ManagedObjectTeam getManagedObjectTeam(
			String managedObjectSourceTeamName) {

		// Must be in office or office floor context
		if ((!this.isInOfficeContext) && (!this.isInOfficeFloorContext)) {
			throw new IllegalStateException(
					"Must not obtain team unless in office or office floor context");
		}

		// Obtain and return the team for the name
		OfficeTeamNode team = this.teams.get(managedObjectSourceTeamName);
		if (team == null) {
			// Create the office team
			team = new OfficeTeamNodeImpl(managedObjectSourceTeamName,
					this.officeLocation, this.context);
			if (this.isInOfficeFloorContext) {
				// Add office floor context as within office floor context
				team.addOfficeFloorContext(this.officeFloorLocation);
			}

			// Add the managed object team
			this.teams.put(managedObjectSourceTeamName, team);
		}
		return team;
	}

	/*
	 * ==================== SectionManagedObjectSource =========================
	 */

	@Override
	public String getSectionManagedObjectSourceName() {
		return this.managedObjectSourceName;
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
		return this.managedObjectSourceName;
	}

	@Override
	public ManagedObjectTeam[] getOfficeSectionManagedObjectTeams() {

		// Ensure managed object type loaded (ensures all teams loaded)
		if (!this.isManagedObjectTypeLoaded) {
			throw new IllegalStateException(
					"Must have the managed object type loaded");
		}

		// Return the managed object teams
		return this.teams.values().toArray(new ManagedObjectTeam[0]);
	}

	@Override
	public OfficeSectionManagedObject[] getOfficeSectionManagedObjects() {
		return this.managedObjects.values().toArray(
				new OfficeSectionManagedObject[0]);
	}

	/*
	 * ================== OfficeManagedObjectSource ============================
	 */

	@Override
	public String getOfficeManagedObjectSourceName() {
		return this.managedObjectSourceName;
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

		// Must be in office context
		if (!this.isInOfficeFloorContext) {
			throw new IllegalStateException(
					"Must not obtain managing office unless in office floor context");
		}

		// Return the managing office
		return this.managingOffice;
	}

	@Override
	public OfficeFloorManagedObject addOfficeFloorManagedObject(
			String managedObjectName, ManagedObjectScope managedObjectScope) {
		return this.addManagedObject(managedObjectName, managedObjectScope);
	}

}