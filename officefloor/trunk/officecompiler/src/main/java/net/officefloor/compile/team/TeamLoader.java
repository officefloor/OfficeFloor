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
package net.officefloor.compile.team;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceProperty;
import net.officefloor.frame.spi.team.source.TeamSourceSpecification;

/**
 * Loads the {@link TeamType} from the {@link TeamSource}.
 * 
 * @author Daniel
 */
public interface TeamLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link TeamSourceSpecification} for the {@link TeamSource}.
	 * 
	 * @param teamSourceClass
	 *            Class of the {@link TeamSource}.
	 * @return {@link PropertyList} of the {@link TeamSourceProperty} instances
	 *         of the {@link TeamSourceSpecification} or <code>null</code> if
	 *         issues, which are reported to the {@link CompilerIssues}.
	 */
	<TS extends TeamSource> PropertyList loadSpecification(
			Class<TS> teamSourceClass);

	/**
	 * Loads and returns the {@link TeamType} sourced from the
	 * {@link TeamSource}.
	 * 
	 * @param teamSourceClass
	 *            Class of the {@link TeamSource}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link TeamType}.
	 * @return {@link TeamType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	// TODO rename to loadTeamType
	<TS extends TeamSource> TeamType loadTeam(Class<TS> teamSourceClass,
			PropertyList propertyList);

}