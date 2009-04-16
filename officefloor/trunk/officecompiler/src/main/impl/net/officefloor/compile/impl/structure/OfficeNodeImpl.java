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

import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.internal.structure.AdministratorNode;
import net.officefloor.compile.internal.structure.LinkOfficeNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SectionObjectNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeRequiredManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.office.OfficeSectionOutput;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.TaskTeam;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.configuration.OfficeFloorConfiguration;
import net.officefloor.model.repository.ConfigurationContext;

/**
 * {@link OfficeNode} implementation.
 * 
 * @author Daniel
 */
public class OfficeNodeImpl extends AbstractNode implements OfficeNode {

	/**
	 * Name of this {@link DeployedOffice}.
	 */
	private final String officeName;

	/**
	 * Class name of the {@link OfficeSource}.
	 */
	private final String officeSourceClassName;

	/**
	 * {@link PropertyList} to source the {@link Office}.
	 */
	private final PropertyList properties = new PropertyListImpl();

	/**
	 * {@link ConfigurationContext}.
	 */
	private final ConfigurationContext configurationContext;

	/**
	 * {@link ClassLoader}.
	 */
	private final ClassLoader classLoader;

	/**
	 * Location of the {@link Office}.
	 */
	private final String officeLocation;

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues;

	/**
	 * {@link SectionObjectNode} instances by their
	 * {@link OfficeRequiredManagedObject} name.
	 */
	private final Map<String, SectionObjectNode> objects = new HashMap<String, SectionObjectNode>();

	/**
	 * {@link OfficeTeamNode} instances by their {@link OfficeTeam} name.
	 */
	private final Map<String, OfficeTeamNode> teams = new HashMap<String, OfficeTeamNode>();

	/**
	 * {@link SectionNode} instances by their {@link OfficeSection} name.
	 */
	private final Map<String, SectionNode> sections = new HashMap<String, SectionNode>();

	/**
	 * {@link ManagedObjectNode} instances by their {@link OfficeManagedObject}
	 * name.
	 */
	private final Map<String, ManagedObjectNode> managedObjects = new HashMap<String, ManagedObjectNode>();

	/**
	 * {@link AdministratorNode} instances by their {@link OfficeAdministrator}
	 * name.
	 */
	private final Map<String, AdministratorNode> administrators = new HashMap<String, AdministratorNode>();

	/**
	 * Flag indicating if in the {@link OfficeFloor} context.
	 */
	private boolean isInOfficeFloorContext = false;

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private String officeFloorLocation;

	/**
	 * Initiate.
	 * 
	 * @param officeName
	 *            Name of this {@link DeployedOffice}.
	 * @param officeSourceClassName
	 *            Class name of the {@link OfficeSource}.
	 * @param configurationContext
	 *            {@link ConfigurationContext}.
	 * @param classLoader
	 *            {@link ClassLoader}.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public OfficeNodeImpl(String officeName, String officeSourceClassName,
			ConfigurationContext configurationContext, ClassLoader classLoader,
			String officeLocation, CompilerIssues issues) {
		this.officeName = officeName;
		this.officeSourceClassName = officeSourceClassName;
		this.configurationContext = configurationContext;
		this.classLoader = classLoader;
		this.officeLocation = officeLocation;
		this.issues = issues;
	}

	/*
	 * =================== AbstractNode ================================
	 */

	@Override
	protected void addIssue(String issueDescription) {
		this.issues.addIssue(LocationType.OFFICE, this.officeLocation, null,
				null, issueDescription);
	}

	/*
	 * ================== OfficeNode ===================================
	 */

	@Override
	public void addOfficeFloorContext(String officeFloorLocation) {
		this.officeFloorLocation = officeFloorLocation;

		// Flag all the teams within office floor context
		for (OfficeTeamNode team : this.teams.values()) {
			team.addOfficeFloorContext(this.officeFloorLocation);
		}

		this.isInOfficeFloorContext = true;
	}

	/*
	 * ===================== OfficeArchitect ================================
	 */

	@Override
	public OfficeRequiredManagedObject getOfficeRequiredManagedObject(
			String officeManagedObjectName, String objectType) {
		// Obtain and return the required object for the name
		SectionObjectNode object = this.objects.get(officeManagedObjectName);
		if (object == null) {

			// Ensure issue if already added as managed object
			ManagedObjectNode managedObject = this.managedObjects
					.get(officeManagedObjectName);
			if (managedObject != null) {
				// Managed object already added
				this.addIssue("Object " + officeManagedObjectName
						+ " already added as Managed Object");
			}

			// Add the object
			object = new SectionObjectNodeImpl(officeManagedObjectName,
					objectType, this.officeLocation, this.issues);
			this.objects.put(officeManagedObjectName, object);

		} else {
			// Added but determine if requires initialising
			if (!object.isInitialised()) {
				// Initialise as not yet initialised
				object.initialise(objectType);
			} else {
				// Object already added and initialised
				this.addIssue("Object " + officeManagedObjectName
						+ " already added");
			}
		}
		return object;
	}

	@Override
	public OfficeTeam getTeam(String officeTeamName) {
		// Obtain and return the team for the name
		OfficeTeamNode team = this.teams.get(officeTeamName);
		if (team == null) {
			// Create the team
			team = new OfficeTeamNodeImpl(officeTeamName, this.officeLocation,
					this.issues);
			if (this.isInOfficeFloorContext) {
				// Add office floor context as within office floor context
				team.addOfficeFloorContext(this.officeFloorLocation);
			}

			// Add the team
			this.teams.put(officeTeamName, team);
		} else {
			// Team already added and initialised
			this.addIssue("Team " + officeTeamName + " already added");
		}
		return team;
	}

	@Override
	public OfficeSection addSection(String sectionName,
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties) {
		// Obtain and return the section for the name
		SectionNode section = this.sections.get(sectionName);
		if (section == null) {
			// Create the section and have it loaded
			section = new SectionNodeImpl(sectionName, sectionSourceClassName,
					properties, sectionLocation, this.issues);
			section.loadSection(this.officeLocation, this.configurationContext,
					this.classLoader);

			// Add the section
			this.sections.put(sectionName, section);
		} else {
			// Section already added
			this.addIssue("Section " + sectionName + " already added");
		}
		return section;
	}

	@Override
	public OfficeManagedObject addManagedObject(String managedObjectName,
			String managedObjectSourceClassName) {
		// Obtain and return the section managed object for the name
		ManagedObjectNode managedObject = this.managedObjects
				.get(managedObjectName);
		if (managedObject == null) {

			// Ensure not already added as required object
			SectionObjectNode object = this.objects.get(managedObjectName);
			if (object != null) {
				// Object already added
				this.addIssue("Managed object " + managedObjectName
						+ " already added as Object");
			}

			// Create the office managed object (within office context)
			managedObject = new ManagedObjectNodeImpl(managedObjectName,
					managedObjectSourceClassName, this.officeLocation,
					this.issues);
			managedObject.addOfficeContext(this.officeLocation);

			// Add the office managed object
			this.managedObjects.put(managedObjectName, managedObject);

		} else {
			// Managed object already added
			this.addIssue("Managed object " + managedObjectName
					+ " already added");
		}
		return managedObject;
	}

	@Override
	public OfficeAdministrator addAdministrator(String administratorName,
			String administratorSourceClassName) {
		// Obtain and return the administrator for the name
		AdministratorNode administrator = this.administrators
				.get(administratorName);
		if (administrator == null) {
			// Add the administrator
			administrator = new AdministratorNodeImpl(administratorName,
					this.officeLocation, this.issues);
			this.administrators.put(administratorName, administrator);
		} else {
			// Administrator already added and initialised
			this.addIssue("Administrator " + administratorName
					+ " already added");
		}
		return administrator;
	}

	@Override
	public void link(OfficeSectionObject sectionObject,
			OfficeManagedObject managedObject) {
		this.linkObject(sectionObject, managedObject);
	}

	@Override
	public void link(OfficeSectionObject sectionObject,
			OfficeRequiredManagedObject managedObject) {
		this.linkObject(sectionObject, managedObject);
	}

	@Override
	public void link(ManagedObjectDependency dependency,
			OfficeManagedObject managedObject) {
		this.linkObject(dependency, managedObject);
	}

	@Override
	public void link(ManagedObjectDependency dependency,
			OfficeRequiredManagedObject managedObject) {
		this.linkObject(dependency, managedObject);
	}

	@Override
	public void link(ManagedObjectFlow flow, OfficeSectionInput input) {
		this.linkFlow(flow, input);
	}

	@Override
	public void link(OfficeSectionOutput output, OfficeSectionInput input) {
		this.linkFlow(output, input);
	}

	@Override
	public void link(TaskTeam team, OfficeTeam officeTeam) {
		this.linkTeam(team, officeTeam);
	}

	@Override
	public void link(ManagedObjectTeam team, OfficeTeam officeTeam) {
		this.linkTeam(team, officeTeam);
	}

	/*
	 * =================== DeployedOffice =====================================
	 */

	@Override
	public String getDeployedOfficeName() {
		return this.officeName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	@Override
	public DeployedOfficeInput getDeployedOfficeInput(String sectionName,
			String inputName) {

		// Ensure in office floor context
		if (!this.isInOfficeFloorContext) {
			throw new IllegalStateException(
					"Must be in office floor context to obtain office input");
		}

		// Obtain the section
		SectionNode section = this.sections.get(sectionName);
		if (section == null) {
			// Add the section
			section = new SectionNodeImpl(sectionName);
			this.sections.put(sectionName, section);
		}

		// Obtain and return the section input
		return section.getDeployedOfficeInput(inputName);
	}

	@Override
	public OfficeRequiredManagedObject getOfficeRequiredManagedObject(
			String officeManagedObjectName) {

		// Ensure in office floor context
		if (!this.isInOfficeFloorContext) {
			throw new IllegalStateException(
					"Must be in office floor context to obtain required managed object");
		}

		// Obtain and return the required managed object
		SectionObjectNode object = this.objects.get(officeManagedObjectName);
		if (object == null) {
			// Create the object within the office floor context
			object = new SectionObjectNodeImpl(officeManagedObjectName,
					this.officeLocation, this.issues);
			object.addOfficeFloorContext(this.officeFloorLocation);

			// Add the object
			this.objects.put(officeManagedObjectName, object);
		}
		return object;
	}

	@Override
	public OfficeTeam getOfficeTeam(String officeTeamName) {

		// Ensure in office floor context
		if (!this.isInOfficeFloorContext) {
			throw new IllegalStateException(
					"Must be in office floor context to obtain team");
		}

		// Obtain and return the office team
		OfficeTeamNode team = this.teams.get(officeTeamName);
		if (team == null) {
			// Create the team within the office floor context
			team = new OfficeTeamNodeImpl(officeTeamName, this.officeLocation,
					this.issues);
			team.addOfficeFloorContext(this.officeFloorLocation);

			// Add the office team
			this.teams.put(officeTeamName, team);
		}
		return team;
	}

	/*
	 * ================== LinkOfficeNode ===============================
	 */

	/**
	 * Linked {@link LinkOfficeNode}.
	 */
	private LinkOfficeNode linkedOfficeNode;

	@Override
	public boolean linkOfficeNode(LinkOfficeNode node) {

		// Link
		this.linkedOfficeNode = node;
		return true;
	}

	@Override
	public LinkOfficeNode getLinkedOfficeNode() {
		return this.linkedOfficeNode;
	}

}