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
package net.officefloor.compile.team;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.officefloor.OfficeFloorTeamSourceType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.team.source.TeamSource;
import net.officefloor.frame.spi.team.source.TeamSourceProperty;
import net.officefloor.frame.spi.team.source.TeamSourceSpecification;

/**
 * Loads the {@link TeamType} from the {@link TeamSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface TeamLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link TeamSourceSpecification} for the {@link TeamSource}.
	 * 
	 * @param <TS>
	 *            {@link TeamSource} type.
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
	 * @param <TS>
	 *            {@link TeamSource} type.
	 * @param teamSourceClass
	 *            Class of the {@link TeamSource}.
	 * @param propertyList
	 *            {@link PropertyList} containing the properties to source the
	 *            {@link TeamType}.
	 * @return {@link TeamType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<TS extends TeamSource> TeamType loadTeamType(Class<TS> teamSourceClass,
			PropertyList propertyList);

	/**
	 * Loads and returns the {@link OfficeFloorTeamSourceType}.
	 * 
	 * @param <TS>
	 *            {@link TeamSource} type.
	 * @param teamSourceClass
	 *            Class of the {@link TeamSource}.
	 * @param propertyList
	 *            {@link PropertyList} for configuring the {@link TeamSource}.
	 * @return {@link OfficeFloorTeamSourceType} or <code>null</code> if issues,
	 *         which are reported to the {@link CompilerIssues}.
	 */
	<TS extends TeamSource> OfficeFloorTeamSourceType loadOfficeFloorTeamSourceType(
			Class<TS> teamSourceClass, PropertyList propertyList);

}