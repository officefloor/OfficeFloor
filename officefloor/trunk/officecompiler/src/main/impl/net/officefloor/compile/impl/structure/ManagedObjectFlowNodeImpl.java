/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.compile.impl.structure;

import net.officefloor.compile.internal.structure.LinkFlowNode;
import net.officefloor.compile.internal.structure.ManagedObjectFlowNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link ManagedObjectFlowNode} implementation.
 * 
 * @author Daniel
 */
public class ManagedObjectFlowNodeImpl implements ManagedObjectFlowNode {

	/**
	 * Name of this {@link ManagedObjectFlow}.
	 */
	private final String managedObjectFlowName;

	/**
	 * Location of the {@link OfficeSection} containing this
	 * {@link ManagedObjectFlowNode}.
	 */
	private final String sectionLocation;

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues;

	/**
	 * Flags if within context of the {@link Office}.
	 */
	private boolean isInOfficeContext = false;

	/**
	 * Location of the {@link Office}.
	 */
	private String officeLocation;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectFlowName
	 *            Name of this {@link ManagedObjectFlow}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link ManagedObjectFlowNode}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public ManagedObjectFlowNodeImpl(String managedObjectFlowName,
			String sectionLocation, CompilerIssues issues) {
		this.managedObjectFlowName = managedObjectFlowName;
		this.sectionLocation = sectionLocation;
		this.issues = issues;
	}

	/*
	 * ================== ManagedObjectFlowNode ==============================
	 */

	@Override
	public void addOfficeContext(String officeLocation) {
		this.officeLocation = officeLocation;
		this.isInOfficeContext = true;
	}

	/*
	 * =================== ManagedObjectFlow =============================
	 */

	@Override
	public String getManagedObjectFlowName() {
		return this.managedObjectFlowName;
	}

	/*
	 * =================== LinkFlowNode =================================
	 */

	/**
	 * Linked {@link LinkFlowNode}.
	 */
	private LinkFlowNode linkedFlowNode;

	@Override
	public boolean linkFlowNode(LinkFlowNode node) {

		// Ensure not already linked
		if (this.linkedFlowNode != null) {
			if (this.isInOfficeContext) {
				// Office managed object flow
				this.issues.addIssue(LocationType.OFFICE, this.officeLocation,
						null, null, "Managed object flow "
								+ this.managedObjectFlowName
								+ " linked more than once");
			} else {
				// Section managed object flow
				this.issues.addIssue(LocationType.SECTION,
						this.sectionLocation, null, null,
						"Managed object flow " + this.managedObjectFlowName
								+ " linked more than once");
			}
			return false; // already linked
		}

		// Link
		this.linkedFlowNode = node;
		return true;
	}

	@Override
	public LinkFlowNode getLinkedFlowNode() {
		return this.linkedFlowNode;
	}

}