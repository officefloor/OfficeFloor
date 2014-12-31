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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.administrator.AdministratorLoader;
import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.impl.util.StringExtractor;
import net.officefloor.compile.internal.structure.AdministratorNode;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.DutyNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.AdministerableManagedObject;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.compile.spi.office.OfficeDuty;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorIssues.AssetType;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.spi.administration.Administrator;
import net.officefloor.frame.spi.administration.source.AdministratorSource;

/**
 * {@link AdministratorNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministratorNodeImpl implements AdministratorNode {

	/**
	 * Name of this {@link OfficeAdministrator}.
	 */
	private final String administratorName;

	/**
	 * Class name of the {@link AdministratorSource}.
	 */
	private final String administratorSourceClassName;

	/**
	 * {@link AdministratorSource} instance to use. Should this be specified it
	 * overrides the {@link Class}.
	 */
	@SuppressWarnings("unused")
	private final AdministratorSource<?, ?> administratorSource;

	/**
	 * {@link PropertyList} to source the {@link Administrator}.
	 */
	private final PropertyList properties = new PropertyListImpl();

	/**
	 * Location of the {@link Office} containing this
	 * {@link OfficeAdministrator}.
	 */
	private final String officeLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link DutyNode} instances by their {@link OfficeDuty} name.
	 */
	private final Map<String, DutyNode> duties = new HashMap<String, DutyNode>();

	/**
	 * Listing of {@link AdministerableManagedObject} instances to administer.
	 */
	private final List<AdministerableManagedObject> administeredManagedObjects = new LinkedList<AdministerableManagedObject>();

	/**
	 * Initiate.
	 * 
	 * @param administratorName
	 *            Name of this {@link OfficeAdministrator}.
	 * @param administratorSourceClassName
	 *            Class name of the {@link AdministratorSource}.
	 * @param officeLocation
	 *            Location of the {@link Office} containing this
	 *            {@link OfficeAdministrator}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public AdministratorNodeImpl(String administratorName,
			String administratorSourceClassName, String officeLocation,
			NodeContext context) {
		this.administratorName = administratorName;
		this.administratorSourceClassName = administratorSourceClassName;
		this.administratorSource = null;
		this.officeLocation = officeLocation;
		this.context = context;
	}

	/**
	 * Initiate.
	 * 
	 * @param administratorName
	 *            Name of this {@link OfficeAdministrator}.
	 * @param administratorSource
	 *            {@link AdministratorSource} instance to use.
	 * @param officeLocation
	 *            Location of the {@link Office} containing this
	 *            {@link OfficeAdministrator}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public AdministratorNodeImpl(String administratorName,
			AdministratorSource<?, ?> administratorSource,
			String officeLocation, NodeContext context) {
		this.administratorName = administratorName;
		this.administratorSourceClassName = null;
		this.administratorSource = administratorSource;
		this.officeLocation = officeLocation;
		this.context = context;
	}

	/*
	 * ======================= OfficeAdministrator ========================
	 */

	@Override
	public String getOfficeAdministratorName() {
		return this.administratorName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	@Override
	public OfficeDuty getDuty(String dutyName) {
		// Obtain and return the duty for the name
		DutyNode duty = this.duties.get(dutyName);
		if (duty == null) {
			// Add the duty
			duty = new DutyNodeImpl(dutyName, this);
			this.duties.put(dutyName, duty);
		}
		return duty;
	}

	@Override
	public void administerManagedObject(
			AdministerableManagedObject managedObject) {

		// Administer the managed object
		this.administeredManagedObjects.add(managedObject);

		// Make office object aware of administration
		if (managedObject instanceof OfficeObjectNode) {
			((OfficeObjectNode) managedObject).addAdministrator(this);
		}
	}

	/*
	 * ===================== AdministratorNode ==============================
	 */

	/**
	 * Lazy loaded {@link AdministratorType}.
	 */
	private AdministratorType<?, ?> administratorType = null;

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AdministratorType<?, ?> getAdministratorType() {

		// Lazy load the administrator type
		if (this.administratorType == null) {

			// Obtain the administrator source class
			Class administratorSourceClass = this.context
					.getAdministratorSourceClass(
							this.administratorSourceClassName,
							this.officeLocation, this.administratorName);
			if (administratorSourceClass == null) {
				return null; // must obtain source class
			}

			// Load the administrator type
			AdministratorLoader loader = this.context.getAdministratorLoader(
					this.officeLocation, this.administratorName);
			this.administratorType = loader.loadAdministrator(
					administratorSourceClass, this.properties);
		}

		// Return administrator type
		return this.administratorType;
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void buildAdministrator(OfficeBuilder officeBuilder) {

		// Obtain the administrator source class
		Class administratorSourceClass = this.context
				.getAdministratorSourceClass(this.administratorSourceClassName,
						this.officeLocation, this.administratorName);
		if (administratorSourceClass == null) {
			return; // must obtain source class
		}

		// Build the administrator
		AdministratorBuilder<?> adminBuilder = officeBuilder
				.addThreadAdministrator(this.administratorName,
						administratorSourceClass);
		for (Property property : this.properties) {
			adminBuilder.addProperty(property.getName(), property.getValue());
		}

		// Obtain the office team responsible for this administration
		OfficeTeam officeTeam = LinkUtil.retrieveTarget(this, OfficeTeam.class,
				"Administrator " + this.administratorName, LocationType.OFFICE,
				this.officeLocation, AssetType.ADMINISTRATOR,
				this.administratorName, this.context.getCompilerIssues());
		if (officeTeam != null) {
			// Build the team responsible for the administrator
			adminBuilder.setTeam(officeTeam.getOfficeTeamName());
		}

		// Administer the managed objects
		for (AdministerableManagedObject administerableManagedObject : this.administeredManagedObjects) {

			// Obtain as link object node
			LinkObjectNode linkObject = (LinkObjectNode) administerableManagedObject;

			// Obtain the managed object
			BoundManagedObjectNode managedObject;
			if (linkObject instanceof BoundManagedObjectNode) {
				// Directly linked to managed object
				managedObject = (BoundManagedObjectNode) linkObject;
			} else {
				// Locate the managed object
				managedObject = LinkUtil.retrieveTarget(
						linkObject,
						BoundManagedObjectNode.class,
						"Managed Object "
								+ administerableManagedObject
										.getAdministerableManagedObjectName(),
						LocationType.OFFICE, this.officeLocation,
						AssetType.ADMINISTRATOR, this.administratorName,
						this.context.getCompilerIssues());
			}
			if (managedObject == null) {
				continue; // must have managed object
			}

			// Build administration of the managed object
			adminBuilder.administerManagedObject(managedObject
					.getBoundManagedObjectName());
		}

		// Build the duties (in deterministic order)
		DutyNode[] duties = CompileUtil.toSortedArray(this.duties.values(),
				new DutyNode[0], new StringExtractor<DutyNode>() {
					@Override
					public String toString(DutyNode object) {
						return object.getOfficeDutyName();
					}
				});
		for (DutyNode duty : duties) {
			adminBuilder.addDuty(duty.getOfficeDutyName());
		}
	}

	/*
	 * ==================== LinkTeamNode ===================================
	 */

	/**
	 * Linked {@link LinkTeamNode}.
	 */
	private LinkTeamNode linkedTeamNode = null;

	@Override
	public boolean linkTeamNode(LinkTeamNode node) {

		// Ensure not already linked
		if (this.linkedTeamNode != null) {
			// Team already linked
			this.context.getCompilerIssues().addIssue(LocationType.OFFICE,
					this.officeLocation, AssetType.ADMINISTRATOR,
					this.administratorName, "Team already assigned");
			return false; // already linked
		}

		// Link
		this.linkedTeamNode = node;
		return true;
	}

	@Override
	public LinkTeamNode getLinkedTeamNode() {
		return this.linkedTeamNode;
	}

}