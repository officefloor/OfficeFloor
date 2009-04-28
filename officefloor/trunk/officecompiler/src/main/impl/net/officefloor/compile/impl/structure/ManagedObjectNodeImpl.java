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
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
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
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.compile.spi.section.SectionManagedObject;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagedObjectNode} implementation.
 * 
 * @author Daniel
 */
public class ManagedObjectNodeImpl implements ManagedObjectNode {

	/**
	 * Name of this {@link SectionManagedObject}.
	 */
	private final String managedObjectName;

	/**
	 * Class name of the {@link ManagedObjectSource}.
	 */
	private final String managedObjectSourceClassName;

	/**
	 * {@link PropertyList} to load the {@link ManagedObjectSource}.
	 */
	private final PropertyList propertyList = new PropertyListImpl();

	/**
	 * {@link ManagedObjectDependencyNode} instances by their
	 * {@link ManagedObjectDependency} names.
	 */
	private final Map<String, ManagedObjectDependencyNode> depedencies = new HashMap<String, ManagedObjectDependencyNode>();

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
	 * Location of the {@link OfficeSection} containing this
	 * {@link SectionManagedObject}.
	 */
	private final String sectionLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Flags whether the {@link ManagedObjectType} has been loaded.
	 */
	private boolean isManagedObjectTypeLoaded = false;

	/**
	 * Loaded {@link ManagedObjectType}.
	 */
	private ManagedObjectType<?> managedObjectType;

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
	 * {@link ManagingOffice}.
	 */
	private ManagingOfficeNode managingOffice;

	/**
	 * Location of the {@link OfficeFloor} containing this
	 * {@link OfficeFloorManagedObject}.
	 */
	private String officeFloorLocation;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectName
	 *            Name of this {@link SectionManagedObject}.
	 * @param managedObjectSourceClassName
	 *            Class name of the {@link ManagedObjectSource}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link SectionManagedObject}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagedObjectNodeImpl(String managedObjectName,
			String managedObjectSourceClassName, String sectionLocation,
			NodeContext context) {
		this.managedObjectName = managedObjectName;
		this.managedObjectSourceClassName = managedObjectSourceClassName;
		this.sectionLocation = sectionLocation;
		this.context = context;
	}

	/*
	 * ===================== ManagedObjectNode ================================
	 */

	@Override
	public void addOfficeContext(String officeLocation) {
		this.officeLocation = officeLocation;

		// Flag all existing dependencies within office context
		for (ManagedObjectDependencyNode dependency : this.depedencies.values()) {
			dependency.addOfficeContext(this.officeLocation);
		}

		// Flag all existing flows within office context
		for (ManagedObjectFlowNode flow : this.flows.values()) {
			flow.addOfficeContext(this.officeLocation);
		}

		// Now in office context
		this.isInOfficeContext = true;
	}

	@Override
	public void addOfficeFloorContext(String officeFloorLocation) {
		this.officeFloorLocation = officeFloorLocation;

		// Load the managing office
		this.managingOffice = new ManagingOfficeNodeImpl(
				this.managedObjectName, this.officeFloorLocation, this.context);

		// Flag all existing dependencies within office floor context
		for (ManagedObjectDependencyNode dependency : this.depedencies.values()) {
			dependency.addOfficeFloorContext(this.officeFloorLocation);
		}

		// Flag all existing flows within office floor context
		for (ManagedObjectFlowNode flow : this.flows.values()) {
			flow.addOfficeFloorContext(this.officeFloorLocation);
		}

		// Flag all existing teams within office floor context
		for (OfficeTeamNode team : this.teams.values()) {
			team.addOfficeFloorContext(this.officeFloorLocation);
		}

		// Now in office floor context
		this.isInOfficeFloorContext = true;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void loadManagedObjectMetaData() {

		// Obtain the managed object source class
		Class<? extends ManagedObjectSource> managedObjectSourceClass = this.context
				.getManagedObjectSourceClass(this.managedObjectSourceClassName,
						LocationType.SECTION, this.sectionLocation,
						this.managedObjectName);
		if (managedObjectSourceClass == null) {
			return; // must have managed object source class
		}

		// Create the loader to obtain the managed object type
		ManagedObjectLoader loader = new ManagedObjectLoaderImpl(
				LocationType.SECTION, this.sectionLocation,
				this.managedObjectName, this.context);

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

		// Managed object type loaded
		this.isManagedObjectTypeLoaded = true;
	}

	@Override
	public void buildManagedObject(OfficeFloorBuilder builder) {

		// Obtain the managed object source class
		Class<? extends ManagedObjectSource<?, ?>> managedObjectSourceClass = this.context
				.getManagedObjectSourceClass(this.managedObjectSourceClassName,
						LocationType.OFFICE_FLOOR, this.officeFloorLocation,
						this.managedObjectName);
		if (managedObjectSourceClass == null) {
			return; // must have managed object source class
		}

		// Build the managed object source
		ManagedObjectBuilder<?> moBuilder = builder.addManagedObject(
				this.managedObjectName, managedObjectSourceClass);
		for (Property property : this.propertyList) {
			moBuilder.addProperty(property.getName(), property.getValue());
		}

		// Obtain the managing office
		DeployedOffice managingOffice = LinkUtil.retrieveTarget(
				this.managingOffice, DeployedOffice.class, "Managed Object "
						+ this.managedObjectName, LocationType.OFFICE_FLOOR,
				this.officeFloorLocation, AssetType.MANAGED_OBJECT,
				this.managedObjectName, this.context.getCompilerIssues());
		if (managingOffice != null) {
			// Specify the managing office
			moBuilder.setManagingOffice(managingOffice.getDeployedOfficeName());
		}
	}

	/*
	 * ==================== SectionManagedObject ===============================
	 */

	@Override
	public String getSectionManagedObjectName() {
		return this.managedObjectName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.propertyList.addProperty(name).setValue(value);
	}

	@Override
	public ManagedObjectDependency getManagedObjectDependency(
			String managedObjectDependencyName) {
		// Obtain and return the dependency for the name
		ManagedObjectDependencyNode dependency = this.depedencies
				.get(managedObjectDependencyName);
		if (dependency == null) {
			// Create the managed object dependency
			dependency = new ManagedObjectDependencyNodeImpl(
					managedObjectDependencyName, this.sectionLocation,
					this.context);
			if (this.isInOfficeContext) {
				// Add office context as within office context
				dependency.addOfficeContext(this.officeLocation);
			}
			if (this.isInOfficeFloorContext) {
				// Add office floor context as within office floor context
				dependency.addOfficeFloorContext(this.officeFloorLocation);
			}

			// Add the managed object dependency
			this.depedencies.put(managedObjectDependencyName, dependency);
		}
		return dependency;
	}

	@Override
	public ManagedObjectFlow getManagedObjectFlow(String managedObjectFlowName) {
		// Obtain and return the flow for the name
		ManagedObjectFlowNode flow = this.flows.get(managedObjectFlowName);
		if (flow == null) {
			// Create the managed object flow
			flow = new ManagedObjectFlowNodeImpl(managedObjectFlowName,
					this.sectionLocation, this.context);
			if (this.isInOfficeContext) {
				// Add office context as within office context
				flow.addOfficeContext(this.officeLocation);
			}
			if (this.isInOfficeFloorContext) {
				// Add office floor context as within office floor context
				flow.addOfficeFloorContext(this.officeFloorLocation);
			}

			// Add the managed object flow
			this.flows.put(managedObjectFlowName, flow);
		}
		return flow;
	}

	/*
	 * ================ OfficeSectionManagedObject ============================
	 */

	@Override
	public String getOfficeSectionManagedObjectName() {
		return this.managedObjectName;
	}

	@Override
	public ManagedObjectTeam[] getOfficeSectionManagedObjectTeams() {

		// Ensure managed object type loaded
		if (!this.isManagedObjectTypeLoaded) {
			throw new IllegalStateException(
					"Should not obtain managed object teams before being initialised");
		}

		// Return the managed object teams
		return this.teams.values().toArray(new ManagedObjectTeam[0]);
	}

	@Override
	public Class<?>[] getSupportedExtensionInterfaces() {

		// Ensure managed object type loaded
		if (!this.isManagedObjectTypeLoaded) {
			throw new IllegalStateException(
					"Should not obtain supported extension interfaces before being initialised");
		}

		// Return the supported extension interfaces
		if (this.managedObjectType == null) {
			// Issue in loading type, no extension interfaces
			return new Class[0];
		} else {
			// Return the extension interfaces supported by the managed object
			return this.managedObjectType.getExtensionInterfaces();
		}
	}

	/*
	 * ======================= OfficeManagedObject ============================
	 */

	@Override
	public String getOfficeManagedObjectName() {
		return this.managedObjectName;
	}

	@Override
	public ManagedObjectTeam getManagedObjectTeam(String managedObjectTeamName) {

		// Must be in office or office floor context
		if ((!this.isInOfficeContext) && (!this.isInOfficeFloorContext)) {
			throw new IllegalStateException(
					"Must not obtain team unless in office or office floor context");
		}

		// Obtain and return the team for the name
		OfficeTeamNode team = this.teams.get(managedObjectTeamName);
		if (team == null) {
			// Create the office team
			team = new OfficeTeamNodeImpl(managedObjectTeamName,
					this.officeLocation, this.context);
			if (this.isInOfficeFloorContext) {
				// Add office floor context as within office floor context
				team.addOfficeFloorContext(this.officeFloorLocation);
			}

			// Add the managed object team
			this.teams.put(managedObjectTeamName, team);
		}
		return team;
	}

	/*
	 * ==================== OfficeFloorManagedObject ===========================
	 */

	@Override
	public String getOfficeFloorManagedObjectName() {
		return this.managedObjectName;
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

	/*
	 * =================== DependentManagedObject ==============================
	 */

	@Override
	public String getDependentManagedObjectName() {
		return this.managedObjectName;
	}

	/*
	 * =================== AdministerableManagedObject =========================
	 */

	@Override
	public String getAdministerableManagedObjectName() {
		return this.managedObjectName;
	}

	/*
	 * =================== LinkObjectNode ======================================
	 */

	/**
	 * Linked {@link LinkObjectNode}.
	 */
	private LinkObjectNode linkedObjectName;

	@Override
	public boolean linkObjectNode(LinkObjectNode node) {
		// Link
		this.linkedObjectName = node;
		return true;
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectName;
	}

}