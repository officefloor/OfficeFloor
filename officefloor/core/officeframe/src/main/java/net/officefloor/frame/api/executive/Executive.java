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
package net.officefloor.frame.api.executive;

import net.officefloor.frame.internal.structure.Execution;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * Executive.
 * 
 * @author Daniel Sagenschneider
 */
public interface Executive {

	/**
	 * Creates a new {@link ProcessState} identifier.
	 * 
	 * @return New {@link ProcessState} identifier.
	 */
	default Object createProcessIdentifier() {
		return new Object();
	}

	/**
	 * <p>
	 * Manages the {@link Execution}.
	 * <p>
	 * The {@link Thread#currentThread()} will provide the inbound {@link Thread}.
	 * 
	 * @param           <T> Type of {@link Throwable} thrown.
	 * @param execution {@link Execution} to be undertaken.
	 * @throws T Propagation of failure from {@link Execution}.
	 */
	default <T extends Throwable> void manageExecution(Execution<T> execution) throws T {
		execution.execute();
	}

	/**
	 * Obtains the {@link ExecutionStrategy} strategies.
	 * 
	 * @return {@link ExecutionStrategy} instances.
	 */
	ExecutionStrategy[] getExcutionStrategies();

	/**
	 * Obtains the {@link TeamOversight} instances.
	 * 
	 * @return {@link TeamOversight} instances.
	 */
	default TeamOversight[] getTeamOversights() {
		return new TeamOversight[0]; // no oversight by default
	}

}