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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.compile.governance.GovernanceLoader;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.AutoWire;
import net.officefloor.compile.internal.structure.AutoWireLink;
import net.officefloor.compile.internal.structure.AutoWirer;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.LinkTeamNode;
import net.officefloor.compile.internal.structure.ManagedObjectExtensionNode;
import net.officefloor.compile.internal.structure.ManagedObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.internal.structure.OfficeTeamNode;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.governance.source.GovernanceSource;
import net.officefloor.compile.spi.office.GovernerableManagedObject;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.frame.api.build.GovernanceBuilder;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.governance.Governance;
import net.officefloor.frame.api.manage.Office;

/**
 * Implementation of the {@link GovernanceNode}.
 * 
 * @author Daniel Sagenschneider
 */
public class GovernanceNodeImpl implements GovernanceNode {

	/**
	 * Name of this {@link OfficeGovernance}.
	 */
	private final String governanceName;

	/**
	 * {@link PropertyList} to source the {@link Governance}.
	 */
	private final PropertyList properties;

	/**
	 * {@link OfficeNode} of the {@link Office} containing this {@link Governance}.
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
		 * Class name of the {@link GovernanceSource}.
		 */
		private final String governanceSourceClassName;

		/**
		 * {@link GovernanceSource} instance to use. Should this be specified it
		 * overrides the {@link Class}.
		 */
		private final GovernanceSource<?, ?> governanceSource;

		/**
		 * Instantiate.
		 * 
		 * @param governanceSourceClassName Class name of the {@link GovernanceSource}.
		 * @param governanceSource          {@link GovernanceSource} instance to use.
		 *                                  Should this be specified it overrides the
		 *                                  {@link Class}.
		 */
		public InitialisedState(String governanceSourceClassName, GovernanceSource<?, ?> governanceSource) {
			this.governanceSourceClassName = governanceSourceClassName;
			this.governanceSource = governanceSource;
		}
	}

	/**
	 * {@link OfficeObjectNode} instances being governed by this {@link Governance}.
	 */
	private final List<OfficeObjectNode> governedOfficeObjects = new LinkedList<>();

	/**
	 * {@link ManagedObjectNode} instances being governed by this
	 * {@link Governance}.
	 */
	private final List<ManagedObjectNode> governedManagedObjects = new LinkedList<>();

	/**
	 * Flags whether to auto-wire the {@link GovernerableManagedObject} instances.
	 */
	private boolean isAutoWireExtensions = false;

	/**
	 * {@link GovernanceSource} used to source this {@link GovernanceNode}.
	 */
	private GovernanceSource<?, ?> usedGovernanceSource = null;

	/**
	 * Initiate.
	 * 
	 * @param governanceName Name of this {@link OfficeGovernance}.
	 * @param officeNode     {@link OfficeNode} of the {@link Office} containing
	 *                       this {@link Governance}.
	 * @param context        {@link NodeContext}.
	 */
	public GovernanceNodeImpl(String governanceName, OfficeNode officeNode, NodeContext context) {
		this.governanceName = governanceName;
		this.officeNode = officeNode;
		this.context = context;

		// Create additional objects
		this.properties = this.context.createPropertyList();
	}

	/*
	 * ========================== Node ==============================
	 */

	@Override
	public String getNodeName() {
		return this.governanceName;
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
	public void initialise(String governanceSourceClassName, GovernanceSource<?, ?> governanceSource) {
		this.state = NodeUtil.initialise(this, this.context, this.state,
				() -> new InitialisedState(governanceSourceClassName, governanceSource));
	}

	/*
	 * ======================== GovernanceNode ======================
	 */

	@Override
	public GovernanceType<?, ?> loadGovernanceType(boolean isLoadingType) {

		// Obtain the goverannce source
		GovernanceSource<?, ?> governanceSource = this.state.governanceSource;
		if (governanceSource == null) {

			// Obtain the governance source class
			Class<? extends GovernanceSource<?, ?>> governanceSourceClass = this.context
					.getGovernanceSourceClass(this.state.governanceSourceClassName, this);
			if (governanceSourceClass == null) {
				return null; // must obtain source class
			}

			// Obtain the governance source
			governanceSource = CompileUtil.newInstance(governanceSourceClass, GovernanceSource.class, this,
					this.context.getCompilerIssues());
			if (governanceSource == null) {
				return null; // must obtain source
			}
		}

		// Keep track of used governance source
		this.usedGovernanceSource = governanceSource;

		// Load and return the governance type
		GovernanceLoader loader = this.context.getGovernanceLoader(this, isLoadingType);
		return loader.loadGovernanceType(governanceSource, this.properties);
	}

	@Override
	@SuppressWarnings("rawtypes")
	public boolean sourceGovernance(CompileContext compileContext) {

		// Obtain the governance type
		GovernanceType govType = compileContext.getOrLoadGovernanceType(this);
		if (govType == null) {
			return false; // must obtain governance type
		}

		// As here, successful
		return true;
	}

	@Override
	public boolean isAutoWireGovernance() {
		return this.isAutoWireExtensions;
	}

	@Override
	public void autoWireExtensions(AutoWirer<ManagedObjectExtensionNode> autoWirer, CompileContext compileContext) {

		// Do no auto wire if already extensions
		if ((this.governedOfficeObjects.size() > 0) || (this.governedManagedObjects.size() > 0)) {
			return;
		}

		// Build the governance type
		GovernanceType<?, ?> governanceType = compileContext.getOrLoadGovernanceType(this);
		if (governanceType == null) {
			return; // must load type
		}

		// Load the auto-wire extensions
		AutoWireLink<GovernanceNode, ManagedObjectExtensionNode>[] links = autoWirer.getAutoWireLinks(this,
				new AutoWire(governanceType.getExtensionType()));
		for (AutoWireLink<GovernanceNode, ManagedObjectExtensionNode> link : links) {
			ManagedObjectNode managedObjectNode = (ManagedObjectNode) link.getTargetNode(this.officeNode);
			managedObjectNode.addGovernance(this, this.officeNode);
			this.governedManagedObjects.add(managedObjectNode);
		}
	}

	@Override
	public void autoWireTeam(AutoWirer<LinkTeamNode> autoWirer, CompileContext compileContext) {

		// Ignore if already specified team
		if (this.linkedTeamNode != null) {
			return;
		}

		// Create the listing of source auto-wires
		Set<AutoWire> autoWires = new HashSet<>();
		this.governedOfficeObjects.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getOfficeObjectName(), b.getOfficeObjectName()))
				.forEachOrdered((object) -> LinkUtil.loadAllObjectAutoWires(object, autoWires, compileContext,
						this.context.getCompilerIssues()));
		this.governedManagedObjects.stream()
				.sorted((a, b) -> CompileUtil.sortCompare(a.getGovernerableManagedObjectName(),
						b.getGovernerableManagedObjectName()))
				.forEachOrdered((managedObject) -> LinkUtil.loadAllObjectAutoWires(managedObject, autoWires,
						compileContext, this.context.getCompilerIssues()));
		AutoWire[] sourceAutoWires = autoWires.stream().toArray(AutoWire[]::new);

		// Attempt to auto-wire this governance
		AutoWireLink<GovernanceNode, LinkTeamNode>[] links = autoWirer.findAutoWireLinks(this, sourceAutoWires);
		if (links.length == 1) {
			LinkUtil.linkTeamNode(this, links[0].getTargetNode(this.officeNode), this.context.getCompilerIssues(),
					(link) -> this.linkTeamNode(link));
		}
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void buildGovernance(OfficeBuilder officeBuilder, CompileContext compileContext) {

		// Obtain the governance type
		GovernanceType govType = compileContext.getOrLoadGovernanceType(this);
		if (govType == null) {
			return; // must obtain governance type
		}

		// Register as possible MBean
		String qualifiedName = this.officeNode.getQualifiedName(this.governanceName);
		compileContext.registerPossibleMBean(GovernanceSource.class, qualifiedName, this.usedGovernanceSource);

		// Build the governance
		GovernanceBuilder govBuilder = officeBuilder.addGovernance(this.governanceName, govType.getExtensionType(),
				govType.getGovernanceFactory());

		// Obtain the office team responsible for this governance
		OfficeTeamNode officeTeam = LinkUtil.findTarget(this, OfficeTeamNode.class, this.context.getCompilerIssues());
		if (officeTeam != null) {
			// Build the team responsible for the governance
			govBuilder.setResponsibleTeam(officeTeam.getOfficeTeamName());
		}
	}

	/*
	 * ======================== OfficeGovernance ======================
	 */

	@Override
	public String getOfficeGovernanceName() {
		return this.governanceName;
	}

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	@Override
	public void governManagedObject(GovernerableManagedObject managedObject) {

		// Register governance with the managed object
		if (managedObject instanceof ManagedObjectNode) {
			// Register governance with the managed object node
			ManagedObjectNode managedObjectNode = (ManagedObjectNode) managedObject;
			managedObjectNode.addGovernance(this, this.officeNode);
			this.governedManagedObjects.add(managedObjectNode);

		} else if (managedObject instanceof OfficeObjectNode) {
			// Register governance with the office object node
			OfficeObjectNode officeObjectNode = (OfficeObjectNode) managedObject;
			officeObjectNode.addGovernance(this);
			this.governedOfficeObjects.add(officeObjectNode);

		} else {
			// Unknown governable managed object node
			this.context.getCompilerIssues().addIssue(this,
					"Unknown " + GovernerableManagedObject.class.getSimpleName() + " node");
		}
	}

	@Override
	public void enableAutoWireExtensions() {
		this.isAutoWireExtensions = true;
	}

	/*
	 * ========================== LinkTeamNode ========================
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
