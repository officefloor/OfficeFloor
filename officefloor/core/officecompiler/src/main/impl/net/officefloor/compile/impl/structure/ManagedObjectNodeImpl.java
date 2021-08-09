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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.impl.section.OfficeSectionManagedObjectTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.AdministrationNode;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireLink;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeBindings;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OptionalThreadLocalReceiver;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.managedobject.ManagedObjectDependencyType;
import net.officefloor.compile.managedobject.ManagedObjectFlowType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.object.DependentObjectType;
import net.officefloor.compile.object.ObjectDependencyType;
import net.officefloor.compile.section.OfficeSectionManagedObjectSourceType;
import net.officefloor.compile.section.OfficeSectionManagedObjectType;
import net.officefloor.compile.section.TypeQualification;
import net.officefloor.compile.spi.managedobject.ManagedObjectDependency;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.ExecutionManagedObject;
import net.officefloor.compile.spi.office.ExecutionObjectExplorer;
import net.officefloor.compile.spi.office.ExecutionObjectExplorerContext;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeManagedObjectDependency;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectDependency;
import net.officefloor.compile.spi.section.SectionManagedObjectDependency;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.ThreadDependencyMappingBuilder;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link ManagedObjectNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ManagedObjectNodeImpl implements ManagedObjectNode {

	/**
	 * Name of this {@link ManagedObject}.
	 */
	private final String managedObjectName;

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
	 * Containing {@link OfficeFloorNode}.
	 */
	private final OfficeFloorNode containingOfficeFloorNode;

	/**
	 * {@link ManagedObjectDependencyNode} instances by their
	 * {@link ManagedObjectDependency} names.
	 */
	private final Map<String, ManagedObjectDependencyNode> dependencies = new HashMap<String, ManagedObjectDependencyNode>();

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Initialise state.
	 */
	private InitialiseState state;

	/**
	 * Initialised state.
	 */
	private static class InitialiseState {

		/**
		 * {@link ManagedObjectScope} of this {@link ManagedObject}.
		 */
		private final ManagedObjectScope managedObjectScope;

		/**
		 * {@link ManagedObjectSourceNode} for the {@link ManagedObjectSource} to source
		 * this {@link ManagedObject}.
		 */
		private final ManagedObjectSourceNode managedObjectSourceNode;

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
		 * Containing {@link OfficeFloorNode}.
		 */
		@SuppressWarnings("unused")
		private final OfficeFloorNode containingOfficeFloorNode;

		/**
		 * Instantiate.
		 * 
		 * @param managedObjectScope      {@link ManagedObjectScope} of this
		 *                                {@link ManagedObject}.
		 * @param managedObjectSourceNode {@link ManagedObjectSourceNode} for the
		 *                                {@link ManagedObjectSource} to source this
		 *                                {@link ManagedObject}.
		 */
		public InitialiseState(ManagedObjectScope managedObjectScope, ManagedObjectSourceNode managedObjectSourceNode) {
			this.managedObjectScope = managedObjectScope;
			this.managedObjectSourceNode = managedObjectSourceNode;
			this.containingSectionNode = this.managedObjectSourceNode.getSectionNode();
			this.containingOfficeNode = (this.containingSectionNode != null ? this.containingSectionNode.getOfficeNode()
					: this.managedObjectSourceNode.getOfficeNode());
			this.containingOfficeFloorNode = (this.containingOfficeNode != null
					? this.containingOfficeNode.getOfficeFloorNode()
					: this.managedObjectSourceNode.getOfficeFloorNode());
		}
	}

	/**
	 * {@link TypeQualification} instances for this {@link ManagedObjectNode}.
	 */
	private final List<TypeQualification> typeQualifications = new LinkedList<TypeQualification>();

	/**
	 * {@link GovernanceNode} instances to provide {@link Governance} over this
	 * {@link ManagedObjectNode} within the specified {@link OfficeNode}.
	 */
	private final Map<OfficeNode, List<GovernanceNode>> governancesPerOffice = new HashMap<>();

	/**
	 * Pre-load {@link AdministrationNode} instances configured directly to this
	 * {@link ManagedObjectNode}.
	 */
	private final List<AdministrationNode> preLoadAdministrations = new LinkedList<>();

	/**
	 * Pre-load {@link AdministrationNode} instances over {@link ManagedObjectNode}
	 * within the specified {@link OfficeNode}.
	 */
	private final Map<OfficeNode, List<AdministrationNode>> preLoadAdministrationsPerOffice = new HashMap<>();

	/**
	 * {@link OptionalThreadLocalLinker}.
	 */
	private final OptionalThreadLocalLinker optionalThreadLocalLinker = new OptionalThreadLocalLinker();

	/**
	 * {@link ExecutionObjectExplorer} instances.
	 */
	private final List<ExecutionObjectExplorer> executionExplorers = new LinkedList<>();

	/**
	 * Initiate.
	 * 
	 * @param managedObjectName         Name of this {@link ManagedObject}.
	 * @param containingSectionNode     {@link SectionNode} containing this
	 *                                  {@link ManagedObjectNode}. <code>null</code>
	 *                                  if contained in the {@link Office} or
	 *                                  {@link OfficeFloor}.
	 * @param containingOfficeNode      {@link OfficeNode} containing this
	 *                                  {@link ManagedObjectNode}. <code>null</code>
	 *                                  if contained in the {@link OfficeFloor}.
	 * @param containingOfficeFloorNode {@link OfficeFloorNode} containing this
	 *                                  {@link ManagedObjectNode}.
	 * @param context                   {@link NodeContext}.
	 */
	public ManagedObjectNodeImpl(String managedObjectName, SectionNode containingSectionNode,
			OfficeNode containingOfficeNode, OfficeFloorNode containingOfficeFloorNode, NodeContext context) {
		this.managedObjectName = managedObjectName;
		this.containingSectionNode = containingSectionNode;
		this.containingOfficeNode = containingOfficeNode;
		this.containingOfficeFloorNode = containingOfficeFloorNode;
		this.context = context;
	}

	/*
	 * ===================== Node ===========================
	 */

	@Override
	public String getNodeName() {
		return this.managedObjectName;
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
		return (this.containingSectionNode != null) ? this.containingSectionNode
				: ((this.containingOfficeNode != null) ? this.containingOfficeNode : this.containingOfficeFloorNode);
	}

	@Override
	public String getQualifiedName() {

		// Obtain name
		String name = Node.escape(this.managedObjectName);

		// Obtain the name based on location
		if (this.state.containingSectionNode != null) {
			// Use name qualified with both office and section
			return this.state.containingSectionNode.getQualifiedName(name);

		} else if (this.state.containingOfficeNode != null) {
			// Use name qualified with office name
			return this.state.containingOfficeNode.getQualifiedName(name);

		} else {
			// Use name unqualified
			return name;
		}
	}

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes(this.dependencies);
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise(ManagedObjectScope managedObjectScope, ManagedObjectSourceNode managedObjectSourceNode) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialiseState(managedObjectScope, managedObjectSourceNode));
	}

	/*
	 * ======================== ManagedObjectNode ============================
	 */

	@Override
	public ManagedObjectSourceNode getManagedObjectSourceNode() {
		return (this.state != null ? this.state.managedObjectSourceNode : null);
	}

	@Override
	public TypeQualification[] getTypeQualifications(CompileContext compileContext) {

		// Obtain the type qualifications
		TypeQualification[] qualifications = this.typeQualifications.stream().toArray(TypeQualification[]::new);
		if (qualifications.length == 0) {

			// No qualifications, so use managed object type
			ManagedObjectType<?> managedObjectType = compileContext
					.getOrLoadManagedObjectType(this.state.managedObjectSourceNode);
			if (managedObjectType == null) {
				return null; // must have type
			}

			// Use the managed object type
			qualifications = new TypeQualification[] {
					new TypeQualificationImpl(null, managedObjectType.getObjectType().getName()) };
		}
		return qualifications;
	}

	@Override
	public ManagedObjectDependencyNode[] getManagedObjectDepdendencies() {
		return this.dependencies.values().stream().sorted((a, b) -> CompileUtil
				.sortCompare(a.getManagedObjectDependencyName(), b.getManagedObjectDependencyName()))
				.toArray(ManagedObjectDependencyNode[]::new);
	}

	@Override
	public boolean sourceManagedObject(CompileContext compileContext) {

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = compileContext
				.getOrLoadManagedObjectType(this.state.managedObjectSourceNode);
		if (managedObjectType == null) {
			return false; // must have type
		}

		// Initialise the dependencies
		for (ManagedObjectDependencyType<?> dependencyType : managedObjectType.getDependencyTypes()) {
			String dependencyName = dependencyType.getDependencyName();
			NodeUtil.getInitialisedNode(dependencyName, this.dependencies, this.context,
					() -> this.context.createManagedObjectDependencyNode(dependencyName, this),
					(dependency) -> dependency.initialise());
		}

		// Successfully sourced
		return true;
	}

	@Override
	public void autoWireDependencies(AutoWirer<LinkObjectNode> autoWirer, OfficeNode office,
			CompileContext compileContext) {

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = compileContext
				.getOrLoadManagedObjectType(this.state.managedObjectSourceNode);
		if (managedObjectType == null) {
			return; // must have type
		}

		// Create the map of dependency types by names
		Map<String, ManagedObjectDependencyType<?>> dependencyTypes = new HashMap<>();
		Arrays.stream(managedObjectType.getDependencyTypes())
				.forEach((dependencyType) -> dependencyTypes.put(dependencyType.getDependencyName(), dependencyType));

		// Auto-wire dependencies
		this.dependencies.values().stream().sorted((a, b) -> CompileUtil.sortCompare(a.getManagedObjectDependencyName(),
				b.getManagedObjectDependencyName())).forEachOrdered((dependency) -> {

					// Ignore if already configured
					if (dependency.getLinkedObjectNode() != null) {
						return;
					}

					// Obtain the dependency type
					ManagedObjectDependencyType<?> dependencyType = dependencyTypes
							.get(dependency.getManagedObjectDependencyName());
					if (dependencyType == null) {
						return; // must have type
					}

					// Auto-wire the dependency
					AutoWireLink<ManagedObjectDependencyNode, LinkObjectNode>[] links = autoWirer.getAutoWireLinks(
							dependency,
							new AutoWire(dependencyType.getTypeQualifier(), dependencyType.getDependencyType()));
					if (links.length == 1) {
						LinkUtil.linkAutoWireObjectNode(dependency, links[0].getTargetNode(office), office, autoWirer,
								compileContext, this.context.getCompilerIssues(),
								(link) -> dependency.linkObjectNode(link));
					}
				});
	}

	@Override
	public ExecutionManagedObject createExecutionManagedObject(CompileContext compileContext) {
		return new ExecutionManagedObjectImpl(this, compileContext);
	}

	@Override
	public OfficeSectionManagedObjectType loadOfficeSectionManagedObjectType(CompileContext compileContext) {

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = compileContext
				.getOrLoadManagedObjectType(this.state.managedObjectSourceNode);
		if (managedObjectType == null) {
			return null; // must have type
		}

		// Create the type qualifications
		TypeQualification[] qualifications = this.getTypeQualifications(compileContext);

		// Obtain the extension types
		Class<?>[] extensionTypes = managedObjectType.getExtensionTypes();

		// Obtain the dependencies
		ObjectDependencyType[] objectDependencyTypes = CompileUtil.loadTypes(this.dependencies,
				(dependency) -> dependency.getManagedObjectDependencyName(),
				(dependency) -> dependency.loadObjectDependencyType(compileContext), ObjectDependencyType[]::new);
		if (objectDependencyTypes == null) {
			return null;
		}

		// Load the managed object source type
		OfficeSectionManagedObjectSourceType managedObjectSourceType = this.state.managedObjectSourceNode
				.loadOfficeSectionManagedObjectSourceType(compileContext);
		if (managedObjectSourceType == null) {
			return null;
		}

		// Create and return the managed object type
		return new OfficeSectionManagedObjectTypeImpl(this.managedObjectName, qualifications, extensionTypes,
				objectDependencyTypes, managedObjectSourceType);
	}

	@Override
	public boolean runExecutionExplorers(CompileContext compileContext) {

		// Create the execution managed object
		ExecutionManagedObject executionManagedObject = this.createExecutionManagedObject(compileContext);

		// Run explorers
		for (ExecutionObjectExplorer explorer : this.executionExplorers) {
			try {
				explorer.explore(new ExecutionObjectExplorerContext() {

					@Override
					public ExecutionManagedObject getInitialManagedObject() {
						return executionManagedObject;
					}
				});
			} catch (Exception ex) {
				this.context.getCompilerIssues().addIssue(this,
						"Failure in exploring managed object " + this.getBoundManagedObjectName(), ex);
			}
		}

		// As here, successful
		return true;
	}

	/*
	 * ===================== DependentObjectNode ===========================
	 */

	@Override
	public DependentObjectType loadDependentObjectType(CompileContext compileContext) {
		return this.loadOfficeSectionManagedObjectType(compileContext);
	}

	/*
	 * ===================== BoundManagedObjectNode ===========================
	 */

	@Override
	public String getBoundManagedObjectName() {

		// No bound name until initialised
		if (this.state == null) {
			return null;
		}

		// Use the qualified name
		return this.getQualifiedName();
	}

	@Override
	public void addGovernance(GovernanceNode governance, OfficeNode office) {

		// Obtain the listing of governances for the office
		List<GovernanceNode> governances = this.governancesPerOffice.get(office);
		if (governances == null) {
			// Create and register listing to add the governance
			governances = new LinkedList<GovernanceNode>();
			this.governancesPerOffice.put(office, governances);
		}

		// Add the governance for the specified office
		governances.add(governance);
	}

	@Override
	public void addPreLoadAdministration(AdministrationNode preLoadAdministration, OfficeNode office) {

		// Obtain the listing of pre-load administration for the office
		List<AdministrationNode> preLoadAdmins = this.preLoadAdministrationsPerOffice.get(office);
		if (preLoadAdmins == null) {
			// Create and register listing to add the pre-load administration
			preLoadAdmins = new LinkedList<>();
			this.preLoadAdministrationsPerOffice.put(office, preLoadAdmins);
		}

		// Add the pre-load administration for the specified office
		preLoadAdmins.add(preLoadAdministration);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void buildOfficeManagedObject(OfficeNode office, OfficeBuilder officeBuilder, OfficeBindings officeBindings,
			CompileContext compileContext) {

		// Obtain the managed object type
		ManagedObjectType<?> managedObjectType = compileContext
				.getOrLoadManagedObjectType(this.state.managedObjectSourceNode);
		if (managedObjectType == null) {
			return; // must have managed object type
		}

		// Obtain the managed object name
		String managedObjectName = this.getBoundManagedObjectName();

		// Register to the office
		officeBuilder.registerManagedObjectSource(managedObjectName,
				this.state.managedObjectSourceNode.getQualifiedName());

		// Add the managed object to the office
		ThreadDependencyMappingBuilder mapper;
		switch (this.state.managedObjectScope) {
		case PROCESS:
			mapper = officeBuilder.addProcessManagedObject(managedObjectName, managedObjectName);
			break;
		case THREAD:
			mapper = officeBuilder.addThreadManagedObject(managedObjectName, managedObjectName);
			break;
		case FUNCTION:
			// function bound not built here
			return;
		default:
			throw new IllegalStateException("Unknown managed object scope " + this.state.managedObjectScope);
		}

		// Load the dependencies for the managed object
		for (ManagedObjectDependencyType<?> dependencyType : managedObjectType.getDependencyTypes()) {

			// Obtain the dependency type details
			String dependencyName = dependencyType.getDependencyName();
			Enum dependencyKey = dependencyType.getKey();
			int dependencyIndex = dependencyType.getIndex();

			// Obtain the dependency
			ManagedObjectDependencyNode dependencyNode = this.dependencies.get(dependencyName);
			BoundManagedObjectNode dependency = LinkUtil.retrieveTarget(dependencyNode, BoundManagedObjectNode.class,
					this.context.getCompilerIssues());
			if (dependency == null) {
				continue; // must have dependency
			}

			// Ensure the dependent managed object is built into the office
			officeBindings.buildManagedObjectIntoOffice(dependency);

			// Link the dependency
			String dependentManagedObjectName = dependency.getBoundManagedObjectName();
			if (dependencyKey != null) {
				mapper.mapDependency(dependencyKey, dependentManagedObjectName);
			} else {
				mapper.mapDependency(dependencyIndex, dependentManagedObjectName);
			}
		}

		// Load governances for the managed object from the office
		List<GovernanceNode> governances = this.governancesPerOffice.get(office);
		if (governances != null) {
			// Load the governance for the managed object
			for (GovernanceNode governance : governances) {
				mapper.mapGovernance(governance.getOfficeGovernanceName());
			}
		}

		// Load the pre-load administration for the managed object
		if (this.state.containingOfficeNode == office) {
			for (AdministrationNode administration : this.preLoadAdministrations) {
				administration.buildPreLoadManagedObjectAdministration(mapper, compileContext);
			}
		}
		List<AdministrationNode> administrations = this.preLoadAdministrationsPerOffice.get(office);
		if (administrations != null) {
			// Load the pre-load administration for the managed object
			for (AdministrationNode administration : administrations) {
				administration.buildPreLoadManagedObjectAdministration(mapper, compileContext);
			}
		}

		// Register for optional thread locals
		this.optionalThreadLocalLinker.setThreadDependencyMappingBuilder(mapper);
	}

	@Override
	public void buildSupplierThreadLocal(OptionalThreadLocalReceiver optionalThreadLocalReceiver) {
		this.optionalThreadLocalLinker.addOptionalThreadLocalReceiver(optionalThreadLocalReceiver);
	}

	/*
	 * ========================== (Common) ===============================
	 */

	@Override
	public void addTypeQualification(String qualifier, String type) {
		this.typeQualifications.add(new TypeQualificationImpl(qualifier, type));
	}

	@Override
	public void addPreLoadAdministration(OfficeAdministration administration) {

		// Ensure adminstration node
		if (!(administration instanceof AdministrationNode)) {
			// Unknown administration node
			this.context.getCompilerIssues().addIssue(this, "Illegal " + AdministrationNode.class.getSimpleName()
					+ " node - " + administration.getClass().getName());
			return;
		}

		// Add the administration node
		AdministrationNode adminNode = (AdministrationNode) administration;
		this.preLoadAdministrations.add(adminNode);
	}

	/*
	 * ==================== SectionManagedObject ===============================
	 */

	@Override
	public String getSectionManagedObjectName() {
		return this.managedObjectName;
	}

	@Override
	public SectionManagedObjectDependency getSectionManagedObjectDependency(String managedObjectDependencyName) {
		return NodeUtil.getNode(managedObjectDependencyName, this.dependencies,
				() -> this.context.createManagedObjectDependencyNode(managedObjectDependencyName, this));
	}

	/*
	 * ================ OfficeSectionManagedObject ============================
	 */

	@Override
	public String getOfficeSectionManagedObjectName() {
		return this.managedObjectName;
	}

	/*
	 * ======================= OfficeManagedObject ============================
	 */

	@Override
	public String getOfficeManagedObjectName() {
		return this.managedObjectName;
	}

	@Override
	public OfficeManagedObjectDependency getOfficeManagedObjectDependency(String managedObjectDependencyName) {
		return NodeUtil.getNode(managedObjectDependencyName, this.dependencies,
				() -> this.context.createManagedObjectDependencyNode(managedObjectDependencyName, this));
	}

	@Override
	public void addExecutionExplorer(ExecutionObjectExplorer executionObjectExplorer) {
		this.executionExplorers.add(executionObjectExplorer);
	}

	/*
	 * ==================== OfficeFloorManagedObject ===========================
	 */

	@Override
	public String getOfficeFloorManagedObjectName() {
		return this.managedObjectName;
	}

	@Override
	public OfficeFloorManagedObjectDependency getOfficeFloorManagedObjectDependency(
			String managedObjectDependencyName) {
		return NodeUtil.getNode(managedObjectDependencyName, this.dependencies,
				() -> this.context.createManagedObjectDependencyNode(managedObjectDependencyName, this));
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
	 * =================== GovernerableManagedObject =========================
	 */

	@Override
	public String getGovernerableManagedObjectName() {
		return this.managedObjectName;
	}

	/*
	 * =================== LinkObjectNode ======================================
	 */

	/**
	 * Linked {@link LinkObjectNode}.
	 */
	private LinkObjectNode linkedObjectNode = null;

	@Override
	public boolean linkObjectNode(LinkObjectNode node) {
		return LinkUtil.linkObjectNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedObjectNode = link);
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectNode;
	}

	/**
	 * {@link ExecutionManagedObject} implementation.
	 */
	private static class ExecutionManagedObjectImpl implements ExecutionManagedObject {

		/**
		 * {@link ManagedObjectNodeImpl}.
		 */
		private final ManagedObjectNodeImpl node;

		/**
		 * {@link CompileContext}.
		 */
		private final CompileContext compileContext;

		/**
		 * Instantiate.
		 * 
		 * @param node           {@link ManagedObjectNodeImpl}.
		 * @param compileContext {@link CompileContext}.
		 */
		public ExecutionManagedObjectImpl(ManagedObjectNodeImpl node, CompileContext compileContext) {
			this.node = node;
			this.compileContext = compileContext;
		}

		/*
		 * ================== ExecutionManagedObject ========================
		 */

		@Override
		public String getManagedObjectName() {
			return this.node.getBoundManagedObjectName();
		}

		@Override
		public ManagedObjectType<?> getManagedObjectType() {
			return this.node.state.managedObjectSourceNode.loadManagedObjectType(compileContext);
		}

		@Override
		public ExecutionManagedObject getManagedObject(ManagedObjectDependencyType<?> dependencyType) {

			// Obtain the dependency
			String dependencyName = dependencyType.getDependencyName();
			ManagedObjectDependencyNode dependency = this.node.dependencies.get(dependencyName);
			if (dependency == null) {
				return null;
			}

			// Obtain the managed object node
			ManagedObjectNode object = LinkUtil.retrieveTarget(dependency, ManagedObjectNode.class,
					this.node.context.getCompilerIssues());
			if (object == null) {
				return null;
			}

			// Return the execution managed object
			return object.createExecutionManagedObject(this.compileContext);
		}

		@Override
		public ExecutionManagedFunction getManagedFunction(ManagedObjectFlowType<?> flowType) {
			return this.node.state.managedObjectSourceNode.createExecutionManagedFunction(flowType,
					this.compileContext);
		}
	}

}
