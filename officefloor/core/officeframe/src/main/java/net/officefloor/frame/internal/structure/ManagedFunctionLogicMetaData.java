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
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.spi.team.Team;

/**
 * Meta-data for a {@link ManagedFunctionContainer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedFunctionLogicMetaData {

	/**
	 * Obtains the name of the {@link FunctionState}.
	 * 
	 * @return Name of the {@link FunctionState}.
	 */
	String getFunctionName();

	/**
	 * Obtains the {@link TeamManagement} responsible for completion of the
	 * {@link FunctionState}.
	 * 
	 * @return {@link TeamManagement} responsible for completion of the
	 *         {@link FunctionState}. May be <code>null</code> to enable any
	 *         {@link Team} to execute the {@link FunctionState}.
	 */
	TeamManagement getResponsibleTeam();

	/**
	 * Obtains the {@link FunctionLoop}.
	 * 
	 * @return {@link FunctionLoop}.
	 */
	FunctionLoop getFunctionLoop();

	/**
	 * Obtains the {@link ManagedFunctionMetaData} of the next
	 * {@link FunctionState}.
	 * 
	 * @return {@link ManagedFunctionMetaData} of next {@link FunctionState}.
	 */
	ManagedFunctionMetaData<?, ?> getNextManagedFunctionMetaData();

	/**
	 * Obtains the {@link EscalationProcedure} for the {@link FunctionState} of
	 * this {@link ManagedFunctionLogicMetaData}.
	 * 
	 * @return {@link EscalationProcedure}.
	 */
	EscalationProcedure getEscalationProcedure();

}