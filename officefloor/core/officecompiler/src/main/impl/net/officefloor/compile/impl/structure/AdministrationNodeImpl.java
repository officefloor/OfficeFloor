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

import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.administration.AdministrationLoader;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.AdministrationNode;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.office.AdministerableManagedObject;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.frame.api.administration.Administration;
import net.officefloor.frame.api.administration.AdministrationFactory;
import net.officefloor.frame.api.build.AdministrationBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link AdministrationNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AdministrationNodeImpl implements AdministrationNode {

	/**
	 * Name of this {@link OfficeAdministration}.
	 */
	private final String administrationName;

	/**
	 * {@link PropertyList} to source the {@link Administration}.
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
		 * Class name of the {@link AdministrationSource}.
		 */
		private final String administrationSourceClassName;

		/**
		 * {@link AdministrationSource} instance to use. Should this be
		 * specified it overrides the {@link Class}.
		 */
		@SuppressWarnings("unused")
		private final AdministrationSource<?, ?, ?> administrationSource;

		/**
		 * Instantiate.
		 * 
		 * @param administratorSourceClassName
		 *            Class name of the {@link AdministrationSource}.
		 * @param administratorSource
		 *            {@link AdministrationSource} instance to use. Should this
		 *            be specified it overrides the {@link Class}.
		 */
		public InitialisedState(String administrationSourceClassName,
				AdministrationSource<?, ?, ?> administrationSource) {
			this.administrationSourceClassName = administrationSourceClassName;
			this.administrationSource = administrationSource;
		}
	}

	/**
	 * Listing of {@link AdministerableManagedObject} instances to administer.
	 */
	private final List<AdministerableManagedObject> administeredManagedObjects = new LinkedList<AdministerableManagedObject>();

	/**
	 * Initiate.
	 * 
	 * @param administrationName
	 *            Name of this {@link OfficeAdministration}.
	 * @param officeNode
	 *            Parent {@link OfficeNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public AdministrationNodeImpl(String administrationName, OfficeNode officeNode, NodeContext context) {
		this.administrationName = administrationName;
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
		return this.administrationName;
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
	public void initialise(String administrationSourceClassName, AdministrationSource<?, ?, ?> administrationSource) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(administrationSourceClassName, administrationSource));
	}

	/*
	 * ======================= OfficeAdministration ========================
	 */

	@Override
	public String getOfficeAdministrationName() {
		return this.administrationName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	@Override
	public void administerManagedObject(AdministerableManagedObject managedObject) {

		// Administer the managed object
		this.administeredManagedObjects.add(managedObject);

		// Make office object aware of administration
		if (managedObject instanceof OfficeObjectNode) {
			((OfficeObjectNode) managedObject).addAdministrator(this);
		}
	}

	/*
	 * ===================== AdministrationNode ==============================
	 */

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AdministrationType<?, ?, ?> loadAdministrationType() {

		// Obtain the administration source class
		Class administrationSourceClass = this.context
				.getAdministrationSourceClass(this.state.administrationSourceClassName, this);
		if (administrationSourceClass == null) {
			return null; // must obtain source class
		}

		// Load and return the administration type
		AdministrationLoader loader = this.context.getAdministrationLoader(this);
		return loader.loadAdministrationType(administrationSourceClass, this.properties);
	}

	@Override
	public void buildPreFunctionAdministration(ManagedFunctionBuilder<?, ?> functionBuilder) {
		this.buildAdministration(true, functionBuilder);
	}

	@Override
	public void buildPostFunctionAdministration(ManagedFunctionBuilder<?, ?> functionBuilder) {
		this.buildAdministration(false, functionBuilder);
	}

	/**
	 * Builds the {@link Administration} onto the {@link ManagedFunction}.
	 * 
	 * @param isPreNotPost
	 *            <code>true</code> for pre {@link Administration}. Otherwise
	 *            post {@link Administration}.
	 * @param functionBuilder
	 *            {@link ManagedFunctionBuilder}.
	 */
	private void buildAdministration(boolean isPreNotPost, ManagedFunctionBuilder<?, ?> functionBuilder) {

		// Build the administration type
		AdministrationType<?, ?, ?> adminType = this.loadAdministrationType();

		// Obtain the administration details
		Class extension = adminType.getExtensionInterface();
		AdministrationFactory administrationFactory = adminType.getAdministrationFactory();

		// Register the administration
		AdministrationBuilder<?, ?> adminBuilder;
		if (isPreNotPost) {
			adminBuilder = functionBuilder.preAdminister(this.administrationName, extension, administrationFactory);
		} else {
			adminBuilder = functionBuilder.postAdminister(this.administrationName, extension, administrationFactory);
		}

		// Obtain the office team responsible for this administration
		OfficeTeamNode officeTeam = LinkUtil.findTarget(this, OfficeTeamNode.class, this.context.getCompilerIssues());
		if (officeTeam != null) {
			// Build the team responsible for the administrator
			adminBuilder.setResponsibleTeam(officeTeam.getOfficeTeamName());
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
				managedObject = LinkUtil.retrieveTarget(linkObject, BoundManagedObjectNode.class,
						this.context.getCompilerIssues());
			}
			if (managedObject == null) {
				continue; // must have managed object
			}

			// Build administration of the managed object
			adminBuilder.administerManagedObject(managedObject.getBoundManagedObjectName());
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
		return LinkUtil.linkTeamNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedTeamNode = link);
	}

	@Override
	public LinkTeamNode getLinkedTeamNode() {
		return this.linkedTeamNode;
	}

}