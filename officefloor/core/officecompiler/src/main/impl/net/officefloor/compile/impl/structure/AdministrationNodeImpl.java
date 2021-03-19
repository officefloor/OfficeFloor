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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import net.officefloor.compile.administration.AdministrationLoader;
import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.AdministrationNode;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireLink;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedObjectExtensionNode;
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
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.ManagedFunctionBuilder;
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
		 * {@link AdministrationSource} instance to use. Should this be specified it
		 * overrides the {@link Class}.
		 */
		private final AdministrationSource<?, ?, ?> administrationSource;

		/**
		 * Instantiate.
		 * 
		 * @param administratorSourceClassName Class name of the
		 *                                     {@link AdministrationSource}.
		 * @param administratorSource          {@link AdministrationSource} instance to
		 *                                     use. Should this be specified it
		 *                                     overrides the {@link Class}.
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
	 * Flags whether to auto-wire the {@link AdministerableManagedObject} instances.
	 */
	private boolean isAutoWireExtensions = false;

	/**
	 * {@link AdministrationSource} used to source this {@link AdministrationNode}.
	 */
	private AdministrationSource<?, ?, ?> usedAdministrationSource = null;

	/**
	 * Initiate.
	 * 
	 * @param administrationName Name of this {@link OfficeAdministration}.
	 * @param officeNode         Parent {@link OfficeNode}.
	 * @param context            {@link NodeContext}.
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
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes();
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

	@Override
	public void enableAutoWireExtensions() {
		this.isAutoWireExtensions = true;
	}

	/*
	 * ===================== AdministrationNode ==============================
	 */

	@Override
	public AdministrationType<?, ?, ?> loadAdministrationType(boolean isLoadingType) {

		// Obtain the administration source
		AdministrationSource<?, ?, ?> administrationSource = this.state.administrationSource;
		if (administrationSource == null) {

			// Obtain the administration source class
			Class<? extends AdministrationSource<?, ?, ?>> administrationSourceClass = this.context
					.getAdministrationSourceClass(this.state.administrationSourceClassName, this);
			if (administrationSourceClass == null) {
				return null; // must obtain source class
			}

			// Obtain the administration source
			administrationSource = CompileUtil.newInstance(administrationSourceClass, AdministrationSource.class, this,
					this.context.getCompilerIssues());
			if (administrationSource == null) {
				return null; // must obtain source
			}
		}

		// Keep track of the administration source
		this.usedAdministrationSource = administrationSource;

		// Load and return the administration type
		AdministrationLoader loader = this.context.getAdministrationLoader(this, isLoadingType);
		return loader.loadAdministrationType(administrationSource, this.properties);
	}

	@Override
	public boolean sourceAdministration(CompileContext compileContext) {

		// Build the administration type
		AdministrationType<?, ?, ?> adminType = compileContext.getOrLoadAdministrationType(this);
		if (adminType == null) {
			return false; // must load type
		}

		// As here, successful
		return true;
	}

	@Override
	public boolean isAutoWireAdministration() {
		return this.isAutoWireExtensions;
	}

	@Override
	public void autoWireExtensions(AutoWirer<ManagedObjectExtensionNode> autoWirer, CompileContext compileContext) {

		// Do no auto wire if already extensions
		if (this.administeredManagedObjects.size() > 0) {
			return;
		}

		// Build the administration type
		AdministrationType<?, ?, ?> adminType = compileContext.getOrLoadAdministrationType(this);
		if (adminType == null) {
			return; // must load type
		}

		// Load the auto-wire extensions
		AutoWireLink<AdministrationNode, ManagedObjectExtensionNode>[] links = autoWirer.getAutoWireLinks(this,
				new AutoWire(adminType.getExtensionType()));
		for (AutoWireLink<AdministrationNode, ManagedObjectExtensionNode> link : links) {
			this.administeredManagedObjects.add(link.getTargetNode(this.officeNode));
		}
	}

	@Override
	public void autoWireTeam(AutoWirer<LinkTeamNode> autoWirer, CompileContext compileContext) {

		// Do not auto wire if already responsible team
		if (this.linkedTeamNode != null) {
			return;
		}

		// Load the source auto-wires
		Set<AutoWire> autoWires = new HashSet<>();
		this.administeredManagedObjects.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getAdministerableManagedObjectName(),
						b.getAdministerableManagedObjectName()))
				.forEachOrdered((administerable) -> LinkUtil.loadAllObjectAutoWires((LinkObjectNode) administerable,
						autoWires, compileContext, this.context.getCompilerIssues()));
		AutoWire[] sourceAutoWires = autoWires.stream().toArray(AutoWire[]::new);

		// Determine if auto wire the team
		AutoWireLink<AdministrationNode, LinkTeamNode>[] links = autoWirer.findAutoWireLinks(this, sourceAutoWires);
		if (links.length == 1) {
			LinkUtil.linkTeamNode(this, links[0].getTargetNode(this.officeNode), this.context.getCompilerIssues(),
					(link) -> this.linkTeamNode(link));
		}
	}

	@Override
	public void buildPreFunctionAdministration(ManagedFunctionBuilder<?, ?> functionBuilder,
			CompileContext compileContext) {
		this.buildAdministration(compileContext,
				(name, extensionType, factory) -> functionBuilder.preAdminister(name, extensionType, factory));
	}

	@Override
	public void buildPostFunctionAdministration(ManagedFunctionBuilder<?, ?> functionBuilder,
			CompileContext compileContext) {
		this.buildAdministration(compileContext,
				(name, extensionType, factory) -> functionBuilder.postAdminister(name, extensionType, factory));
	}

	@Override
	public void buildPreLoadManagedObjectAdministration(DependencyMappingBuilder dependencyMappingBuilder,
			CompileContext compileContext) {
		this.buildAdministration(compileContext, (name, extensionType, factory) -> dependencyMappingBuilder
				.preLoadAdminister(name, extensionType, factory));
	}

	/**
	 * {@link Function} interface to create the {@link AdministrationBuilder}.
	 */
	private static interface AdministrationBuilderFactory {

		/**
		 * Creates the {@link AdministrationBuilder}.
		 * 
		 * @param administrationName Name of the {@link Administration}.
		 * @param extensionType      Extension type.
		 * @param adminFactory       {@link AdministrationFactory}.
		 * @return {@link AdministrationBuilder}.
		 */
		AdministrationBuilder<?, ?> createAdministrationBuilder(String administrationName, Class<Object> extensionType,
				AdministrationFactory<Object, ?, ?> adminFactory);
	}

	/**
	 * Builds the {@link Administration}.
	 * 
	 * @param compileContext      {@link CompileContext}.
	 * @param adminBuilderFactory {@link AdministrationBuilderFactory}.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void buildAdministration(CompileContext compileContext, AdministrationBuilderFactory adminBuilderFactory) {

		// Build the administration type
		AdministrationType<?, ?, ?> adminType = compileContext.getOrLoadAdministrationType(this);
		if (adminType == null) {
			return; // must load type
		}

		// Register as possible MBean
		String qualifiedName = this.officeNode.getQualifiedName(this.administrationName);
		compileContext.registerPossibleMBean(AdministrationSource.class, qualifiedName, this.usedAdministrationSource);

		// Obtain the administration details
		Class extension = adminType.getExtensionType();
		AdministrationFactory administrationFactory = adminType.getAdministrationFactory();

		// Create the administration builder
		AdministrationBuilder<?, ?> adminBuilder = adminBuilderFactory
				.createAdministrationBuilder(this.administrationName, extension, administrationFactory);

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
