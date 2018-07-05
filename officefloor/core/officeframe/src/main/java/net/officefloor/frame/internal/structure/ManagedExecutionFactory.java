/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.internal.structure;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Factory for the {@link ManagedExecution}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedExecutionFactory {

	/**
	 * Creates the {@link ManagedExecution}.
	 * 
	 * @param <E>
	 *            Possible {@link Escalation} from {@link Execution}.
	 * @param execution
	 *            {@link Execution}.
	 * @return {@link ManagedExecution}.
	 */
	<E extends Throwable> ManagedExecution<E> createManagedExecution(Execution<E> execution);

}