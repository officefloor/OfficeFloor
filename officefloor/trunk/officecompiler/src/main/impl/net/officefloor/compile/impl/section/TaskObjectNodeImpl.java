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
package net.officefloor.compile.impl.section;

import net.officefloor.compile.internal.structure.LinkObjectNode;
import net.officefloor.compile.internal.structure.TaskObjectNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.office.source.OfficeSection;
import net.officefloor.compile.spi.section.TaskObject;

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
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues;

	/**
	 * Initiate.
	 * 
	 * @param objectName
	 *            Name of this {@link TaskObject}.
	 * @param sectionLocation
	 *            Location of the {@link OfficeSection} containing this
	 *            {@link TaskObject}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public TaskObjectNodeImpl(String objectName, String sectionLocation,
			CompilerIssues issues) {
		this.objectName = objectName;
		this.sectionLocation = sectionLocation;
		this.issues = issues;
	}

	/*
	 * ===================== TaskObject ===============================
	 */

	@Override
	public String getTaskObjectName() {
		return this.objectName;
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
			this.issues.addIssue(LocationType.SECTION, this.sectionLocation,
					null, null, "Task object " + this.objectName
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