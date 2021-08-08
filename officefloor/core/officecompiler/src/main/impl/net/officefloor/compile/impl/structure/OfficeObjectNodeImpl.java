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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.compile.administration.AdministrationType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.impl.office.OfficeManagedObjectTypeImpl;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.AdministrationNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.internal.structure.CompileContext;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.frame.api.governance.Governance;

/**
 * {@link OfficeObjectNode} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeObjectNodeImpl implements OfficeObjectNode {

	/**
	 * Name of the {@link SectionObjectType}.
	 */
	private final String objectName;

	/**
	 * Listing of the {@link AdministrationNode} instances of the
	 * {@link OfficeAdministration} instances administering this
	 * {@link OfficeObjectNode}.
	 */
	private final List<AdministrationNode> administrators = new LinkedList<AdministrationNode>();

	/**
	 * Listing of the {@link GovernanceNode} instances of the
	 * {@link OfficeGovernance} instances providing {@link Governance} over this
	 * {@link OfficeObjectNode}.
	 */
	private final List<GovernanceNode> governances = new LinkedList<GovernanceNode>();

	/**
	 * Pre-load {@link OfficeAdministration}.
	 */
	private final List<OfficeAdministration> preLoadAdministrations = new LinkedList<>();

	/**
	 * Parent {@link OfficeNode}.
	 */
	private final OfficeNode officeNode;

	/**
	 * {@link NodeContext}.
	 */
	private NodeContext context;

	/**
	 * {@link InitialisedState}.
	 */
	private InitialisedState state;

	/**
	 * Initialised state.
	 */
	private static class InitialisedState {

		/**
		 * Object type.
		 */
		private String objectType;

		/**
		 * Instantiate.
		 * 
		 * @param objectType
		 *            Object type.
		 */
		public InitialisedState(String objectType) {
			this.objectType = objectType;
		}
	}

	/**
	 * Type qualifier.
	 */
	private String typeQualifier = null;

	/**
	 * Allow adding a {@link OfficeObject} via the {@link OfficeArchitect}.
	 * 
	 * @param objectName
	 *            Name of the {@link OfficeObject}.
	 * @param office
	 *            Parent {@link OfficeNode}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public OfficeObjectNodeImpl(String objectName, OfficeNode office, NodeContext context) {
		this.objectName = objectName;
		this.officeNode = office;
		this.context = context;
	}

	/*
	 * ================== Node ========================
	 */

	@Override
	public String getNodeName() {
		return this.objectName;
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
	public void initialise(String objectType) {
		this.state = NodeUtil.initialise(this, this.context, this.state, () -> new InitialisedState(objectType));
	}

	/*
	 * ================== OfficeObjectNode ========================
	 */

	@Override
	public void addAdministrator(AdministrationNode administrator) {
		this.administrators.add(administrator);
	}

	@Override
	public void addGovernance(GovernanceNode governance) {
		this.governances.add(governance);
	}

	@Override
	public GovernanceNode[] getGovernances() {
		return this.governances.toArray(new GovernanceNode[this.governances.size()]);
	}

	@Override
	public AdministrationNode[] getPreLoadAdministrations() {
		return this.preLoadAdministrations.toArray(new AdministrationNode[this.preLoadAdministrations.size()]);
	}

	@Override
	public String getTypeQualifier() {
		return this.typeQualifier;
	}

	@Override
	public String getOfficeObjectType() {
		return this.state.objectType;
	}

	@Override
	public OfficeManagedObjectType loadOfficeManagedObjectType(CompileContext compileContext) {

		// Ensure have name
		if (CompileUtil.isBlank(this.objectName)) {
			this.context.getCompilerIssues().addIssue(this, "Null name for " + TYPE);
			return null; // must have name
		}

		// Ensure have type
		if (CompileUtil.isBlank(this.state.objectType)) {
			this.context.getCompilerIssues().addIssue(this,
					"Null type for managed object (name=" + this.objectName + ")");
			return null; // must have type
		}

		// Obtain the set of extension interfaces to be supported
		Set<String> extensionInterfaces = new HashSet<String>();

		// Load the extension interfaces for administration
		for (AdministrationNode admin : this.administrators) {

			// Attempt to obtain the administrator type
			AdministrationType<?, ?, ?> adminType = compileContext.getOrLoadAdministrationType(admin);
			if (adminType == null) {
				continue; // problem loading administrator type
			}

			// Add the extension interface
			Class<?> extensionInterface = adminType.getExtensionType();
			extensionInterfaces.add(extensionInterface.getName());
		}

		// Load the extension interfaces for governance
		for (GovernanceNode governance : this.governances) {

			// Attempt to obtain the governance type
			GovernanceType<?, ?> govType = compileContext.getOrLoadGovernanceType(governance);
			if (govType == null) {
				continue; // problem loading governance type
			}

			// Add the extension interface
			Class<?> extensionInterface = govType.getExtensionType();
			extensionInterfaces.add(extensionInterface.getName());
		}

		// Create and return the listing of sorted extension interfaces
		String[] listing = extensionInterfaces.toArray(new String[0]);
		Arrays.sort(listing);

		// Create and return the extension interfaces
		return new OfficeManagedObjectTypeImpl(this.objectName, (this.state != null ? this.state.objectType : null),
				this.typeQualifier, listing);
	}

	/*
	 * ==================== OfficeObject ========================
	 */

	@Override
	public String getOfficeObjectName() {
		return this.objectName;
	}

	@Override
	public void setTypeQualifier(String qualifier) {
		this.typeQualifier = qualifier;
	}

	@Override
	public void addPreLoadAdministration(OfficeAdministration administration) {
		this.preLoadAdministrations.add(administration);
	}

	/*
	 * =================== DependentManagedObject =============================
	 */

	@Override
	public String getDependentManagedObjectName() {
		return this.objectName;
	}

	/*
	 * ================== AdministerableManagedObject =========================
	 */

	@Override
	public String getAdministerableManagedObjectName() {
		return this.objectName;
	}

	/*
	 * ==================== GovernerableManagedObject ==========================
	 */

	@Override
	public String getGovernerableManagedObjectName() {
		return this.objectName;
	}

	/*
	 * =============== LinkObjectNode ==============================
	 */

	/**
	 * Linked {@link LinkObjectNode}.
	 */
	private LinkObjectNode linkedObjectNode;

	@Override
	public boolean linkObjectNode(LinkObjectNode node) {
		return LinkUtil.linkObjectNode(this, node, this.context.getCompilerIssues(),
				(link) -> this.linkedObjectNode = link);
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectNode;
	}

}
