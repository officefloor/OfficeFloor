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

import net.officefloor.compile.internal.structure.LinkOfficeNode;
import net.officefloor.compile.internal.structure.ManagingOfficeNode;
import net.officefloor.compile.internal.structure.NodeContext;
import net.officefloor.compile.issues.CompilerIssues.LocationType;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.spi.managedobject.ManagedObject;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;

/**
 * {@link ManagingOfficeNode} implementation.
 * 
 * @author Daniel
 */
public class ManagingOfficeNodeImpl implements ManagingOfficeNode {

	/**
	 * Name of the {@link ManagedObjectSource} for which this is the
	 * {@link ManagingOffice}.
	 */
	private final String managedObjectSourceName;

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String officeFloorLocation;

	/**
	 * {@link NodeContext}.
	 */
	private final NodeContext context;

	/**
	 * {@link ProcessState} bound {@link ManagedObject} name for the
	 * {@link ManagedObjectSource}.
	 */
	private String processBoundManagedObjectName;

	/**
	 * Initiate.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link ManagedObjectSource} for which this is the
	 *            {@link ManagingOffice}.
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param context
	 *            {@link NodeContext}.
	 */
	public ManagingOfficeNodeImpl(String managedObjectSourceName,
			String officeFloorLocation, NodeContext context) {
		this.managedObjectSourceName = managedObjectSourceName;
		this.officeFloorLocation = officeFloorLocation;
		this.context = context;
	}

	/*
	 * ================== ManagingOfficeNode ===========================
	 */

	@Override
	public String getProcessBoundManagedObjectName() {
		return this.processBoundManagedObjectName;
	}

	@Override
	public void setProcessBoundManagedObjectName(
			String processBoundManagedObjectName) {
		this.processBoundManagedObjectName = processBoundManagedObjectName;
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

		// Ensure not already linked
		if (this.linkedOfficeNode != null) {
			this.context.getCompilerIssues().addIssue(
					LocationType.OFFICE_FLOOR,
					this.officeFloorLocation,
					null,
					null,
					"Managing office for managed object source "
							+ this.managedObjectSourceName
							+ " linked more than once");
			return false; // already linked
		}

		// Link
		this.linkedOfficeNode = node;
		return true;
	}

	@Override
	public LinkOfficeNode getLinkedOfficeNode() {
		return this.linkedOfficeNode;
	}

}