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

import net.officefloor.compile.internal.structure.OfficeFloorNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeRequiredManagedObject;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ManagingOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloorNode} implementation.
 * 
 * @author Daniel
 */
public class OfficeFloorNodeImpl implements OfficeFloorNode {

	/**
	 * Location of the {@link OfficeFloor}.
	 */
	private final String officeFloorLocation;

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues;

	/**
	 * Initiate.
	 * 
	 * @param officeFloorLocation
	 *            Location of the {@link OfficeFloor}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public OfficeFloorNodeImpl(String officeFloorLocation, CompilerIssues issues) {
		this.officeFloorLocation = officeFloorLocation;
		this.issues = issues;
	}

	/*
	 * ===================== OfficeFloorDeployer =============================
	 */

	@Override
	public OfficeFloorManagedObject addManagedObject(String managedObjectName,
			String managedObjectSourceClassName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorDeployer.addManagedObject");
	}

	@Override
	public OfficeFloorTeam addTeam(String teamName, String teamSourceClassName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorDeployer.addTeam");
	}

	@Override
	public DeployedOffice deployOffice(String officeName,
			String officeSourceClassName, String officeLocation) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorDeployer.deployOffice");
	}

	@Override
	public void link(ManagedObjectTeam team, OfficeFloorTeam officeFloorTeam) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorDeployer.link");
	}

	@Override
	public void link(ManagedObjectDependency dependency,
			OfficeFloorManagedObject managedObject) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorDeployer.link");
	}

	@Override
	public void link(ManagedObjectFlow flow, DeployedOfficeInput input) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorDeployer.link");
	}

	@Override
	public void link(ManagingOffice managingOffice, DeployedOffice office) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorDeployer.link");
	}

	@Override
	public void link(OfficeTeam team, OfficeFloorTeam officeFloorTeam) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorDeployer.link");
	}

	@Override
	public void link(OfficeRequiredManagedObject requiredManagedObject,
			OfficeFloorManagedObject officeFloorManagedObject) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeFloorDeployer.link");
	}

}