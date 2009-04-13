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

import net.officefloor.compile.internal.structure.OfficeNode;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeAdministrator;
import net.officefloor.compile.spi.office.OfficeFloorManagedObject;
import net.officefloor.compile.spi.office.OfficeManagedObject;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link OfficeNode} implementation.
 * 
 * @author Daniel
 */
public class OfficeNodeImpl implements OfficeNode {

	/**
	 * Location of the {@link Office}.
	 */
	private final String officeLocation;

	/**
	 * {@link CompilerIssues}.
	 */
	private final CompilerIssues issues;

	/**
	 * Initiate.
	 * 
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 * @param issues
	 *            {@link CompilerIssues}.
	 */
	public OfficeNodeImpl(String officeLocation, CompilerIssues issues) {
		this.officeLocation = officeLocation;
		this.issues = issues;
	}

	/*
	 * ===================== OfficeArchitect ================================
	 */

	@Override
	public OfficeFloorManagedObject addOfficeFloorManagedObject(
			String officeManagedObjectName, String objectType) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeArchitect.addOfficeFloorManagedObject");
	}

	@Override
	public OfficeTeam addTeam(String officeTeamName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeArchitect.addTeam");
	}

	@Override
	public OfficeSection addSection(String sectionName,
			String sectionSourceClassName, String sectionLocation,
			PropertyList properties) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeArchitect.addSection");
	}

	@Override
	public OfficeManagedObject addManagedObject(String managedObjectName,
			String managedObjectSourceClassName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeArchitect.addManagedObject");
	}

	@Override
	public OfficeAdministrator addAdministrator(String administratorName,
			String administratorSourceClassName) {
		// TODO Implement
		throw new UnsupportedOperationException(
				"TODO implement OfficeArchitect.addAdministrator");
	}

}