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
package net.officefloor.compile.internal.structure;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.officefloor.OfficeFloorPropertyType;
import net.officefloor.compile.officefloor.OfficeFloorType;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorNode extends Node, OfficeFloorDeployer {

	/**
	 * Default name of the {@link OfficeFloorNode}.
	 */
	static final String OFFICE_FLOOR_NAME = "OfficeFloor";

	/**
	 * Adds a {@link OfficeFloorManagedObjectSource} supplied from an
	 * {@link OfficeFloorSupplier}.
	 * 
	 * @param managedObjectSourceName
	 *            Name of the {@link OfficeFloorManagedObjectSource}.
	 * @param suppliedManagedObject
	 *            {@link SuppliedManagedObjectNode} to supply the
	 *            {@link OfficeFloorManagedObjectSource}.
	 * @return {@link OfficeFloorManagedObjectSource}.
	 */
	OfficeFloorManagedObjectSource addManagedObjectSource(
			String managedObjectSourceName,
			SuppliedManagedObjectNode suppliedManagedObject);

	/**
	 * Loads the {@link OfficeFloorType}.
	 * 
	 * @param properties
	 *            {@link OfficeFloorPropertyType} instances to configure the
	 *            {@link OfficeFloor}.
	 * @return <code>true</code> if the {@link OfficeFloorType} was loaded.
	 */
	boolean loadOfficeFloorType(OfficeFloorPropertyType[] properties);

	/**
	 * Obtains the {@link OfficeFloorType}.
	 * 
	 * @return {@link OfficeFloorType} or <code>null</code> if issue loading
	 *         with issue reported to the {@link CompilerIssues}.
	 */
	OfficeFloorType getOfficeFloorType();

	/**
	 * Deploys the {@link OfficeFloor}.
	 * 
	 * @param officeFrame
	 *            {@link OfficeFrame} to deploy the {@link OfficeFloor} within.
	 * @return {@link OfficeFloor}.
	 */
	OfficeFloor deployOfficeFloor(OfficeFrame officeFrame);

}