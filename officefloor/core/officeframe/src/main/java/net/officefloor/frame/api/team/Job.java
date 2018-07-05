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
package net.officefloor.frame.api.team;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * {@link Job} executed by a {@link Team}.
 * 
 * @author Daniel Sagenschneider
 */
public interface Job extends Runnable {

	/**
	 * <p>
	 * Obtains the identifier for the {@link ProcessState} containing this
	 * {@link Job}.
	 * <p>
	 * This allows the {@link Team} executing the {@link Job} to be aware of the
	 * {@link ProcessState} context in which the {@link Job} is to be executed.
	 * <p>
	 * An example use would be embedding {@link OfficeFloor} within an
	 * Application Server and using this identifier and a
	 * {@link ThreadLocalAwareTeam} to know the invoking {@link Thread} for
	 * interaction with {@link ThreadLocal} instances of the Application Server.
	 * 
	 * @return Identifier for the {@link ProcessState} containing this
	 *         {@link Job}
	 * 
	 * @see ThreadLocalAwareTeam
	 */
	Object getProcessIdentifier();

	/**
	 * Enables a {@link Team} to cancel the {@link Job} should it be overloaded
	 * with {@link Job} instances.
	 * 
	 * @param cause
	 *            Reason by {@link Team} for canceling the {@link Job}.
	 */
	void cancel(Throwable cause);

}