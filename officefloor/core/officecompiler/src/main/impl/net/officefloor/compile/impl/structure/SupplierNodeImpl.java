/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.compile.impl.structure;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireLink;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectExtensionNode;
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
import net.officefloor.compile.spi.office.OfficeSupplierThreadLocal;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplierThreadLocal;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.supplier.InitialSupplierType;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierLoader;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorEvent;
import net.officefloor.frame.api.build.OfficeFloorListener;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.thread.ThreadSynchroniser;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;
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
	 * {@link ThreadSynchroniserFactory} instances.
	 */
	private ThreadSynchroniserFactory[] threadSynchronisers;

	/**
	 * {@link InternalSupplier} instances.
	 */
	private InternalSupplier[] internalSuppliers;

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
	 * Added {@link ManagedObjectSourceNode} instances.
	 */
	private final Map<String, Object> addedManagedObjectSources = new HashMap<>();

	/**
	 * {@link InitialSupplierType}.
	 */
	private InitialSupplierType initialSupplierType = null;

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

	/**
	 * Obtains the {@link OfficeFloorManagedObjectSource} /
	 * {@link OfficeManagedObjectSource}.
	 *
	 * @param <S>       {@link OfficeFloorManagedObjectSource} /
	 *                  {@link OfficeManagedObjectSource} type.
	 * @param qualifier Qualifier. May be <code>null</code>.
	 * @param type      Type.
	 * @param factory   Factory to create the {@link OfficeFloorManagedObjectSource}
	 *                  / {@link OfficeManagedObjectSource}.
	 * @return {@link OfficeFloorManagedObjectSource} /
	 *         {@link OfficeManagedObjectSource}.
	 */
	@SuppressWarnings("unchecked")
	public <S> S getManagedObjectSource(String qualifier, String type,
			Function<SuppliedManagedObjectSourceNode, S> factory) {

		// Determine if already added
		String name = SuppliedManagedObjectSourceNodeImpl.getSuppliedManagedObjectSourceName(qualifier, type);
		S existing = (S) this.addedManagedObjectSources.get(name);
		if (existing != null) {
			return existing;
		}

		// Not yet added, so create the supplied managed object node
		SuppliedManagedObjectSourceNode suppliedManagedObjectNode = NodeUtil.getNode(name, this.suppliedManagedObjects,
				() -> this.context.createSuppliedManagedObjectSourceNode(qualifier, type, this));

		// Add and register the managed object source
		S added = factory.apply(suppliedManagedObjectNode);
		this.addedManagedObjectSources.put(name, added);

		// Return the managed object source
		return added;
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
	public OfficeFloorSupplierThreadLocal getOfficeFloorSupplierThreadLocal(String qualifier, String type) {
		String name = SupplierThreadLocalNodeImpl.getSupplierThreadLocalName(qualifier, type);
		return NodeUtil.getNode(name, this.supplierThreadLocals,
				() -> this.context.createSupplierThreadLocalNode(qualifier, type, this));
	}

	@Override
	public OfficeFloorManagedObjectSource getOfficeFloorManagedObjectSource(String managedObjectSourceName,
			String qualifier, String type) {
		return this.getManagedObjectSource(qualifier, type, (suppliedManagedObjectNode) -> this.officeFloorNode
				.addManagedObjectSource(managedObjectSourceName, suppliedManagedObjectNode));
	}

	/*
	 * ================== OfficeSupplier ========================
	 */

	@Override
	public String getOfficeSupplierName() {
		return this.supplierName;
	}

	@Override
	public OfficeSupplierThreadLocal getOfficeSupplierThreadLocal(String qualifier, String type) {
		String name = SupplierThreadLocalNodeImpl.getSupplierThreadLocalName(qualifier, type);
		return NodeUtil.getNode(name, this.supplierThreadLocals,
				() -> this.context.createSupplierThreadLocalNode(qualifier, type, this));
	}

	@Override
	public OfficeManagedObjectSource getOfficeManagedObjectSource(String managedObjectSourceName, String qualifier,
			String type) {
		return this.getManagedObjectSource(qualifier, type, (suppliedManagedObjectNode) -> this.officeNode
				.addManagedObjectSource(managedObjectSourceName, suppliedManagedObjectNode));
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
	public InitialSupplierType loadInitialSupplierType(boolean isLoadingType) {

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
		SupplierLoader loader = this.context.getSupplierLoader(this, isLoadingType);
		return loader.loadInitialSupplierType(supplierSource, this.propertyList);
	}

	@Override
	public SupplierType loadSupplierType(CompileContext compileContext, boolean isLoadingType,
			AvailableType[] availableTypes) {

		// Obtain the initial supplier type
		InitialSupplierType initialSupplierType = compileContext.getOrLoadInitialSupplierType(this);
		if (initialSupplierType == null) {
			return null; // must load initial type first
		}

		// Load and return the type
		SupplierLoader loader = this.context.getSupplierLoader(this, isLoadingType);
		return loader.loadSupplierType(initialSupplierType, availableTypes);
	}

	@Override
	public void registerAsPossibleMBean(CompileContext compileContext) {
		compileContext.registerPossibleMBean(SupplierSource.class, this.supplierName, this.usedSupplierSource);
	}

	@Override
	public void loadAutoWireObjects(AutoWirer<LinkObjectNode> autoWirer,
			ManagedObjectSourceVisitor managedObjectSourceVisitor, CompileContext compileContext) {
		this.loadAutoWireObjects(compileContext, managedObjectSourceVisitor,
				(suppliedMosType, moType, targetNodeFactory) -> {

					// Load the auto-wire objects
					autoWirer.addAutoWireTarget(targetNodeFactory,
							new AutoWire(suppliedMosType.getQualifier(), suppliedMosType.getObjectType()));

				}, (mos, office) -> {
					// nothing to decorate
				});
	}

	@Override
	public void loadAutoWireExtensions(AutoWirer<ManagedObjectExtensionNode> autoWirer,
			ManagedObjectSourceVisitor managedObjectSourceVisitor, CompileContext compileContext) {
		this.loadAutoWireObjects(compileContext, managedObjectSourceVisitor,
				(suppliedMosType, moType, targetNodeFactory) -> {

					// Load the auto-wire extensions
					for (Class<?> extensionType : moType.getExtensionTypes()) {
						autoWirer.addAutoWireTarget(targetNodeFactory, new AutoWire(extensionType));
					}

				},
				(mos, office) -> {
					// Decorate to the office
					mos.autoWireToOffice(office, this.context.getCompilerIssues());
				});
	}

	/**
	 * {@link FunctionalInterface} to load the auto-wire objects.
	 */
	@FunctionalInterface
	private static interface AutoWireObjectLoader<N extends Node> {

		/**
		 * Loads the auto-wire object.
		 * 
		 * @param suppliedMosType   {@link SuppliedManagedObjectSourceType}.
		 * @param moType            {@link ManagedObjectType}.
		 * @param targetNodeFactory {@link Function} to obtain the target {@link Node}.
		 */
		void load(SuppliedManagedObjectSourceType suppliedMosType, ManagedObjectType<?> moType,
				Function<OfficeNode, N> targetNodeFactory);
	}

	/**
	 * Loads the auto-wire objects.
	 * 
	 * @param compileContext             {@link CompileContext}.
	 * @param managedObjectSourceVisitor {@link ManagedObjectSourceVisitor}.
	 * @param autoWireLoader             {@link AutoWireObjectLoader}.
	 * @param mosDecorator               Decorates the
	 *                                   {@link ManagedObjectSourceNode}.
	 */
	private void loadAutoWireObjects(CompileContext compileContext,
			ManagedObjectSourceVisitor managedObjectSourceVisitor,
			AutoWireObjectLoader<ManagedObjectNode> autoWireLoader,
			BiConsumer<ManagedObjectSourceNode, OfficeNode> mosDecorator) {

		// Load the supplier type
		SupplierType supplierType = compileContext.getOrLoadSupplierType(this);
		if (supplierType == null) {
			return; // must have type
		}

		// Load the supplied managed objects for auto-wiring
		Arrays.stream(supplierType.getSuppliedManagedObjectTypes()).forEach((suppliedMosType) -> {

			// Obtain the managed object name
			String qualifier = suppliedMosType.getQualifier();
			String type = suppliedMosType.getObjectType().getName();
			String managedObjectName = (qualifier == null ? "" : qualifier + "-") + type;

			// Determine if flows for managed object
			ManagedObjectSourceNode mosNode = this.officeNode != null
					? this.context.createManagedObjectSourceNode(managedObjectName, this.officeNode)
					: this.context.createManagedObjectSourceNode(managedObjectName, this.officeFloorNode);
			ManagedObjectLoader loader = this.context.getManagedObjectLoader(mosNode);
			ManagedObjectType<?> moType = loader.loadManagedObjectType(suppliedMosType.getManagedObjectSource(),
					suppliedMosType.getPropertyList());
			if ((moType.getFlowTypes().length > 0) || (moType.getTeamTypes().length > 0)) {
				return; // can not auto-wire input managed object
			}

			// Load the supplied managed object source
			autoWireLoader.load(suppliedMosType, moType, (office) -> {

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
				mosDecorator.accept(mos, office);
				mos.sourceManagedObjectSource(managedObjectSourceVisitor, compileContext);
				mo.sourceManagedObject(compileContext);

				// Return the managed object
				return mo;
			});
		});
	}

	@Override
	public boolean sourceSupplier(CompileContext compileContext) {

		// Load the initial supplier type
		this.initialSupplierType = compileContext.getOrLoadInitialSupplierType(this);
		if (this.initialSupplierType == null) {
			return false; // must have type
		}

		// Successfully sourced
		return true;
	}

	@Override
	public boolean sourceComplete(CompileContext compileContext) {

		// Load the completed supplier type
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

		// Load the thread synchronisers
		this.threadSynchronisers = supplierType.getThreadSynchronisers();

		// Load the internal suppliers
		this.internalSuppliers = supplierType.getInternalSuppliers();

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

		// Register for termination
		SupplierSource terminateSupplierSource = this.usedSupplierSource;
		this.officeFloorNode.addOfficeFloorListener(new OfficeFloorListener() {

			@Override
			public void officeFloorOpened(OfficeFloorEvent event) throws Exception {
				// Nothing on startup, as supplier type triggered
			}

			@Override
			public void officeFloorClosed(OfficeFloorEvent event) throws Exception {
				// Terminate the supplier
				terminateSupplierSource.terminate();
			}
		});

		// Successfully sourced
		return true;
	}

	@Override
	public boolean ensureNoThreadLocals(CompileContext compileContext) {

		// Ensure no thread locals
		boolean[] isNoThreadLocals = new boolean[] { true };
		this.supplierThreadLocals.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeFloorSupplierThreadLocalName(),
						b.getOfficeFloorSupplierThreadLocalName()))
				.forEachOrdered((threadLocal) -> {

					// Flag that have thread local
					isNoThreadLocals[0] = false;

					// Add issue, as should not have thread local
					this.context.getCompilerIssues().addIssue(this,
							"Should not have " + threadLocal.getNodeType() + " ("
									+ threadLocal.getOfficeFloorSupplierThreadLocalName() + ") registered, as "
									+ SupplierSource.class.getSimpleName() + " registered at "
									+ OfficeFloor.class.getSimpleName());
				});

		// Ensure no thread synchronisers
		boolean isNoThreadSynchronisers = (this.threadSynchronisers.length == 0);
		if (!isNoThreadSynchronisers) {
			// Add issue, as should not have thread synchroniser
			this.context.getCompilerIssues().addIssue(this,
					"Should not have " + ThreadSynchroniser.class.getSimpleName() + " registered, as "
							+ SupplierSource.class.getSimpleName() + " registered at "
							+ OfficeFloor.class.getSimpleName());
		}

		// Return if no thread locals and thread synchronisers
		return isNoThreadLocals[0] && isNoThreadSynchronisers;
	}

	@Override
	public void autoWireObjects(AutoWirer<LinkObjectNode> autoWirer, OfficeNode office, CompileContext compileContext) {

		// Auto-wire thread locals
		this.supplierThreadLocals.values().stream().sorted((a, b) -> CompileUtil
				.sortCompare(a.getOfficeSupplierThreadLocalName(), b.getOfficeSupplierThreadLocalName()))
				.forEachOrdered((threadLocal) -> {

					// Ignore if already configured
					if (threadLocal.getLinkedObjectNode() != null) {
						return;
					}

					// Auto-wire the thread local
					AutoWireLink<SupplierThreadLocalNode, LinkObjectNode>[] links = autoWirer.getAutoWireLinks(
							threadLocal, new AutoWire(threadLocal.getQualifier(), threadLocal.getType()));
					if (links.length == 1) {
						LinkUtil.linkAutoWireObjectNode(threadLocal, links[0].getTargetNode(office), office, autoWirer,
								compileContext, this.context.getCompilerIssues(),
								(link) -> threadLocal.linkObjectNode(link));
					}
				});
	}

	@Override
	public void buildSupplier(OfficeBuilder officeBuilder, CompileContext compileContext) {

		// Build the supplier thread locals
		this.supplierThreadLocals.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeFloorSupplierThreadLocalName(),
						b.getOfficeFloorSupplierThreadLocalName()))
				.forEachOrdered((threadLocal) -> threadLocal.buildSupplierThreadLocal(compileContext));

		// Add the thread synchronisers
		for (ThreadSynchroniserFactory threadSynchroniser : this.threadSynchronisers) {
			officeBuilder.addThreadSynchroniser(threadSynchroniser);
		}
	}

	@Override
	public InternalSupplier[] getInternalSuppliers() {
		return this.internalSuppliers;
	}

}
