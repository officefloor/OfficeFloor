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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.officefloor.compile.impl.office.OfficeSourceContextImpl;
import net.officefloor.compile.impl.office.OfficeTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.impl.util.LoadTypeError;
import net.officefloor.compile.internal.structure.AdministrationNode;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireDirection;
import net.officefloor.compile.internal.structure.AutoWireLink;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.AutoWirerVisitor;
import net.officefloor.compile.internal.structure.BoundManagedObjectNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.EscalationNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.LinkOfficeNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedFunctionNode;
import net.officefloor.compile.internal.structure.ManagedFunctionVisitor;
import net.officefloor.compile.internal.structure.ManagedObjectExtensionNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectPoolNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.ManagedObjectSourceVisitor;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeBindings;
import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.internal.structure.OfficeInputNode;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.internal.structure.OfficeOutputNode;
import net.officefloor.compile.internal.structure.OfficeStartNode;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.internal.structure.SectionNode;
import net.officefloor.compile.internal.structure.SuppliedManagedObjectSourceNode;
import net.officefloor.compile.internal.structure.SupplierNode;
import net.officefloor.compile.internal.structure.TeamNode;
import net.officefloor.compile.issues.CompileError;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.office.OfficeAvailableSectionInputType;
import net.officefloor.compile.office.OfficeInputType;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.office.OfficeOutputType;
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.administration.source.AdministrationSource;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.AugmentedFunctionObject;
import net.officefloor.compile.spi.office.CompletionExplorer;
import net.officefloor.compile.spi.office.EscalationExplorer;
import net.officefloor.compile.spi.office.EscalationExplorerContext;
import net.officefloor.compile.spi.office.ExecutionManagedFunction;
import net.officefloor.compile.spi.office.ManagedFunctionAugmentor;
import net.officefloor.compile.spi.office.ManagedFunctionAugmentorContext;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeDependencyObjectNode;
import net.officefloor.compile.spi.office.OfficeDependencyRequireNode;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeFlowSinkNode;
import net.officefloor.compile.spi.office.OfficeFlowSourceNode;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeInput;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObjectPool;
import net.officefloor.compile.spi.office.OfficeManagedObjectSource;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.compile.spi.office.OfficeOutput;
import net.officefloor.compile.spi.office.OfficeResponsibility;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionTransformer;
import net.officefloor.compile.spi.office.OfficeStart;
import net.officefloor.compile.spi.office.OfficeSupplier;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.section.source.SectionSource;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.InternalSupplier;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.api.source.AbstractSourceError;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link OfficeNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeNodeImpl implements OfficeNode, ManagedFunctionVisitor {

	/**
	 * Name of this {@link DeployedOffice}.
	 */
	private final String officeName;

	/**
	 * Additional profiles.
	 */
	private final List<String> additionalProfiles = new LinkedList<>();

	/**
	 * {@link PropertyList} to source the {@link Office}.
	 */
	private final PropertyList properties;

	/**
	 * {@link PropertyList} to override {@link Office} {@link Node} {@link Property}
	 * instances.
	 */
	private final PropertyList overrideProperties;

	/**
	 * Parent {@link OfficeFloorNode}.
	 */
	private final OfficeFloorNode officeFloor;

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
		 * Class name of the {@link OfficeSource}.
		 */
		private final String officeSourceClassName;

		/**
		 * {@link OfficeSource} instance to use rather than instantiating the
		 * {@link OfficeSource} class.
		 */
		private final OfficeSource officeSource;

		/**
		 * Location of the {@link Office}.
		 */
		private final String officeLocation;

		/**
		 * Instantiate.
		 * 
		 * @param officeSourceClassName Class name of the {@link OfficeSource}.
		 * @param officeSource          {@link OfficeSource} instance to use rather than
		 *                              instantiating the {@link OfficeSource} class.
		 * @param officeLocation        Location of the {@link Office}.
		 */
		public InitialisedState(String officeSourceClassName, OfficeSource officeSource, String officeLocation) {
			this.officeSourceClassName = officeSourceClassName;
			this.officeSource = officeSource;
			this.officeLocation = officeLocation;
		}
	}

	/**
	 * {@link OfficeObjectNode} instances by their {@link OfficeObject} name.
	 */
	private final Map<String, OfficeObjectNode> objects = new HashMap<String, OfficeObjectNode>();

	/**
	 * {@link OfficeTeamNode} instances by their {@link OfficeTeam} name.
	 */
	private final Map<String, OfficeTeamNode> teams = new HashMap<String, OfficeTeamNode>();

	/**
	 * {@link OfficeInputNode} instances by their name.
	 */
	private final Map<String, OfficeInputNode> inputs = new HashMap<String, OfficeInputNode>();

	/**
	 * {@link OfficeOutputNode} instances by their name.
	 */
	private final Map<String, OfficeOutputNode> outputs = new HashMap<String, OfficeOutputNode>();

	/**
	 * {@link SectionNode} instances by their {@link OfficeSection} name.
	 */
	private final Map<String, SectionNode> sections = new HashMap<String, SectionNode>();

	/**
	 * {@link ManagedObjectSourceNode} instances by their
	 * {@link OfficeManagedObjectSource} name.
	 */
	private final Map<String, ManagedObjectSourceNode> managedObjectSources = new HashMap<String, ManagedObjectSourceNode>();

	/**
	 * {@link ManagedObjectPoolNode} instances by their
	 * {@link OfficeManagedObjectPool} name.
	 */
	private final Map<String, ManagedObjectPoolNode> managedObjectPools = new HashMap<>();

	/**
	 * {@link SupplierNode} instances by their {@link Supplier} name.
	 */
	private final Map<String, SupplierNode> suppliers = new HashMap<>();

	/**
	 * {@link ManagedObjectNode} instances by their {@link OfficeManagedObject}
	 * name.
	 */
	private final Map<String, ManagedObjectNode> managedObjects = new HashMap<String, ManagedObjectNode>();

	/**
	 * {@link AdministrationNode} instances by their {@link OfficeAdministration}
	 * name.
	 */
	private final Map<String, AdministrationNode> administrators = new HashMap<String, AdministrationNode>();

	/**
	 * {@link GovernanceNode} instances by their {@link OfficeGovernance} name.
	 */
	private final Map<String, GovernanceNode> governances = new HashMap<String, GovernanceNode>();

	/**
	 * {@link EscalationNode} instances by their {@link OfficeEscalation} type.
	 */
	private final Map<String, EscalationNode> escalations = new HashMap<String, EscalationNode>();

	/**
	 * {@link EscalationExplorer} instances.
	 */
	private final List<EscalationExplorer> escalationExplorers = new LinkedList<>();

	/**
	 * {@link CompletionExplorer} instances.
	 */
	private final List<CompletionExplorer> completionExplorers = new LinkedList<>();

	/**
	 * {@link OfficeStartNode} instances by their {@link OfficeStart} name.
	 */
	private final Map<String, OfficeStartNode> starts = new HashMap<String, OfficeStartNode>();

	/**
	 * {@link OfficeSectionTransformer} instances.
	 */
	private final List<OfficeSectionTransformer> officeSectionTransformers = new LinkedList<>();

	/**
	 * {@link ManagedFunctionAugmentor} instances.
	 */
	private final List<ManagedFunctionAugmentor> managedFunctionAugmentors = new LinkedList<>();

	/**
	 * Indicates whether to {@link AutoWire} the objects.
	 */
	private boolean isAutoWireObjects = false;

	/**
	 * Indicates whether to {@link AutoWire} the {@link Team} instances.
	 */
	private boolean isAutoWireTeams = false;

	/**
	 * {@link OfficeSource} used to source this {@link OfficeNode}.
	 */
	private OfficeSource usedOfficeSource = null;

	/**
	 * Initialise with all parameters.
	 * 
	 * @param officeName  Name of the {@link DeployedOffice}.
	 * @param officeFloor Parent {@link OfficeFloorNode}.
	 * @param context     {@link NodeContext}.
	 */
	public OfficeNodeImpl(String officeName, OfficeFloorNode officeFloor, NodeContext context) {
		this.officeName = officeName;
		this.officeFloor = officeFloor;
		this.context = context;

		// Create additional objects
		this.properties = this.context.createPropertyList();
		this.overrideProperties = this.context.createPropertyList();
	}

	/**
	 * Adds a {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceName      Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSourceClassName {@link Class} name of the
	 *                                     {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectSourceNode}.
	 */
	private ManagedObjectSourceNode addManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName, this.managedObjectSources, this.context,
				() -> this.context.createManagedObjectSourceNode(managedObjectSourceName, this),
				(managedObjectSource) -> managedObjectSource.initialise(managedObjectSourceClassName, null));
	}

	/**
	 * Adds a {@link ManagedObjectSourceNode}.
	 * 
	 * @param managedObjectSourceName Name of the {@link ManagedObjectSource}.
	 * @param managedObjectSource     {@link ManagedObjectSource}.
	 * @return {@link ManagedObjectSourceNode}.
	 */
	private ManagedObjectSourceNode addManagedObjectSource(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName, this.managedObjectSources, this.context,
				() -> this.context.createManagedObjectSourceNode(managedObjectSourceName, this),
				(managedObjectSourceNode) -> managedObjectSourceNode
						.initialise(managedObjectSource.getClass().getName(), managedObjectSource));
	}

	/**
	 * Creates the {@link OfficeSourceContext}.
	 * 
	 * @param isLoadingType Indicates if loading type.
	 * @return {@link OfficeSourceContext}.
	 */
	private OfficeSourceContextImpl createOfficeSourceContext(boolean isLoadingType) {

		// Obtain the overridden properties
		PropertyList overriddenProperties = this.context.overrideProperties(this, this.officeName, this,
				this.properties);
		overriddenProperties = this.context.overrideProperties(this, this.officeName, this.officeFloor,
				overriddenProperties);

		// Create the office source context
		String[] additionalProfiles = this.context.additionalProfiles(this);
		OfficeSourceContextImpl context = new OfficeSourceContextImpl(isLoadingType, this.state.officeLocation,
				additionalProfiles, overriddenProperties, this, this.context);

		// Return the context
		return context;
	}

	/*
	 * ================== Node ===================================
	 */

	@Override
	public String getNodeName() {
		return this.officeName;
	}

	@Override
	public String getNodeType() {
		return TYPE;
	}

	@Override
	public String getLocation() {
		return (this.state == null ? "[NOT INITIALISED]"
				: NodeUtil.getLocation(this.state.officeSourceClassName, this.state.officeSource,
						this.state.officeLocation));
	}

	@Override
	public Node getParentNode() {
		return this.officeFloor;
	}

	@Override
	public Node[] getChildNodes() {
		return NodeUtil.getChildNodes(this.inputs, this.outputs, this.objects, this.sections, this.teams,
				this.suppliers, this.managedObjects, this.managedObjectSources, this.governances, this.administrators,
				this.escalations, this.starts);
	}

	@Override
	public boolean isInitialised() {
		return (this.state != null);
	}

	@Override
	public void initialise(String officeSourceClassName, OfficeSource officeSource, String officeLocation) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(officeSourceClassName, officeSource, officeLocation));
	}

	/*
	 * ================== ManagedObjectRegistry ==================
	 */

	@Override
	public ManagedObjectNode getManagedObjectNode(String managedObjectName) {
		return NodeUtil.getNode(managedObjectName, this.managedObjects,
				() -> this.context.createManagedObjectNode(managedObjectName, this));
	}

	@Override
	public ManagedObjectNode addManagedObjectNode(String managedObjectName, ManagedObjectScope managedObjectScope,
			ManagedObjectSourceNode managedObjectSourceNode) {
		return NodeUtil.getInitialisedNode(managedObjectName, this.managedObjects, this.context,
				() -> this.context.createManagedObjectNode(managedObjectName, this),
				(managedObject) -> managedObject.initialise(managedObjectScope, managedObjectSourceNode));
	}

	/*
	 * ================== OfficeTeamRegistry ========================
	 */

	@Override
	public OfficeTeamNode[] getOfficeTeams() {
		return this.teams.values().stream().toArray(OfficeTeamNode[]::new);
	}

	@Override
	public OfficeTeamNode createOfficeTeam(String officeTeamName) {

		// Ensure have a unique Office team name
		int suffix = 1;
		String uniqueOfficeTeamName = officeTeamName;
		while (this.teams.containsKey(uniqueOfficeTeamName)) {
			uniqueOfficeTeamName = officeTeamName + "_" + String.valueOf(++suffix);
		}

		// Create and return the Office team
		final String newOfficeTeamName = uniqueOfficeTeamName;
		return NodeUtil.getInitialisedNode(newOfficeTeamName, this.teams, this.context,
				() -> this.context.createOfficeTeamNode(newOfficeTeamName, this), (team) -> team.initialise());
	}

	/*
	 * ============= ManagedFunctionVisitor ============================
	 */

	@Override
	public void visit(ManagedFunctionType<?, ?> managedFunctionType, ManagedFunctionNode managedFunctionNode,
			CompileContext compileContext) {

		// Create the managed function augment context
		ManagedFunctionAugmentorContext context = new ManagedFunctionAugmentorContext() {

			@Override
			public String getManagedFunctionName() {
				return managedFunctionNode.getQualifiedFunctionName();
			}

			@Override
			public ManagedFunctionType<?, ?> getManagedFunctionType() {
				return managedFunctionType;
			}

			@Override
			public AugmentedFunctionObject getFunctionObject(String objectName) {
				return managedFunctionNode.getAugmentedFunctionObject(objectName);
			}

			@Override
			public void addPreAdministration(OfficeAdministration administration) {
				managedFunctionNode.addPreAdministration(administration);
			}

			@Override
			public void addPostAdministration(OfficeAdministration administration) {
				managedFunctionNode.addPostAdministration(administration);
			}

			@Override
			public void link(AugmentedFunctionObject object, OfficeManagedObject managedObject) {
				LinkUtil.linkObject(object, managedObject, OfficeNodeImpl.this.context.getCompilerIssues(),
						managedFunctionNode);
			}

			@Override
			public CompileError addIssue(String issueDescription) {
				return OfficeNodeImpl.this.addIssue(issueDescription);
			}

			@Override
			public CompileError addIssue(String issueDescription, Throwable cause) {
				return OfficeNodeImpl.this.addIssue(issueDescription, cause);
			}
		};

		// Augment the managed function
		for (ManagedFunctionAugmentor augmentor : this.managedFunctionAugmentors) {
			augmentor.augmentManagedFunction(context);
		}
	}

	/*
	 * ============= OverrideProperties ===============================
	 */

	@Override
	public PropertyList getOverridePropertyList() {
		return this.overrideProperties;
	}

	/*
	 * ================== OfficeNode ===================================
	 */

	@Override
	public String[] getAdditionalProfiles() {
		return this.additionalProfiles.toArray(new String[this.additionalProfiles.size()]);
	}

	@Override
	public OfficeFloorNode getOfficeFloorNode() {
		return this.officeFloor;
	}

	@Override
	public OfficeManagedObjectSource addManagedObjectSource(String managedObjectSourceName,
			SuppliedManagedObjectSourceNode suppliedManagedObject) {
		return NodeUtil.getInitialisedNode(managedObjectSourceName, this.managedObjectSources, this.context,
				() -> this.context.createManagedObjectSourceNode(managedObjectSourceName, suppliedManagedObject),
				(managedObjectSource) -> managedObjectSource.initialise(null, null));
	}

	/**
	 * Sources the {@link Office}.
	 * 
	 * @param compileContext {@link CompileContext}.
	 * @param isLoadingType  Indicates if loading type.
	 * @return <true> to indicate sourced, otherwise <false> with issues reported to
	 *         the {@link CompilerIssues}.
	 */
	private boolean sourceOffice(CompileContext compileContext, boolean isLoadingType) {

		// Ensure the office is initialised
		if (!this.isInitialised()) {
			this.context.getCompilerIssues().addIssue(this, "Office is not initialised");
			return false; // must be initialised
		}

		// Determine if must instantiate
		OfficeSource source = this.state.officeSource;
		if (source == null) {

			// Obtain the office source class
			Class<? extends OfficeSource> officeSourceClass = this.context
					.getOfficeSourceClass(this.state.officeSourceClassName, this);
			if (officeSourceClass == null) {
				return false; // must have office source class
			}

			// Obtain the office source
			source = CompileUtil.newInstance(officeSourceClass, OfficeSource.class, this,
					this.context.getCompilerIssues());
			if (source == null) {
				return false; // must have office source
			}
		}

		// Keep track of the office source
		this.usedOfficeSource = source;

		// Create the office source context
		OfficeSourceContextImpl context = this.createOfficeSourceContext(isLoadingType);

		// Obtain the extension services (ensuring all are available)
		List<OfficeExtensionService> extensionServices = new ArrayList<>();
		for (OfficeExtensionService extensionService : context
				.loadOptionalServices(OfficeExtensionServiceFactory.class)) {
			extensionServices.add(extensionService);
		}

		try {
			// Source the office
			source.sourceOffice(this, context);

			// Extend the office
			for (OfficeExtensionService extensionService : extensionServices) {
				extensionService.extendOffice(this, context);
			}

		} catch (AbstractSourceError ex) {
			ex.addIssue(new SourceIssuesIssueTarget(this));
			return false; // can not carry on

		} catch (LoadTypeError ex) {
			ex.addLoadTypeIssue(this, this.context.getCompilerIssues());
			return false; // must not fail in loading types

		} catch (CompileError ex) {
			return false; // issue already reported

		} catch (Throwable ex) {
			this.addIssue("Failed to source " + OfficeType.class.getSimpleName() + " definition from "
					+ OfficeSource.class.getSimpleName() + " " + source.getClass().getName(), ex);
			return false; // must be successful
		}

		// As here, successfully sourced
		return true;
	}

	/**
	 * Transforms the {@link OfficeSection} instances.
	 */
	private void transformOfficeSections() {
		this.sections.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeSectionName(), b.getOfficeSectionName()))
				.forEachOrdered((section) -> {

					// Transform the section
					for (OfficeSectionTransformer transformer : this.officeSectionTransformers) {
						transformer.transformOfficeSection(section);
					}
				});
	}

	@Override
	public boolean sourceOfficeWithTopLevelSections(ManagedObjectSourceVisitor managedObjectSourceVisitor,
			CompileContext compileContext) {

		// Source the office
		boolean isSourced = this.sourceOffice(compileContext, true);
		if (!isSourced) {
			return false;
		}

		// Transform the office sections
		this.transformOfficeSections();

		// Source the top level sections
		isSourced = CompileUtil.source(this.sections, (section) -> section.getOfficeSectionName(),
				(section) -> section.sourceSection(this, managedObjectSourceVisitor, compileContext, true));
		if (!isSourced) {
			return false; // must source all top level sections
		}

		// As here, successfully loaded the office
		return true;
	}

	@Override
	public boolean sourceOfficeTree(ManagedObjectSourceVisitor managedObjectSourceVisitor,
			AutoWirerVisitor autoWirerVisitor, CompileContext compileContext) {

		// Source the office
		boolean isSourced = this.sourceOffice(compileContext, false);
		if (!isSourced) {
			return false;
		}

		// Ensure all the suppliers are sourced
		isSourced = CompileUtil.source(this.suppliers, (supplier) -> supplier.getOfficeFloorSupplierName(),
				(supplier) -> supplier.sourceSupplier(compileContext));
		if (!isSourced) {
			return false;
		}

		// Transform the office sections
		this.transformOfficeSections();

		// Source all section trees
		isSourced = CompileUtil.source(this.sections, (section) -> section.getOfficeSectionName(),
				(section) -> section.sourceSectionTree(this, managedObjectSourceVisitor, compileContext, false));
		if (!isSourced) {
			return false; // must source all top level sections
		}

		// Source inheritance of sections
		isSourced = CompileUtil.source(this.sections, (section) -> section.getOfficeSectionName(),
				(section) -> section.sourceInheritance(compileContext));
		if (!isSourced) {
			return false; // must be able to inherit
		}

		// Ensure all non-supplied managed object sources are sourced
		isSourced = CompileUtil.source(this.managedObjectSources,
				(managedObjectSource) -> managedObjectSource.getSectionManagedObjectSourceName(),
				(managedObjectSource) -> {
					if (managedObjectSource.isSupplied()) {
						return true; // successfully not sourced
					}

					// Source the managed object source
					return managedObjectSource.sourceManagedObjectSource(managedObjectSourceVisitor, compileContext);
				});
		if (!isSourced) {
			return false;
		}

		// Ensure all non-supplied managed objects are sourced
		isSourced = CompileUtil.source(this.managedObjects,
				(managedObject) -> managedObject.getSectionManagedObjectName(), (managedObject) -> {
					if (managedObject.getManagedObjectSourceNode().isSupplied()) {
						return true; // successfully not sourced
					}

					// Source the managed object
					return managedObject.sourceManagedObject(compileContext);
				});
		if (!isSourced) {
			return false;
		}

		// Ensure all managed object pools are sourced
		isSourced = CompileUtil.source(this.managedObjectPools, (pool) -> pool.getOfficeManagedObjectPoolName(),
				(pool) -> pool.sourceManagedObjectPool(compileContext));
		if (!isSourced) {
			return false;
		}

		// Ensure all administration sourced
		isSourced = CompileUtil.source(this.administrators,
				(administration) -> administration.getOfficeAdministrationName(),
				(administration) -> administration.sourceAdministration(compileContext));
		if (!isSourced) {
			return false;
		}

		// Ensure all governance sourced
		isSourced = CompileUtil.source(this.governances, (governance) -> governance.getOfficeGovernanceName(),
				(governance) -> governance.sourceGovernance(compileContext));
		if (!isSourced) {
			return false;
		}

		// Ensure all supplier complete
		isSourced = CompileUtil.source(this.suppliers, (supplier) -> supplier.getOfficeFloorSupplierName(),
				(supplier) -> supplier.sourceComplete(compileContext));
		if (!isSourced) {
			return false;
		}

		// Ensure the office tree is initialised
		if (!NodeUtil.isNodeTreeInitialised(this, this.context.getCompilerIssues())) {
			return false; // must have fully initialised tree
		}

		// Undertake auto-wire of objects
		if (this.isAutoWireObjects || (autoWirerVisitor != null)) {

			// Create the OfficeFloor auto wirer
			final AutoWirer<LinkObjectNode> officeFloorAutoWirer = this.context.createAutoWirer(LinkObjectNode.class,
					AutoWireDirection.SOURCE_REQUIRES_TARGET);
			final AutoWirer<LinkObjectNode> officeFloorContextAutoWirer = this.officeFloor
					.loadAutoWireObjectTargets(officeFloorAutoWirer, compileContext);

			// Create the Office supplier auto wirer
			final AutoWirer<LinkObjectNode> officeSupplierAutoWirer = officeFloorContextAutoWirer
					.createScopeAutoWirer();
			this.suppliers.values().forEach((supplier) -> supplier.loadAutoWireObjects(officeSupplierAutoWirer,
					managedObjectSourceVisitor, compileContext));

			// Create the Office objects auto wirer
			final AutoWirer<LinkObjectNode> officeObjectsAutoWirer = officeSupplierAutoWirer.createScopeAutoWirer();
			this.objects.values().forEach((object) -> officeObjectsAutoWirer.addAutoWireTarget(object,
					new AutoWire(object.getTypeQualifier(), object.getOfficeObjectType())));

			// Create the Office managed object auto wirer
			final AutoWirer<LinkObjectNode> autoWirer = officeObjectsAutoWirer.createScopeAutoWirer();
			this.managedObjects.values().forEach((mo) -> {

				// Create the auto-wires
				AutoWire[] targetAutoWires = Arrays.stream(mo.getTypeQualifications(compileContext))
						.map((type) -> new AutoWire(type.getQualifier(), type.getType())).toArray(AutoWire[]::new);

				// Add the target
				autoWirer.addAutoWireTarget(mo, targetAutoWires);
			});

			// Allow visiting the auto wirer
			if (autoWirerVisitor != null) {
				autoWirerVisitor.visit(this, autoWirer);
			}

			// Iterate over sections (auto-wiring unlinked dependencies)
			this.sections.values().stream()
					.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeSectionName(), b.getOfficeSectionName()))
					.forEachOrdered((section) -> section.autoWireObjects(autoWirer, compileContext));

			// Iterate over managed objects (auto-wiring unlinked dependencies)
			this.managedObjects.values().stream().sorted(
					(a, b) -> CompileUtil.sortCompare(a.getOfficeManagedObjectName(), b.getOfficeManagedObjectName()))
					.forEachOrdered(
							(managedObject) -> managedObject.autoWireDependencies(autoWirer, this, compileContext));

			// Iterate over mo sources (auto-wiring unlinked input dependencies)
			this.managedObjectSources.values().stream()
					.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeFloorManagedObjectSourceName(),
							b.getOfficeFloorManagedObjectSourceName()))
					.forEachOrdered((managedObjectSource) -> {
						// This office will manage the managed object
						OfficeNode officeNode = this;

						// Load input dependencies for managed object source
						managedObjectSource.autoWireInputDependencies(autoWirer, officeNode, compileContext);

						// Load the function dependencies for managed object source
						managedObjectSource.autoWireFunctionDependencies(autoWirer, officeNode, compileContext);
					});

			// Iterate over suppliers (auto-wiring unlinked thread locals)
			this.suppliers.values().stream()
					.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeSupplierName(), b.getOfficeSupplierName()))
					.forEachOrdered((supplier) -> supplier.autoWireObjects(autoWirer, this, compileContext));
		}

		// Undertake auto-wire of administration
		boolean isAutoWireAdministration = this.administrators.values().stream()
				.anyMatch((administrator) -> administrator.isAutoWireAdministration());
		boolean isAutoWireGovernance = this.governances.values().stream()
				.anyMatch((governance) -> governance.isAutoWireGovernance());
		if (isAutoWireAdministration || isAutoWireGovernance) {

			// Create the OfficeFloor extension auto wirer
			final AutoWirer<ManagedObjectExtensionNode> officeFloorAutoWirer = this.context
					.createAutoWirer(ManagedObjectExtensionNode.class, AutoWireDirection.SOURCE_REQUIRES_TARGET);
			final AutoWirer<ManagedObjectExtensionNode> officeFloorContextAutoWirer = this.officeFloor
					.loadAutoWireExtensionTargets(officeFloorAutoWirer, compileContext);

			// Create the Office supplier auto wirer
			final AutoWirer<ManagedObjectExtensionNode> officeSupplierAutoWirer = officeFloorContextAutoWirer
					.createScopeAutoWirer();
			this.suppliers.values().forEach((supplier) -> supplier.loadAutoWireExtensions(officeSupplierAutoWirer,
					managedObjectSourceVisitor, compileContext));

			// Create the Office managed object auto wirer
			final AutoWirer<ManagedObjectExtensionNode> officeAutoWirer = officeSupplierAutoWirer
					.createScopeAutoWirer();
			this.managedObjects.values().forEach((mo) -> {

				// Load the type
				ManagedObjectType<?> moType = mo.getManagedObjectSourceNode().loadManagedObjectType(compileContext);
				if (moType == null) {
					return;
				}

				// Load the extensions
				for (Class<?> extensionType : moType.getExtensionTypes()) {
					officeAutoWirer.addAutoWireTarget(mo, new AutoWire(extensionType));
				}
			});

			// Create the Section auto-wirer
			final AutoWirer<ManagedObjectExtensionNode> autoWirer = officeAutoWirer.createScopeAutoWirer();
			this.sections.values()
					.forEach((section) -> section.loadAutoWireExtensionTargets(autoWirer, compileContext));

			// Auto-wire administration
			this.administrators.values().stream().sorted(
					(a, b) -> CompileUtil.sortCompare(a.getOfficeAdministrationName(), b.getOfficeAdministrationName()))
					.filter((administrator) -> administrator.isAutoWireAdministration())
					.forEachOrdered((administration) -> administration.autoWireExtensions(autoWirer, compileContext));

			// Auto-wire governance
			this.governances.values().stream()
					.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeGovernanceName(), b.getOfficeGovernanceName()))
					.filter((governance) -> governance.isAutoWireGovernance())
					.forEachOrdered((governance) -> governance.autoWireExtensions(autoWirer, compileContext));
		}

		// Determine if auto-wired start up ordering
		boolean isAutoWireStartupOrdering = this.managedObjectSources.values().stream()
				.anyMatch((mos) -> mos.isAutoWireStartupOrdering());
		if (isAutoWireStartupOrdering) {

			// Create the auto wirer
			final AutoWirer<ManagedObjectSourceNode> officeAutoWirer = this.context
					.createAutoWirer(ManagedObjectSourceNode.class, AutoWireDirection.SOURCE_REQUIRES_TARGET);
			final AutoWirer<ManagedObjectSourceNode> autoWirer = this.officeFloor
					.loadAutoWireManagedObjectSourceTargets(officeAutoWirer, compileContext);
			this.managedObjectSources.values().forEach((mos) -> {

				// Load the type
				ManagedObjectType<?> moType = mos.loadManagedObjectType(compileContext);
				if (moType != null) {

					// Register the auto wire
					Class<?> objectType = moType.getObjectType();
					autoWirer.addAutoWireTarget(mos, new AutoWire(objectType));
				}
			});

			// Auto-wire the start up ordering
			this.managedObjectSources.values().stream()
					.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeFloorManagedObjectSourceName(),
							b.getOfficeFloorManagedObjectSourceName()))
					.forEachOrdered((managedObjectSource) -> {

						// Load the start up ordering
						managedObjectSource.autoWireStartupOrdering(autoWirer, this, compileContext);
					});
		}

		// Undertake auto-wire of teams
		if (this.isAutoWireTeams) {

			// Create the OfficeFloor team auto wirer
			final AutoWirer<LinkTeamNode> officeFloorAutoWirer = this.context.createAutoWirer(LinkTeamNode.class,
					AutoWireDirection.TARGET_CATEGORISES_SOURCE);
			this.officeFloor.loadAutoWireTeamTargets(officeFloorAutoWirer, this, compileContext);

			// Create the Office team auto wirer
			final AutoWirer<LinkTeamNode> autoWirer = officeFloorAutoWirer.createScopeAutoWirer();
			this.teams.values().forEach((team) -> {

				// Create the auto-wires
				AutoWire[] targetAutoWires = Arrays.stream(team.getTypeQualifications())
						.map((type) -> new AutoWire(type.getQualifier(), type.getType())).toArray(AutoWire[]::new);
				if (targetAutoWires.length > 0) {
					autoWirer.addAutoWireTarget(team, targetAutoWires);
				}
			});

			// Iterate over sections (auto-wiring functions to teams)
			this.sections.values().stream()
					.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeSectionName(), b.getOfficeSectionName()))
					.forEachOrdered((section) -> section.autoWireTeams(autoWirer, compileContext));

			// Auto-wire governances to teams
			this.governances.values().stream()
					.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeGovernanceName(), b.getOfficeGovernanceName()))
					.forEachOrdered((governance) -> governance.autoWireTeam(autoWirer, compileContext));

			// Auto-wire administrations to teams
			this.administrators.values().stream().sorted(
					(a, b) -> CompileUtil.sortCompare(a.getOfficeAdministrationName(), b.getOfficeAdministrationName()))
					.forEachOrdered((administration) -> administration.autoWireTeam(autoWirer, compileContext));

			// Auto-wire managed object source teams
			this.managedObjectSources.values().stream()
					.sorted((a, b) -> CompileUtil.sortCompare(a.getQualifiedName(), b.getQualifiedName()))
					.forEachOrdered((mos) -> mos.autoWireTeams(autoWirer, compileContext));
		}

		// As here, successfully loaded the office
		return true;
	}

	@Override
	public AvailableType[] getAvailableTypes(CompileContext compileContext) {

		// Obtain the OfficeFloor available types
		AvailableType[] officeFloorAvailableTypes = this.officeFloor.getAvailableTypes(compileContext);

		// Obtain the Office available types
		OfficeSourceContext sourceContext = this.createOfficeSourceContext(false);
		AvailableType[] officeAvailableTypes = AvailableTypeImpl.extractAvailableTypes(this.managedObjects,
				compileContext, sourceContext);

		// Return the concatenated available types
		AvailableType[] availableTypes = Arrays.copyOf(officeFloorAvailableTypes,
				officeFloorAvailableTypes.length + officeAvailableTypes.length);
		System.arraycopy(officeAvailableTypes, 0, availableTypes, officeFloorAvailableTypes.length,
				officeAvailableTypes.length);
		return availableTypes;
	}

	@Override
	public OfficeType loadOfficeType(CompileContext compileContext) {

		// Copy the inputs into an array (in deterministic order)
		OfficeInputNode[] inputs = this.inputs.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeInputName(), b.getOfficeInputName()))
				.toArray(OfficeInputNode[]::new);

		// Copy the outputs into an array (in deterministic order)
		OfficeOutputNode[] outputs = this.outputs.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeOutputName(), b.getOfficeOutputName()))
				.toArray(OfficeOutputNode[]::new);

		// Create the listing of input types
		OfficeInputType[] inputTypes = CompileUtil.loadTypes(Arrays.asList(inputs).stream(),
				(input) -> input.getOfficeInputName(), (input) -> input.loadOfficeInputType(compileContext),
				OfficeInputType[]::new);
		if (inputTypes == null) {
			return null;
		}

		// Create the listing of output types
		OfficeOutputType[] outputTypes = CompileUtil.loadTypes(Arrays.asList(outputs).stream(),
				(output) -> output.getOfficeOutputName(), (output) -> output.loadOfficeOutputType(compileContext),
				OfficeOutputType[]::new);
		if (outputTypes == null) {
			return null;
		}

		// Create the listing of architect added object types
		OfficeManagedObjectType[] moTypes = CompileUtil.loadTypes(this.objects,
				(object) -> object.getOfficeObjectName(),
				(object) -> object.loadOfficeManagedObjectType(compileContext), OfficeManagedObjectType[]::new);
		if (moTypes == null) {
			return null;
		}

		// Copy architect added team types into an array
		OfficeTeamType[] teamTypes = CompileUtil.loadTypes(this.teams, (team) -> team.getOfficeTeamName(),
				(team) -> team.loadOfficeTeamType(compileContext), OfficeTeamType[]::new);
		if (teamTypes == null) {
			return null;
		}

		// Create the listing of office section inputs
		OfficeAvailableSectionInputType[][] sectionInputTypesArrays = CompileUtil.loadTypes(this.sections,
				(section) -> section.getOfficeSectionName(),
				(section) -> section.loadOfficeAvailableSectionInputTypes(compileContext),
				OfficeAvailableSectionInputType[][]::new);
		if (sectionInputTypesArrays == null) {
			return null;
		}
		OfficeAvailableSectionInputType[] sectionInputTypes = Arrays.asList(sectionInputTypesArrays).stream()
				.flatMap((types) -> Arrays.asList(types).stream()).toArray(OfficeAvailableSectionInputType[]::new);

		// Create and return the type
		return new OfficeTypeImpl(inputTypes, outputTypes, teamTypes, moTypes, sectionInputTypes);
	}

	@Override
	public void autoWireObjects(AutoWirer<LinkObjectNode> autoWirer, CompileContext compileContext) {

		// Auto-wire the objects
		this.objects.values().forEach((object) -> {

			// Ignore if already configured
			if (object.getLinkedObjectNode() != null) {
				return;
			}

			// Obtain the qualifier and type for object
			String typeQualifier = object.getTypeQualifier();
			String objectType = object.getOfficeObjectType();

			// Auto-wire the object
			AutoWireLink<OfficeObjectNode, LinkObjectNode>[] links = autoWirer.getAutoWireLinks(object,
					new AutoWire(typeQualifier, objectType));
			if (links.length == 1) {
				LinkUtil.linkAutoWireObjectNode(object, links[0].getTargetNode(this), this, autoWirer, compileContext,
						this.context.getCompilerIssues(), (link) -> object.linkObjectNode(link));
			}
		});
	}

	@Override
	public void autoWireTeams(AutoWirer<LinkTeamNode> autoWirer, CompileContext compileContext) {

		// Auto-wire team
		this.teams.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeTeamName(), b.getOfficeTeamName()))
				.forEachOrdered((team) -> {

					// Ignore if already configured
					if (team.getLinkedTeamNode() != null) {
						return;
					}

					// Create the auto-wires
					AutoWire[] sourceAutoWires = Arrays.stream(team.getTypeQualifications())
							.map((type) -> new AutoWire(type.getQualifier(), type.getType())).toArray(AutoWire[]::new);

					// Auto-wire the team
					AutoWireLink<OfficeTeamNode, LinkTeamNode>[] links = autoWirer.getAutoWireLinks(team,
							sourceAutoWires);
					if (links.length == 1) {
						LinkUtil.linkTeam(team, links[0].getTargetNode(this), this.context.getCompilerIssues(), this);
					}
				});
	}

	@Override
	public boolean runExecutionExplorers(CompileContext compileContext) {

		// Run explores for objects (in deterministic order)
		boolean isObjectsExplored = this.managedObjects.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getBoundManagedObjectName(), b.getBoundManagedObjectName()))
				.allMatch((managedObject) -> managedObject.runExecutionExplorers(compileContext));

		// Create the map of all managed functions
		Map<String, ManagedFunctionNode> managedFunctions = new HashMap<>();
		this.sections.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeSectionName(), b.getOfficeSectionName()))
				.forEachOrdered((section) -> section.loadManagedFunctionNodes(managedFunctions));

		// Run explorers for the sections (in deterministic order)
		boolean isSectionsExplored = this.sections.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeSectionName(), b.getOfficeSectionName()))
				.allMatch((section) -> section.runExecutionExplorers(managedFunctions, compileContext));

		// Run explorers for the escalations (in deterministic order)
		boolean isEscalationsExplored = this.escalations.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeEscalationType(), b.getOfficeEscalationType()))
				.allMatch((escalation) -> {

					// Run the execution explorer
					ExecutionManagedFunction initialFunction = null;
					for (EscalationExplorer explorer : this.escalationExplorers) {

						// Lazy obtain function servicing this section input
						if (initialFunction == null) {
							ManagedFunctionNode functionNode = LinkUtil.retrieveTarget(escalation,
									ManagedFunctionNode.class, this.context.getCompilerIssues());
							if (functionNode != null) {
								initialFunction = functionNode.createExecutionManagedFunction(compileContext);
							}
						}

						// Explore from the escalation
						try {
							final ExecutionManagedFunction finalInitialFunction = initialFunction;
							explorer.explore(new EscalationExplorerContext() {

								@Override
								public String getOfficeEscalationType() {
									return escalation.getOfficeEscalationType();
								}

								@Override
								public ExecutionManagedFunction getManagedFunction(String functionName) {

									// Obtain the managed function node
									ManagedFunctionNode function = managedFunctions.get(functionName);
									if (function == null) {
										return null;
									}

									// Create and return the execution managed function
									return function.createExecutionManagedFunction(compileContext);
								}

								@Override
								public ExecutionManagedFunction getInitialManagedFunction() {
									return finalInitialFunction;
								}
							});
						} catch (Throwable ex) {
							this.context.getCompilerIssues().addIssue(this,
									"Failure in exploring escalation " + escalation.getOfficeEscalationType(), ex);
						}
					}

					// As here, successfully explored
					return true;
				});

		// Run explorers for completion
		boolean isCompletionExplored = this.completionExplorers.stream().allMatch((explorer) -> {

			// Explore the completion
			try {
				explorer.complete();
			} catch (Throwable ex) {
				this.context.getCompilerIssues().addIssue(this, "Failed in exploring completion", ex);
			}

			// As here, successfully explored
			return true;
		});

		// Indicate successfully run explorers
		return isObjectsExplored && isSectionsExplored && isEscalationsExplored && isCompletionExplored;
	}

	@Override
	public OfficeBindings buildOffice(OfficeFloorBuilder builder, CompileContext compileContext, Profiler profiler) {

		// Register as possible MBean
		compileContext.registerPossibleMBean(OfficeSource.class, this.officeName, this.usedOfficeSource);

		// Build this office
		OfficeBuilder officeBuilder = builder.addOffice(this.officeName);

		// Load the profiler (if provided)
		if (profiler != null) {
			officeBuilder.setProfiler(profiler);
		}

		// Create the bindings for the office
		OfficeBindings officeBindings = new OfficeBindingsImpl(this, officeBuilder, builder, compileContext);

		// Register the teams for the office
		this.teams.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeTeamName(), b.getOfficeTeamName()))
				.forEachOrdered((team) -> {
					// Obtain the office team name
					String officeTeamName = team.getOfficeTeamName();

					// Obtain the OfficeFloor team name
					TeamNode officeFloorTeam = LinkUtil.findTarget(team, TeamNode.class,
							this.context.getCompilerIssues());
					if (officeFloorTeam == null) {
						return; // OfficeFloor team not linked
					}
					String officeFloorTeamName = officeFloorTeam.getOfficeFloorTeamName();

					// Register the team to the office
					officeBuilder.registerTeam(officeTeamName, officeFloorTeamName);
				});

		// Build the governance for the office (in deterministic order)
		this.governances.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeGovernanceName(), b.getOfficeGovernanceName()))
				.forEachOrdered((governance) -> governance.buildGovernance(officeBuilder, compileContext));

		// Load the office objects (in deterministic order)
		this.objects.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeObjectName(), b.getOfficeObjectName()))
				.forEachOrdered((objectNode) -> {

					// Obtain the managed object node
					BoundManagedObjectNode managedObjectNode = LinkUtil.retrieveTarget(objectNode,
							BoundManagedObjectNode.class, this.context.getCompilerIssues());
					if (managedObjectNode == null) {
						return;
					}

					// Load governances for linked Managed Object
					GovernanceNode[] governances = objectNode.getGovernances();
					for (GovernanceNode governance : governances) {
						managedObjectNode.addGovernance(governance, this);
					}

					// Load pre-load administration for linked Managed Object
					AdministrationNode[] administrations = objectNode.getPreLoadAdministrations();
					for (AdministrationNode administration : administrations) {
						managedObjectNode.addPreLoadAdministration(administration, this);
					}

					// Build the managed object into the office
					officeBindings.buildManagedObjectIntoOffice(managedObjectNode);
				});

		// Build the managed object sources (in deterministic order)
		this.managedObjectSources.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeManagedObjectSourceName(),
						b.getOfficeManagedObjectSourceName()))
				.forEachOrdered((managedObjectSource) -> officeBindings
						.buildManagedObjectSourceIntoOffice(managedObjectSource));

		// Load the managed objects for office (in deterministic order)
		this.managedObjects.values().stream().sorted(
				(a, b) -> CompileUtil.sortCompare(a.getOfficeManagedObjectName(), b.getOfficeManagedObjectName()))
				.forEachOrdered((mos) -> officeBindings.buildManagedObjectIntoOffice(mos));

		// Build the suppliers
		this.suppliers.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeSupplierName(), b.getOfficeSupplierName()))
				.forEachOrdered((supplier) -> supplier.buildSupplier(officeBuilder, compileContext));

		// Build the sections of the office (in deterministic order)
		this.sections.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeSectionName(), b.getOfficeSectionName()))
				.forEachOrdered((section) -> section.buildSection(officeBuilder, officeBindings, compileContext));

		// Build the list of escalations of the office
		List<EscalationStruct> escalationStructs = new LinkedList<OfficeNodeImpl.EscalationStruct>();
		for (EscalationNode node : this.escalations.values()) {
			// Obtain the escalation type
			String escalationTypeName = node.getOfficeEscalationType();
			Class<? extends Throwable> type = CompileUtil.obtainClass(escalationTypeName, Throwable.class, null,
					this.context.getRootSourceContext(), this, this.context.getCompilerIssues());
			if (type == null) {
				// Failed to obtain escalation type
				this.context.getCompilerIssues().addIssue(this, "Unknown escalation type " + escalationTypeName);
				continue; // ignore this escalation
			}

			// Add the escalation struct
			escalationStructs.add(new EscalationStruct(type, node));
		}

		// Order by more specific escalation first. Allows finer handling first.
		Collections.sort(escalationStructs, new Comparator<EscalationStruct>() {
			@Override
			public int compare(EscalationStruct a, EscalationStruct b) {

				// Compare based on type
				if (a.type != b.type) {
					if (a.type.isAssignableFrom(b.type)) {
						return 1; // a is super type
					} else if (b.type.isAssignableFrom(a.type)) {
						return -1; // b is super type
					}
				}

				// Either same type or no inheritance relationship.
				// Therefore sort alphabetically to have ordering.
				return String.CASE_INSENSITIVE_ORDER.compare(a.type.getName(), b.type.getName());
			}
		});

		// Build the escalation handling for the office (in deterministic order)
		for (EscalationStruct escalation : escalationStructs) {

			// Obtain the target function
			ManagedFunctionNode function = LinkUtil.findTarget(escalation.node, ManagedFunctionNode.class,
					this.context.getCompilerIssues());
			if (function == null) {
				continue; // function not linked
			}

			// Build the escalation handling
			String functionName = function.getQualifiedFunctionName();
			officeBuilder.addEscalation(escalation.type, functionName);
		}

		// Build the start-up triggers for the office (in deterministic order)
		this.starts.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeStartName(), b.getOfficeStartName()))
				.forEachOrdered((start) -> {
					// Obtain the target function
					ManagedFunctionNode function = LinkUtil.findTarget(start, ManagedFunctionNode.class,
							this.context.getCompilerIssues());
					if (function == null) {
						return; // function not linked
					}

					// Build the start-up trigger
					String functionName = function.getQualifiedFunctionName();
					officeBuilder.addStartupFunction(functionName, null);
				});

		// Return the office bindings
		return officeBindings;
	}

	@Override
	public void loadExternalServicing(Office office) throws UnknownFunctionException {
		SectionNode[] sectionNodes = this.sections.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeSectionName(), b.getOfficeSectionName()))
				.toArray(SectionNode[]::new);
		for (SectionNode sectionNode : sectionNodes) {
			sectionNode.loadExternalServicing(office);
		}
	}

	@Override
	public InternalSupplier[] getInternalSuppliers() {

		// Obtain the OfficeFloor internal suppliers
		InternalSupplier[] officeFloorInternalSuppliers = this.officeFloor.getInternalSuppliers();

		// Obtain the Office internal suppliers
		InternalSupplier[] officeInternalSuppliers = this.suppliers.values().stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeSupplierName(), b.getOfficeSupplierName()))
				.flatMap(supplier -> Arrays.stream(supplier.getInternalSuppliers())).toArray(InternalSupplier[]::new);

		// Return the internal suppliers
		InternalSupplier[] internalSuppliers = Arrays.copyOf(officeFloorInternalSuppliers,
				officeFloorInternalSuppliers.length + officeInternalSuppliers.length);
		System.arraycopy(officeInternalSuppliers, 0, internalSuppliers, officeFloorInternalSuppliers.length,
				officeInternalSuppliers.length);
		return internalSuppliers;
	}

	/*
	 * ===================== OfficeArchitect ================================
	 */

	@Override
	public void enableAutoWireObjects() {
		this.isAutoWireObjects = true;
	}

	@Override
	public void enableAutoWireTeams() {
		this.isAutoWireTeams = true;
	}

	@Override
	public OfficeObject addOfficeObject(String officeManagedObjectName, String objectType) {
		return NodeUtil.getInitialisedNode(officeManagedObjectName, this.objects, this.context,
				() -> this.context.createOfficeObjectNode(officeManagedObjectName, this),
				(managedObject) -> managedObject.initialise(objectType));
	}

	@Override
	public OfficeInput addOfficeInput(String inputName, String parameterType) {
		return NodeUtil.getInitialisedNode(inputName, this.inputs, this.context,
				() -> this.context.createOfficeInputNode(inputName, this), (input) -> input.initialise(parameterType));
	}

	@Override
	public OfficeOutput addOfficeOutput(String outputName, String argumentType) {
		return NodeUtil.getInitialisedNode(outputName, this.outputs, this.context,
				() -> this.context.createOfficeOutputNode(outputName, this),
				(output) -> output.initialise(argumentType));
	}

	@Override
	public OfficeTeam addOfficeTeam(String officeTeamName) {
		return NodeUtil.getInitialisedNode(officeTeamName, this.teams, this.context,
				() -> this.context.createOfficeTeamNode(officeTeamName, this), (team) -> team.initialise());
	}

	@Override
	public OfficeSection addOfficeSection(String sectionName, String sectionSourceClassName, String sectionLocation) {
		return NodeUtil.getInitialisedNode(sectionName, this.sections, context,
				() -> this.context.createSectionNode(sectionName, this),
				(section) -> section.initialise(sectionSourceClassName, null, sectionLocation));
	}

	@Override
	public OfficeSection addOfficeSection(String sectionName, SectionSource sectionSource, String sectionLocation) {
		return NodeUtil.getInitialisedNode(sectionName, this.sections, context,
				() -> this.context.createSectionNode(sectionName, this),
				(section) -> section.initialise(sectionSource.getClass().getName(), sectionSource, sectionLocation));
	}

	@Override
	public OfficeSection getOfficeSection(String sectionName) {
		return NodeUtil.getNode(sectionName, this.sections, () -> this.context.createSectionNode(sectionName, this));
	}

	@Override
	public void addOfficeSectionTransformer(OfficeSectionTransformer transformer) {
		this.officeSectionTransformers.add(transformer);
	}

	@Override
	public void addManagedFunctionAugmentor(ManagedFunctionAugmentor managedFunctionAugmentor) {
		this.managedFunctionAugmentors.add(managedFunctionAugmentor);
	}

	@Override
	public OfficeManagedObjectSource addOfficeManagedObjectSource(String managedObjectSourceName,
			String managedObjectSourceClassName) {
		return this.addManagedObjectSource(managedObjectSourceName, managedObjectSourceClassName);
	}

	@Override
	public OfficeManagedObjectSource addOfficeManagedObjectSource(String managedObjectSourceName,
			ManagedObjectSource<?, ?> managedObjectSource) {
		return this.addManagedObjectSource(managedObjectSourceName, managedObjectSource);
	}

	@Override
	public OfficeManagedObjectPool addManagedObjectPool(String managedObjectPoolName,
			String managedObjectPoolSourceClassName) {
		return NodeUtil.getInitialisedNode(managedObjectPoolName, this.managedObjectPools, this.context,
				() -> this.context.createManagedObjectPoolNode(managedObjectPoolName, this),
				(pool) -> pool.initialise(managedObjectPoolSourceClassName, null));
	}

	@Override
	public OfficeManagedObjectPool addManagedObjectPool(String managedObjectPoolName,
			ManagedObjectPoolSource managedObjectPoolSource) {
		return NodeUtil.getInitialisedNode(managedObjectPoolName, this.managedObjectPools, this.context,
				() -> this.context.createManagedObjectPoolNode(managedObjectPoolName, this),
				(pool) -> pool.initialise(managedObjectPoolSource.getClass().getName(), managedObjectPoolSource));
	}

	@Override
	public OfficeSupplier addSupplier(String supplierName, String supplierSourceClassName) {
		return NodeUtil.getInitialisedNode(supplierName, this.suppliers, this.context,
				() -> this.context.createSupplierNode(supplierName, this),
				(supplier) -> supplier.initialise(supplierSourceClassName, null));
	}

	@Override
	public OfficeSupplier addSupplier(String supplierName, SupplierSource supplierSource) {
		return NodeUtil.getInitialisedNode(supplierName, this.suppliers, this.context,
				() -> this.context.createSupplierNode(supplierName, this),
				(supplier) -> supplier.initialise(supplierSource.getClass().getName(), supplierSource));
	}

	@Override
	public OfficeGovernance addOfficeGovernance(String governanceName, String governanceSourceClassName) {
		return NodeUtil.getInitialisedNode(governanceName, this.governances, this.context,
				() -> this.context.createGovernanceNode(governanceName, this),
				(governance) -> governance.initialise(governanceSourceClassName, null));
	}

	@Override
	public OfficeGovernance addOfficeGovernance(String governanceName, GovernanceSource<?, ?> governanceSource) {
		return NodeUtil.getInitialisedNode(governanceName, this.governances, this.context,
				() -> this.context.createGovernanceNode(governanceName, this),
				(governance) -> governance.initialise(governanceSource.getClass().getName(), governanceSource));
	}

	@Override
	public OfficeAdministration addOfficeAdministration(String administrationName,
			String administrationSourceClassName) {
		return NodeUtil.getInitialisedNode(administrationName, this.administrators, this.context,
				() -> this.context.createAdministrationNode(administrationName, this),
				(administrator) -> administrator.initialise(administrationSourceClassName, null));
	}

	@Override
	public OfficeAdministration addOfficeAdministration(String administrationName,
			AdministrationSource<?, ?, ?> administrationSource) {
		return NodeUtil.getInitialisedNode(administrationName, this.administrators, this.context,
				() -> this.context.createAdministrationNode(administrationName, this), (administrator) -> administrator
						.initialise(administrationSource.getClass().getName(), administrationSource));
	}

	@Override
	public OfficeEscalation addOfficeEscalation(String escalationTypeName) {
		return NodeUtil.getInitialisedNode(escalationTypeName, this.escalations, this.context,
				() -> this.context.createEscalationNode(escalationTypeName, this),
				(escalation) -> escalation.initialise());
	}

	@Override
	public void addOfficeEscalationExplorer(EscalationExplorer escalationExplorer) {
		this.escalationExplorers.add(escalationExplorer);
	}

	@Override
	public void addOfficeCompletionExplorer(CompletionExplorer completionExplorer) {
		this.completionExplorers.add(completionExplorer);
	}

	@Override
	public OfficeStart addOfficeStart(String startName) {
		return NodeUtil.getInitialisedNode(startName, this.starts, this.context,
				() -> this.context.createOfficeStartNode(startName, this), (start) -> start.initialise());
	}

	@Override
	public void link(OfficeManagedObjectSource managedObjectSource, OfficeManagedObjectPool managedObjectPool) {
		LinkUtil.linkPool(managedObjectSource, managedObjectPool, this.context.getCompilerIssues(), this);
	}

	@Override
	public void link(OfficeFlowSourceNode flowSourceNode, OfficeFlowSinkNode flowSinkNode) {
		LinkUtil.linkFlow(flowSourceNode, flowSinkNode, this.context.getCompilerIssues(), this);
	}

	@Override
	public void link(OfficeDependencyRequireNode dependencyRequiredNode,
			OfficeDependencyObjectNode dependencyObjectNode) {
		LinkUtil.linkObject(dependencyRequiredNode, dependencyObjectNode, this.context.getCompilerIssues(), this);
	}

	@Override
	public void link(OfficeResponsibility responsibility, OfficeTeam officeTeam) {
		LinkUtil.linkTeam(responsibility, officeTeam, this.context.getCompilerIssues(), this);
	}

	@Override
	public void startBefore(OfficeManagedObjectSource startEarlier, OfficeManagedObjectSource startLater) {
		LinkUtil.linkStartBefore(startEarlier, startLater, this.context.getCompilerIssues(), this);
	}

	@Override
	public void startBefore(OfficeManagedObjectSource managedObjectSource, String managedObjectTypeName) {
		LinkUtil.linkAutoWireStartBefore(managedObjectSource, managedObjectTypeName, this.context.getCompilerIssues(),
				this);
	}

	@Override
	public void startAfter(OfficeManagedObjectSource startLater, OfficeManagedObjectSource startEarlier) {
		LinkUtil.linkStartAfter(startLater, startEarlier, this.context.getCompilerIssues(), this);
	}

	@Override
	public void startAfter(OfficeManagedObjectSource managedObjectSource, String managedObjectTypeName) {
		LinkUtil.linkAutoWireStartAfter(managedObjectSource, managedObjectTypeName, this.context.getCompilerIssues(),
				this);
	}

	@Override
	public CompileError addIssue(String issueDescription) {
		return this.context.getCompilerIssues().addIssue(this, issueDescription);
	}

	@Override
	public CompileError addIssue(String issueDescription, Throwable cause) {
		return this.context.getCompilerIssues().addIssue(this, issueDescription, cause);
	}

	/*
	 * =================== DeployedOffice =====================================
	 */

	@Override
	public String getDeployedOfficeName() {
		return this.officeName;
	}

	@Override
	public void addAdditionalProfile(String profile) {
		this.additionalProfiles.add(profile);
	}

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	@Override
	public void addOverrideProperty(String name, String value) {
		String officeQualifiedName = Node.qualify(this.officeName, name);
		this.overrideProperties.addProperty(officeQualifiedName).setValue(value);
	}

	@Override
	public DeployedOfficeInput getDeployedOfficeInput(String sectionName, String inputName) {
		SectionNode section = NodeUtil.getNode(sectionName, this.sections,
				() -> this.context.createSectionNode(sectionName, this));
		return section.getDeployedOfficeInput(inputName);
	}

	@Override
	public OfficeObject getDeployedOfficeObject(String officeManagedObjectName) {
		return NodeUtil.getNode(officeManagedObjectName, this.objects,
				() -> this.context.createOfficeObjectNode(officeManagedObjectName, this));
	}

	@Override
	public OfficeTeam getDeployedOfficeTeam(String officeTeamName) {
		return NodeUtil.getNode(officeTeamName, this.teams,
				() -> this.context.createOfficeTeamNode(officeTeamName, this));
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
		return LinkUtil.linkOfficeNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedOfficeNode = link);
	}

	@Override
	public LinkOfficeNode getLinkedOfficeNode() {
		return this.linkedOfficeNode;
	}

	/**
	 * Structure containing details of the {@link EscalationNode}.
	 * 
	 * 
	 * @author Daniel Sagenschneider
	 */
	private class EscalationStruct {

		/**
		 * Type of {@link Escalation}.
		 */
		public final Class<? extends Throwable> type;

		/**
		 * {@link EscalationNode}.
		 */
		public final EscalationNode node;

		/**
		 * Initiate.
		 * 
		 * @param type Type of {@link Escalation}.
		 * @param node {@link EscalationNode}.
		 */
		public EscalationStruct(Class<? extends Throwable> type, EscalationNode node) {
			this.type = type;
			this.node = node;
		}
	}

}
