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
import net.officefloor.compile.office.OfficeType;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.source.OfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.profile.Profiler;

/**
 * {@link Office} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeNode
		extends LinkOfficeNode, ManagedObjectRegistry, OfficeTeamRegistry, OfficeArchitect, DeployedOffice {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Office";

	/**
	 * Initialises the {@link OfficeNode}.
	 * 
	 * @param officeSourceClassName
	 *            {@link OfficeSource} class name.
	 * @param officeSource
	 *            Optional instantiated {@link OfficeSource}. May be
	 *            <code>null</code>.
	 * @param officeLocation
	 *            Location of the {@link Office}.
	 */
	void initialise(String officeSourceClassName, OfficeSource officeSource, String officeLocation);

	/**
	 * Sources this {@link Office} along with its top level
	 * {@link OfficeSection} instances into this {@link OfficeNode}.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceOfficeWithTopLevelSections(TypeContext typeContext);

	/**
	 * Sources this {@link Office} and all descendant {@link Node} instances.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceOfficeTree(TypeContext typeContext);

	/**
	 * Obtains the {@link OfficeFloorNode} containing this {@link OfficeNode}.
	 * 
	 * @return {@link OfficeFloorNode} containing this {@link OfficeNode}.
	 */
	OfficeFloorNode getOfficeFloorNode();

	/**
	 * Loads the {@link OfficeType}.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link OfficeType} or <code>null</code> if issue loading with
	 *         issue reported to the {@link CompilerIssues}.
	 */
	OfficeType loadOfficeType(TypeContext typeContext);

	/**
	 * Builds the {@link Office} for this {@link OfficeNode}.
	 * 
	 * @param builder
	 *            {@link OfficeFloorBuilder}.
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @param profiler
	 *            Optional {@link Profiler}. May be <code>null</code>.
	 * @return {@link OfficeBuilder} for the built {@link Office}.
	 */
	OfficeBindings buildOffice(OfficeFloorBuilder builder, TypeContext typeContext, Profiler profiler);

}