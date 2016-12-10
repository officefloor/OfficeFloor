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
import net.officefloor.compile.office.OfficeTeamType;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.type.TypeContext;

/**
 * {@link OfficeTeam} node.
 *
 * @author Daniel Sagenschneider
 */
public interface OfficeTeamNode extends LinkTeamNode, OfficeTeam {

	/**
	 * {@link Node} type.
	 */
	static String TYPE = "Office Team";

	/**
	 * Initialises the {@link OfficeTeamNode}.
	 */
	void initialise();

	/**
	 * Loads the {@link OfficeTeamType}.
	 * 
	 * @param typeContext
	 *            {@link TypeContext}.
	 * @return {@link OfficeTeamType} or <code>null</code> with issues reported
	 *         to the {@link CompilerIssues}.
	 */
	OfficeTeamType loadOfficeTeamType(TypeContext typeContext);

}