/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.impl.structure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceVisitor;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.internal.structure.SupplierThreadLocalNode;
import net.officefloor.compile.managedobject.ManagedObjectLoader;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplierThreadLocal;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierLoader;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link SupplierNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierNodeImpl implements SupplierNode {

	/**
	 * Name of the {@link OfficeFloorSupplier}.
	 */
	private final String supplierName;

	/**
	 * {@link OfficeNode}.
	 */
	private final OfficeNode officeNode;

	/**
	 * {@link OfficeFloorNode}.
	 */
	private final OfficeFloorNode officeFloorNode;

	/**
	 * {@link PropertyList} to source the supplier.
	 */
	private final PropertyList propertyList;

	/**
	 * {@link Map} of {@link SupplierThreadLocalNode} instances by name.
	 */
	private final Map<String, SupplierThreadLocalNode> supplierThreadLocals = new HashMap<>();

	/**
	 * {@link Map} of {@link SuppliedManagedObjectSourceNode} instances by name.
	 */
	private final Map<String, SuppliedManagedObjectSourceNode> suppliedManagedObjects = new HashMap<>();

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
		 * {@link SupplierSource} {@link Class} name.
		 */
		private final String supplierSourceClassName;

		/**
		 * Optional instantiated {@link SupplierSource}. May be <code>null</code>.
		 */
		private final SupplierSource supplierSource;

		/**
		 * Instantiate.
		 * 
		 * @param supplierSourceClassName {@link SupplierSource} {@link Class} name.
		 * @param supplierSource          Optional instantiated {@link SupplierSource}.
		 *                                May be <code>null</code>.
		 */
		public InitialisedState(String supplierSourceClassName, SupplierSource supplierSource) {
			this.supplierSourceClassName = supplierSourceClassName;
			this.supplierSource = supplierSource;
		}
	}

	/**
	 * Used {@link SupplierSource}.
	 */
	private SupplierSource usedSupplierSource = null;

	/**
	 * Instantiate.
	 * 
	 * @param supplierName    Name of the {@link OfficeFloorSupplier}.
	 * @param officeNode      {@link OfficeNode}.
	 * @param officeFloorNode {@link OfficeFloorNode}.
	 * @param context         {@link NodeContext}.
	 */
	public SupplierNodeImpl(String supplierName, OfficeNode officeNode, OfficeFloorNode officeFloorNode,
			NodeContext context) {
		this.supplierName = supplierName;
		this.officeNode = officeNode;
		this.officeFloorNode = officeFloorNode;
		this.context = context;

		// Create the additional objects
		this.propertyList = this.context.createPropertyList();
	}

	/*
	 * =============== Node ======================
	 */

	@Override
	public String getNodeName() {
		return this.supplierName;
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
		return (this.officeNode != null ? this.officeNode : this.officeFloorNode);
	}

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes(this.supplierThreadLocals, this.suppliedManagedObjects);
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise(String supplierSourceClassName, SupplierSource supplierSource) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(supplierSourceClassName, supplierSource));
	}

	/*
	 * =============== OfficeFloorSupplier ======================
	 */

	@Override
	public String getOfficeFloorSupplierName() {
		return this.supplierName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.propertyList.addProperty(name).setValue(value);
	}

	@Override
	public OfficeFloorManagedObjectSource getOfficeFloorManagedObjectSource(String managedObjectSourceName,
			String qualifier, String type) {

		// Create the supplied managed object node
		String name = SuppliedManagedObjectSourceNodeImpl.getSuppliedManagedObjectSourceName(qualifier, type);
		SuppliedManagedObjectSourceNode suppliedManagedObjectNode = NodeUtil.getNode(name, this.suppliedManagedObjects,
				() -> this.context.createSuppliedManagedObjectSourceNode(qualifier, type, this));

		// Add and return the managed object source
		return this.officeFloorNode.addManagedObjectSource(managedObjectSourceName, suppliedManagedObjectNode);
	}

	/*
	 * ================== OfficeSupplier ========================
	 */

	@Override
	public String getOfficeSupplierName() {
		return this.supplierName;
	}

	@Override
	public OfficeFloorSupplierThreadLocal getOfficeFloorSupplierThreadLocal(String qualifier, String type) {
		String name = SupplierThreadLocalNodeImpl.getSupplierThreadLocalName(qualifier, type);
		return NodeUtil.getNode(name, this.supplierThreadLocals,
				() -> this.context.createSupplierThreadLocalNode(qualifier, type, this));
	}

	@Override
	public OfficeManagedObjectSource getOfficeManagedObjectSource(String managedObjectSourceName, String qualifier,
			String type) {

		// Create the supplied managed object node
		String name = SuppliedManagedObjectSourceNodeImpl.getSuppliedManagedObjectSourceName(qualifier, type);
		SuppliedManagedObjectSourceNode suppliedManagedObjectNode = NodeUtil.getNode(name, this.suppliedManagedObjects,
				() -> this.context.createSuppliedManagedObjectSourceNode(qualifier, type, this));

		// Add and return the managed object source
		return this.officeNode.addManagedObjectSource(managedObjectSourceName, suppliedManagedObjectNode);
	}

	/*
	 * =================== SupplierNode =========================
	 */

	@Override
	public OfficeNode getOfficeNode() {
		return this.officeNode;
	}

	@Override
	public OfficeFloorNode getOfficeFloorNode() {
		return this.officeFloorNode;
	}

	@Override
	public SupplierType loadSupplierType() {

		// Obtain the supplier source
		SupplierSource supplierSource = this.state.supplierSource;
		if (supplierSource == null) {

			// Obtain the supplier source class
			Class<? extends SupplierSource> supplierSourceClass = this.context
					.getSupplierSourceClass(this.state.supplierSourceClassName, this);
			if (supplierSourceClass == null) {
				return null; // must obtain class
			}

			// Load the supplier source
			supplierSource = CompileUtil.newInstance(supplierSourceClass, SupplierSource.class, this,
					this.context.getCompilerIssues());
			if (supplierSource == null) {
				return null; // must obtain supplier source
			}
		}

		// Keep track of the used supplier source
		this.usedSupplierSource = supplierSource;

		// Load and return the type
		SupplierLoader loader = this.context.getSupplierLoader(this);
		return loader.loadSupplierType(supplierSource, this.propertyList);
	}

	@Override
	public void registerAsPossibleMBean(CompileContext compileContext) {
		compileContext.registerPossibleMBean(SupplierSource.class, this.supplierName, this.usedSupplierSource);
	}

	@Override
	public void loadAutoWireObjects(AutoWirer<LinkObjectNode> autoWirer,
			ManagedObjectSourceVisitor managedObjectSourceVisitor, CompileContext compileContext) {

		// Load the supplier type
		SupplierType supplierType = compileContext.getOrLoadSupplierType(this);
		if (supplierType == null) {
			return; // must have type
		}

		// Load the supplied managed objects for auto-wiring
		Arrays.stream(supplierType.getSuppliedManagedObjectTypes()).forEach((suppliedMosType) -> {

			// Determine if flows for managed object
			ManagedObjectLoader loader = this.context.getManagedObjectLoader(this);
			ManagedObjectType<?> moType = loader.loadManagedObjectType(suppliedMosType.getManagedObjectSource(),
					suppliedMosType.getPropertyList());
			if ((moType.getFlowTypes().length > 0) || (moType.getTeamTypes().length > 0)) {
				return; // can not auto-wire input managed object
			}

			// Register the supplied managed object source
			autoWirer.addAutoWireTarget((office) -> {

				// Obtain the managed object name
				String qualifier = suppliedMosType.getQualifier();
				String type = suppliedMosType.getObjectType().getName();
				String managedObjectName = (qualifier == null ? "" : qualifier + "-") + type;

				// Determine if office supplier
				ManagedObjectSourceNode mos;
				ManagedObjectNode mo;
				if (this.officeNode != null) {
					// Register the office managed object source
					mos = (ManagedObjectSourceNode) this.getOfficeManagedObjectSource(managedObjectName, qualifier,
							type);

					// Add the office managed object
					mo = (ManagedObjectNode) mos.addOfficeManagedObject(managedObjectName, ManagedObjectScope.THREAD);

				} else {
					// Register the OfficeFloor managed object source
					mos = (ManagedObjectSourceNode) this.getOfficeFloorManagedObjectSource(managedObjectName, qualifier,
							type);

					// Add the OfficeFloor managed object
					mo = (ManagedObjectNode) mos.addOfficeFloorManagedObject(managedObjectName,
							ManagedObjectScope.THREAD);
				}

				// Source the managed object source and managed object
				mos.sourceManagedObjectSource(managedObjectSourceVisitor, compileContext);
				mo.sourceManagedObject(compileContext);

				// Return the managed object
				return mo;

			}, new AutoWire(suppliedMosType.getQualifier(), suppliedMosType.getObjectType()));
		});
	}

	@Override
	public boolean sourceSupplier(CompileContext compileContext) {

		// Load the supplier type
		SupplierType supplierType = compileContext.getOrLoadSupplierType(this);
		if (supplierType == null) {
			return false; // must have type
		}

		// Load the supplier thread locals
		for (SupplierThreadLocalType threadLocalType : supplierType.getSupplierThreadLocalTypes()) {
			String qualifier = threadLocalType.getQualifier();
			Class<?> type = threadLocalType.getObjectType();
			String threadLocalName = SupplierThreadLocalNodeImpl.getSupplierThreadLocalName(qualifier, type.getName());
			NodeUtil.getInitialisedNode(threadLocalName, this.supplierThreadLocals, this.context,
					() -> this.context.createSupplierThreadLocalNode(qualifier, type.getName(), this),
					(node) -> node.initialise(threadLocalType));
		}

		// Load the supplied managed objects
		for (SuppliedManagedObjectSourceType mosType : supplierType.getSuppliedManagedObjectTypes()) {
			String qualifier = mosType.getQualifier();
			Class<?> type = mosType.getObjectType();
			String mosName = SuppliedManagedObjectSourceNodeImpl.getSuppliedManagedObjectSourceName(qualifier,
					type.getName());
			NodeUtil.getInitialisedNode(mosName, this.suppliedManagedObjects, this.context,
					() -> this.context.createSuppliedManagedObjectSourceNode(qualifier, type.getName(), this),
					(mos) -> mos.initialise());
		}

		// Successfully sourced
		return true;
	}

	@Override
	public void buildSupplier(CompileContext compileContext) {

		// Build the supplier thread locals
		this.supplierThreadLocals.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeFloorSupplierThreadLocalName(),
						b.getOfficeFloorSupplierThreadLocalName()))
				.forEachOrdered((threadLocal) -> threadLocal.buildSupplierThreadLocal(compileContext));
	}

}