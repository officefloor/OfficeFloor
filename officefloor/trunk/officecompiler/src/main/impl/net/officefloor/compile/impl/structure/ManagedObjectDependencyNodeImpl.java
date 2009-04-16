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

import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.ManagedObjectDependencyNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link ManagedObjectDependencyNode} implementation.
 * 
 * @author Daniel
 */
public class ManagedObjectDependencyNodeImpl implements
		ManagedObjectDependencyNode {

	/**
	 * Name of this {@link ManagedObjectDependency}.
	 */
	private final String dependencyName;

	/**
	 * Location of the {@link OfficeSection} containing this
	 * {@link ManagedObjectDependencyNode}.
	 */
	private final String sectionLocation;

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues;

	/**
	 * Flag indicating if within {@link Office}.
	 */
	private boolean isInOfficeContext = false;

	/**
	 * Location of the {@link Office} containing this
	 * {@link ManagedObjectDependency}.
	 */
	private String officeLocation;

	/**
	 * Initiate.
	 * 
	 * @param dependencyName
	 *            Name of this {@link ManagedObjectDependency}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link ManagedObjectDependencyNode}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public ManagedObjectDependencyNodeImpl(String dependencyName,
			String sectionLocation, CompilerIssues issues) {
		this.dependencyName = dependencyName;
		this.sectionLocation = sectionLocation;
		this.issues = issues;
	}

	/*
	 * ==================== ManagedObjectDependency ============================
	 */

	@Override
	public String getManagedObjectDependencyName() {
		return this.dependencyName;
	}

	/*
	 * ================= ManagedObjectDependencyNode ====================
	 */

	@Override
	public void addOfficeContext(String officeLocation) {
		this.officeLocation = officeLocation;
		this.isInOfficeContext = true;
	}

	/*
	 * ===================== LinkObjectNode ===========================
	 */

	/**
	 * Linked {@link LinkObjectNode}.
	 */
	private LinkObjectNode linkedObjectNode;

	@Override
	public boolean linkObjectNode(LinkObjectNode node) {

		// Ensure not already linked
		if (this.linkedObjectNode != null) {
			if (this.isInOfficeContext) {
				// Office dependency
				this.issues.addIssue(LocationType.OFFICE, this.officeLocation,
						null, null, "Managed object dependency "
								+ this.dependencyName
								+ " linked more than once");
			} else {
				// Section dependency
				this.issues.addIssue(LocationType.SECTION,
						this.sectionLocation, null, null,
						"Managed object dependency " + this.dependencyName
								+ " linked more than once");
			}
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