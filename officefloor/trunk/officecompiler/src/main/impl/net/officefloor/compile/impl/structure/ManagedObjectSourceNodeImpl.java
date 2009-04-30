/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.structure;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.managedobject.ManagedObjectLoaderImpl;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagingOfficeNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectTeamType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSectionManagedObject;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectSourceNode} implementation.
 * 
 * @author Daniel
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
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedObjectSourceNodeImpl(String managedObjectSourceName,
			String managedObjectSourceClassName, LocationType locationType,
			String location, NodeContext context) {
		this.managedObjectSourceName = managedObjectSourceName;
		this.managedObjectSourceClassName = managedObjectSourceClassName;
		this.locationType = locationType;
		this.location = location;
		this.context = context;
	}

	/**
	 * Adds an issue regarding the {@link ManagedObjectSource} being built.
	 * 
	 * @param issueDescription
	 *            Description of the issue.
	 */
	private void addIssue(String issueDescription) {
		this.context.getCompilerIssues().addIssue(this.locationType,
				this.location, AssetType.MANAGED_OBJECT,
				this.managedObjectSourceName, issueDescription);
	}

	public ManagedObjectNode addManagedObject(String managedObjectName,
			ManagedObjectScope managedObjectScope) {
		// Obtain and return the managed object for the name
		ManagedObjectNode managedObject = this.managedObjects
				.get(managedObjectName);
		if (managedObject == null) {
			// Add the managed object
			managedObject = new ManagedObjectNodeImpl(managedObjectName,
					this.locationType, this.location, this, this.context);
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
			this.addIssue(locationTypeDesc + " managed object "
					+ managedObjectName + " already added");
		}
		return managedObject;
	}

	/*
	 * ================ ManagedObjectSourceNode ===============================
	 */

	@Override
	public void loadManagedObjectType() {

		// Flag that loaded the managed object type (whether successful or not)
		this.isManagedObjectTypeLoaded = true;

		// Obtain the managed object source class
		Class<? extends ManagedObjectSource<?, ?>> managedObjectSourceClass = this.context
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
	public void buildManagedObject(OfficeFloorBuilder builder) {

		// Obtain the managed object source class
		Class<? extends ManagedObjectSource<?, ?>> managedObjectSourceClass = this.context
				.getManagedObjectSourceClass(this.managedObjectSourceClassName,
						LocationType.OFFICE_FLOOR, this.officeFloorLocation,
						this.managedObjectSourceName);
		if (managedObjectSourceClass == null) {
			return; // must have managed object source class
		}

		// Build the managed object source
		ManagedObjectBuilder<?> moBuilder = builder.addManagedObject(
				this.managedObjectSourceName, managedObjectSourceClass);
		for (Property property : this.propertyList) {
			moBuilder.addProperty(property.getName(), property.getValue());
		}

		// Obtain the managing office
		DeployedOffice managingOffice = LinkUtil.retrieveTarget(
				this.managingOffice, DeployedOffice.class, "Managed Object "
						+ this.managedObjectSourceName,
				LocationType.OFFICE_FLOOR, this.officeFloorLocation,
				AssetType.MANAGED_OBJECT, this.managedObjectSourceName,
				this.context.getCompilerIssues());
		if (managingOffice != null) {
			// Specify the managing office
			ManagingOfficeBuilder<?> managingOfficeBuilder = moBuilder
					.setManagingOffice(managingOffice.getDeployedOfficeName());
			// TODO configure in managing details of office
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
	public SectionManagedObject getSectionManagedObject(
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
	public OfficeManagedObject getOfficeManagedObject(String managedObjectName,
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
	public OfficeFloorManagedObject getOfficeFloorManagedObject(
			String managedObjectName, ManagedObjectScope managedObjectScope) {
		return this.addManagedObject(managedObjectName, managedObjectScope);
	}

}