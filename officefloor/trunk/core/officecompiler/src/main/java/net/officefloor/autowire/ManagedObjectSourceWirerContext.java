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
package net.officefloor.autowire;

import net.officefloor.compile.spi.office.ManagedObjectTeam;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.OfficeSectionInput;
import net.officefloor.compile.spi.section.ManagedObjectDependency;
import net.officefloor.compile.spi.section.ManagedObjectFlow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.source.TeamSource;

/**
 * Context for the {@link ManagedObjectSourceWirer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectSourceWirerContext {

	/**
	 * Overrides the default {@link ManagedObjectScope}.
	 * 
	 * @param managedobjectScope
	 *            {@link ManagedObjectScope}.
	 */
	void setManagedObjectScope(ManagedObjectScope managedobjectScope);

	/**
	 * Maps a {@link TeamSource} for the {@link ManagedObjectTeam}.
	 * 
	 * @param <S>
	 *            {@link TeamSource} type.
	 * @param managedObjectSourceTeamName
	 *            Name of the {@link ManagedObjectTeam}.
	 * @param teamSourceClassName
	 *            {@link TeamSource} class name. May be an alias.
	 * @return {@link AutoWireTeam}.
	 */
	<S extends TeamSource> AutoWireTeam mapTeam(
			String managedObjectSourceTeamName, String teamSourceClassName);

	/**
	 * Maps {@link Team} by {@link AutoWire}.
	 * 
	 * @param managedObjectSourceTeamName
	 *            Name of the {@link ManagedObjectTeam}.
	 * @param autoWire
	 *            {@link AutoWire} to identify the {@link AutoWireTeam}.
	 */
	void mapTeam(String managedObjectSourceTeamName, AutoWire autoWire);

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
	 * Overrides the type for the dependency to allow more specific auto-wiring
	 * by {@link AutoWire}.
	 * 
	 * @param dependencyName
	 *            Name of the {@link ManagedObjectDependency}.
	 * @param autoWire
	 *            Specific {@link AutoWire} the dependency must
	 *            implement/extend.
	 */
	void mapDependency(String dependencyName, AutoWire autoWire);

}