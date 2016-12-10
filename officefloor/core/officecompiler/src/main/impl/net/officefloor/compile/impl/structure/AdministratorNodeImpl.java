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
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.AdministratorNode;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.DutyNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.AdministerableManagedObject;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.compile.spi.office.OfficeDuty;
import net.officefloor.frame.api.build.AdministratorBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
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
	 * {@link PropertyList} to source the {@link Administrator}.
	 */
	private final PropertyList properties;

	/**
	 * Parent {@link Office}.
	 */
	private final OfficeNode officeNode;

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

		/**
		 * Class name of the {@link AdministratorSource}.
		 */
		private final String administratorSourceClassName;

		/**
		 * {@link AdministratorSource} instance to use. Should this be specified
		 * it overrides the {@link Class}.
		 */
		@SuppressWarnings("unused")
		private final AdministratorSource<?, ?> administratorSource;

		/**
		 * Instantiate.
		 * 
		 * @param administratorSourceClassName
		 *            Class name of the {@link AdministratorSource}.
		 * @param administratorSource
		 *            {@link AdministratorSource} instance to use. Should this
		 *            be specified it overrides the {@link Class}.
		 */
		public InitialisedState(String administratorSourceClassName,
				AdministratorSource<?, ?> administratorSource) {
			this.administratorSourceClassName = administratorSourceClassName;
			this.administratorSource = administratorSource;
		}
	}

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
	 * @param officeNode
	 *            Parent {@link OfficeNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public AdministratorNodeImpl(String administratorName,
			OfficeNode officeNode, NodeContext context) {
		this.administratorName = administratorName;
		this.officeNode = officeNode;
		this.context = context;

		// Create additional objects
		this.properties = this.context.createPropertyList();
	}

	/*
	 * ======================= Node ========================
	 */

	@Override
	public String getNodeName() {
		return this.administratorName;
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
		return this.officeNode;
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);

	}

	@Override
	public void initialise(String administratorSourceClassName,
			AdministratorSource<?, ?> administratorSource) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(administratorSourceClassName,
						administratorSource));
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
		return NodeUtil.getNode(dutyName, this.duties,
				() -> this.context.createDutyNode(dutyName, this));
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

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AdministratorType<?, ?> loadAdministratorType() {

		// Obtain the administrator source class
		Class administratorSourceClass = this.context
				.getAdministratorSourceClass(
						this.state.administratorSourceClassName, this);
		if (administratorSourceClass == null) {
			return null; // must obtain source class
		}

		// Load and return the administrator type
		AdministratorLoader loader = this.context.getAdministratorLoader(this);
		return loader.loadAdministratorType(administratorSourceClass,
				this.properties);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void buildAdministrator(OfficeBuilder officeBuilder) {

		// Obtain the administrator source class
		Class administratorSourceClass = this.context
				.getAdministratorSourceClass(
						this.state.administratorSourceClassName, this);
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
		OfficeTeamNode officeTeam = LinkUtil.retrieveTarget(this,
				OfficeTeamNode.class, this.context.getCompilerIssues());
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
				managedObject = LinkUtil.retrieveTarget(linkObject,
						BoundManagedObjectNode.class,
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
		this.duties
				.values()
				.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(
						a.getOfficeDutyName(), b.getOfficeDutyName()))
				.forEach(
						(duty) -> adminBuilder.addDuty(duty.getOfficeDutyName()));
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
		return LinkUtil.linkTeamNode(this, node,
				this.context.getCompilerIssues(),
				(link) -> this.linkedTeamNode = link);
	}

	@Override
	public LinkTeamNode getLinkedTeamNode() {
		return this.linkedTeamNode;
	}

}