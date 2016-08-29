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
import net.officefloor.compile.spi.officefloor.DeployedOffice;
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
	 * Sources this {@link Office} into this {@link OfficeNode}.
	 * 
	 * @return <code>true</code> if successfully sourced. Otherwise
	 *         <code>false</code> with issue reported to the
	 *         {@link CompilerIssues}.
	 */
	boolean sourceOffice();

	/**
	 * Obtains the {@link OfficeFloorNode} containing this {@link OfficeNode}.
	 * 
	 * @return {@link OfficeFloorNode} containing this {@link OfficeNode}.
	 */
	OfficeFloorNode getOfficeFloorNode();

	/**
	 * Loads the {@link OfficeType}.
	 * 
	 * @return {@link OfficeType} or <code>null</code> if issue loading with
	 *         issue reported to the {@link CompilerIssues}.
	 */
	OfficeType loadOfficeType();

	/**
	 * Builds the {@link Office} for this {@link OfficeNode}.
	 * 
	 * @param builder
	 *            {@link OfficeFloorBuilder}.
	 * @return {@link OfficeBuilder} for the built {@link Office}.
	 */
	OfficeBuilder buildOffice(OfficeFloorBuilder builder);

}