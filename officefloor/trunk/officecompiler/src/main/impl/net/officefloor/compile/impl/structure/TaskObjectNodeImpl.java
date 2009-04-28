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

import net.officefloor.compile.impl.util.LinkUtil;
import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.internal.structure.TaskObjectNode;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.DependentManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionObject;
import net.officefloor.compile.spi.section.TaskObject;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link TaskObjectNode} implementation.
 * 
 * @author Daniel
 */
public class TaskObjectNodeImpl implements TaskObjectNode {

	/**
	 * Name of this {@link TaskObject}.
	 */
	private final String objectName;

	/**
	 * Location of the {@link OfficeSection} containing this {@link TaskObject}.
	 */
	private final String sectionLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * Flags whether within the {@link Office} context.
	 */
	private boolean isInOfficeContext = false;

	/**
	 * Location of the {@link Office} containing this
	 * {@link OfficeSectionObject}.
	 */
	private String officeLocation;

	/**
	 * Initiate.
	 * 
	 * @param objectName
	 *            Name of this {@link TaskObject}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link TaskObject}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public TaskObjectNodeImpl(String objectName, String sectionLocation,
			NodeContext context) {
		this.objectName = objectName;
		this.sectionLocation = sectionLocation;
		this.context = context;
	}

	/*
	 * ==================== TaskObjectNode ============================
	 */

	@Override
	public void addOfficeContext(String officeLocation) {
		this.officeLocation = officeLocation;
		this.isInOfficeContext = true;
	}

	/*
	 * ===================== TaskObject ===============================
	 */

	@Override
	public String getTaskObjectName() {
		return this.objectName;
	}

	/*
	 * ======================= ObjectDependency ===========================
	 */

	@Override
	public String getObjectDependencyName() {
		// Return the object name
		return this.objectName;
	}

	@Override
	public DependentManagedObject getDependentManagedObject() {

		// Ensure in office context
		if (!this.isInOfficeContext) {
			throw new IllegalStateException("Must be in office context");
		}

		// Return the retrieved dependent managed object
		return LinkUtil.retrieveTarget(this, DependentManagedObject.class,
				"TaskObject " + this.objectName, LocationType.OFFICE,
				this.officeLocation, null, null, this.context
						.getCompilerIssues());
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
			this.context.getCompilerIssues()
					.addIssue(
							LocationType.SECTION,
							this.sectionLocation,
							null,
							null,
							"Task object " + this.objectName
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