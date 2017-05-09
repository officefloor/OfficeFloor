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
import net.officefloor.compile.officefloor.OfficeFloorType;
import net.officefloor.compile.properties.PropertyConfigurable;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObjectSource;
import net.officefloor.compile.spi.officefloor.OfficeFloorSupplier;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.profile.Profiler;

/**
 * {@link OfficeFloor} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeFloorNode extends Node, PropertyConfigurable, ManagedObjectRegistry, OfficeFloorDeployer {

	/**
	 * Default name of the {@link OfficeFloorNode}.
	 */
	static String OFFICE_FLOOR_NAME = "OfficeFloor";

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "OfficeFloor";

	/**
	 * Initialises the {@link OfficeFloorNode}.
	 */
	void initialise();

	/**
	 * Adds a {@link Profiler} for an {@link Office}.
	 * 
	 * @param officeName
	 *            Name of the {@link Office} to profile.
	 * @param profiler
	 *            {@link Profiler}.
	 */
	void addProfiler(String officeName, Profiler profiler);

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
	OfficeFloorManagedObjectSource addManagedObjectSource(String managedObjectSourceName,
			SuppliedManagedObjectNode suppliedManagedObject);

	/**
	 * <p>
	 * Sources the {@link OfficeFloor} into this {@link OfficeFloorNode}.
	 * <p>
	 * This will only source the top level {@link OfficeSection}.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceOfficeFloor(TypeContext typeContext);

	/**
	 * Sources this {@link OfficeFloorNode} and all its descendant {@link Node}
	 * instances recursively.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceOfficeFloorTree(TypeContext typeContext);

	/**
	 * Loads the {@link AutoWire} targets for the
	 * {@link OfficeFloorManagedObject} instances.
	 * 
	 * @param autoWirer
	 *            {@link AutoWirer} to be loaded with the
	 *            {@link OfficeFloorManagedObject} targets.
	 * @param typeContext
	 *            {@link TypeContext}.
	 */
	void loadAutoWireObjectTargets(AutoWirer<LinkObjectNode> autoWirer, TypeContext typeContext);

	/**
	 * Loads the {@link OfficeFloorType}.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return <code>true</code> if the {@link OfficeFloorType} was loaded.
	 */
	OfficeFloorType loadOfficeFloorType(TypeContext typeContext);

	/**
	 * Deploys the {@link OfficeFloor}.
	 * 
	 * @param officeFrame
	 *            {@link OfficeFrame} to deploy the {@link OfficeFloor} within.
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link OfficeFloor}.
	 */
	OfficeFloor deployOfficeFloor(OfficeFrame officeFrame, TypeContext typeContext);

}