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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.compile.administrator.AdministratorType;
import net.officefloor.compile.governance.GovernanceType;
import net.officefloor.compile.internal.structure.AdministratorNode;
import net.officefloor.compile.internal.structure.GovernanceNode;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.Node;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.internal.structure.OfficeObjectNode;
import net.officefloor.compile.office.OfficeManagedObjectType;
import net.officefloor.compile.section.SectionObjectType;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeGovernance;
import net.officefloor.compile.spi.office.OfficeObject;
import net.officefloor.frame.spi.governance.Governance;

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
	 * Listing of the {@link AdministratorNode} instances of the
	 * {@link OfficeAdministrator} instances administering this
	 * {@link OfficeObjectNode}.
	 */
	private final List<AdministratorNode> administrators = new LinkedList<AdministratorNode>();

	/**
	 * Listing of the {@link GovernanceNode} instances of the
	 * {@link OfficeGovernance} instances providing {@link Governance} over this
	 * {@link OfficeObjectNode}.
	 */
	private final List<GovernanceNode> governances = new LinkedList<GovernanceNode>();

	/**
	 * Parent {@link OfficeNode}.
	 */
	private final OfficeNode officeNode;

	/**
	 * {@link NodeContext}.
	 */
	private NodeContext context;

	/**
	 * Indicates if this {@link OfficeManagedObjectType} is initialised.
	 */
	private boolean isInitialised = false;

	/**
	 * Object type.
	 */
	private String objectType;

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
	public OfficeObjectNodeImpl(String objectName, OfficeNode office,
			NodeContext context) {
		this.objectName = objectName;
		this.officeNode = office;
		this.context = context;
	}

	/*
	 * ================== Node ========================
	 */

	@Override
	public String getNodeName() {
		// TODO implement Node.getNodeName
		throw new UnsupportedOperationException(
				"TODO implement Node.getNodeName");

	}

	@Override
	public String getNodeType() {
		// TODO implement Node.getNodeType
		throw new UnsupportedOperationException(
				"TODO implement Node.getNodeType");

	}

	@Override
	public String getLocation() {
		// TODO implement Node.getLocation
		throw new UnsupportedOperationException(
				"TODO implement Node.getLocation");

	}

	@Override
	public Node getParentNode() {
		// TODO implement Node.getParentNode
		throw new UnsupportedOperationException(
				"TODO implement Node.getParentNode");

	}

	/*
	 * ================== OfficeObjectNode ========================
	 */

	@Override
	public boolean isInitialised() {
		return this.isInitialised;
	}

	@Override
	public OfficeObjectNode initialise(String objectType) {
		this.objectType = objectType;
		this.isInitialised = true;
		return this;
	}

	@Override
	public void addAdministrator(AdministratorNode administrator) {
		this.administrators.add(administrator);
	}

	@Override
	public void addGovernance(GovernanceNode governance) {
		this.governances.add(governance);
	}

	@Override
	public GovernanceNode[] getGovernances() {
		return this.governances.toArray(new GovernanceNode[this.governances
				.size()]);
	}

	/*
	 * ===================== OfficeManagedObjectType ===========================
	 */

	@Override
	public String getOfficeManagedObjectName() {
		return this.objectName;
	}

	@Override
	public String getObjectType() {
		return this.objectType;
	}

	@Override
	public String getTypeQualifier() {
		return this.typeQualifier;
	}

	@Override
	public String[] getExtensionInterfaces() {

		// Obtain the set of extension interfaces to be supported
		Set<String> extensionInterfaces = new HashSet<String>();

		// Load the extension interfaces for administration
		for (AdministratorNode admin : this.administrators) {

			// Attempt to obtain the administrator type
			AdministratorType<?, ?> adminType = admin.getAdministratorType();
			if (adminType == null) {
				continue; // problem loading administrator type
			}

			// Add the extension interface
			Class<?> extensionInterface = adminType.getExtensionInterface();
			extensionInterfaces.add(extensionInterface.getName());
		}

		// Load the extension interfaces for governance
		for (GovernanceNode governance : this.governances) {

			// Attempt to obtain the governance type
			GovernanceType<?, ?> govType = governance.getGovernanceType();
			if (govType == null) {
				continue; // problem loading governance type
			}

			// Add the extension interface
			Class<?> extensionInterface = govType.getExtensionInterface();
			extensionInterfaces.add(extensionInterface.getName());
		}

		// Create and return the listing of sorted extension interfaces
		String[] listing = extensionInterfaces.toArray(new String[0]);
		Arrays.sort(listing);
		return listing;
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

		// Ensure not already linked
		if (this.linkedObjectNode != null) {
			// Office object already linked
			this.context.getCompilerIssues().addIssue(
					this,
					"Office object " + this.objectName
							+ " linked more than once");
			return false; // already linked
		}

		// Link
		this.linkedObjectNode = node;
		return true;
	}

	@Override
	public LinkObjectNode getLinkedObjectNode() {
		return this.linkedObjectNode;
	}

}