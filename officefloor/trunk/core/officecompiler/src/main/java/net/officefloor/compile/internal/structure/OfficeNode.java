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
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.type.TypeContext;
import net.officefloor.frame.api.build.OfficeBuilder;
import net.officefloor.frame.api.build.OfficeFloorBuilder;
import net.officefloor.frame.api.manage.Office;

/**
 * {@link Office} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface OfficeNode extends LinkOfficeNode, ManagedObjectRegistry,
		OfficeArchitect, DeployedOffice {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Office";

	/**
	 * Initialises the {@link OfficeNode}.
	 */
	void initialise();

	/**
	 * Sources this {@link Office} along with its top level
	 * {@link OfficeSection} instances into this {@link OfficeNode}.
	 * 
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceOfficeWithTopLevelSections();

	/**
	 * Sources this {@link Office} and all descendant {@link Node} instances.
	 * 
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceOfficeTree();

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
	 * @return {@link OfficeBuilder} for the built {@link Office}.
	 */
	OfficeBuilder buildOffice(OfficeFloorBuilder builder,
			TypeContext typeContext);

}