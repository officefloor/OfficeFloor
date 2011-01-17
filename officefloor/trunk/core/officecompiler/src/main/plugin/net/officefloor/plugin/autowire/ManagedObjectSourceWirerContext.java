/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.autowire;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorInputManagedObject;
import net.officefloor.compile.spi.officefloor.OfficeFloorManagedObject;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * Context for the {@link ManagedObjectSourceWirer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceWirerContext {

	/**
	 * Flags whether an {@link OfficeFloorInputManagedObject}.
	 * 
	 * @param isInput
	 *            <code>true</code> if {@link OfficeFloorInputManagedObject}.
	 */
	void setInput(boolean isInput);

	/**
	 * Maps a {@link TeamSource} for the {@link ManagedObjectTeam}.
	 * 
	 * @param managedObjectSourceTeamName
	 *            Name of the {@link ManagedObjectTeam}.
	 * @param teamSourceClass
	 *            {@link TeamSource} class.
	 * @return {@link PropertyList} for the {@link TeamSource}.
	 */
	<S extends TeamSource> PropertyList mapTeam(
			String managedObjectSourceTeamName, Class<S> teamSourceClass);

	/**
	 * Maps the {@link ManagedObjectFlow}.
	 * 
	 * @param managedObjectSourceFlowName
	 *            Name of the {@link ManagedObjectFlow}.
	 * @param sectionName
	 *            Name of the {@link OfficeSection}.
	 * @param sectionInputName
	 *            Name of the {@link OfficeSectionInput}.
	 */
	void mapFlow(String managedObjectSourceFlowName, String sectionName,
			String sectionInputName);

	/**
	 * Maps the {@link OfficeFloorManagedObject} or
	 * {@link OfficeFloorInputManagedObject} for the particular dependency type.
	 * 
	 * @param dependencyName
	 *            Name of the {@link ManagedObjectDependency}.
	 * @param type
	 *            Type the dependency must implement/extend.
	 * @return {@link OfficeFloorManagedObject} for the dependency.
	 */
	void mapDependency(String dependencyName, Class<?> type);

}