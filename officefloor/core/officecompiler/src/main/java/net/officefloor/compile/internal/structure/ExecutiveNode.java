/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
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

import net.officefloor.compile.executive.ExecutiveType;
import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.spi.officefloor.OfficeFloorExecutive;
import net.officefloor.frame.api.executive.source.ExecutiveSource;

/**
 * {@link OfficeFloorExecutive} node.
 * 
 * @author Daniel Sagenschneider
 */
public interface ExecutiveNode extends Node, OfficeFloorExecutive {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Executive";

	/**
	 * Initialises the {@link ExecutiveNode}.
	 * 
	 * @param executiveSourceClassName Class name of the {@link ExecutiveSource}.
	 * @param executiveSource          Optional instantiated
	 *                                 {@link ExecutiveSource}. May be
	 *                                 <code>null</code>.
	 */
	void initialise(String executiveSourceClassName, ExecutiveSource executiveSource);

	/**
	 * Loads the {@link ExecutiveType} for the {@link ExecutiveSource}.
	 * 
	 * @return {@link ExecutiveType} or <code>null</code> with issues reported to
	 *         the {@link CompilerIssues}.
	 */
	ExecutiveType loadExecutiveType();

}