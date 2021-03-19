/*-
 * #%L
 * OfficeFrame
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.frame.api.team;

import net.officefloor.frame.api.executive.Executive;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.executive.TeamOversight;
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
	 * Obtains the {@link ProcessIdentifier} for the {@link ProcessState} containing
	 * this {@link Job}.
	 * <p>
	 * This allows the {@link Team} executing the {@link Job} to be aware of the
	 * {@link ProcessState} context in which the {@link Job} is to be executed. This
	 * is particular relevant for {@link TeamOversight} provided by the
	 * {@link Executive}.
	 * <p>
	 * An example use would be embedding {@link OfficeFloor} within an Application
	 * Server and using this {@link ProcessIdentifier} and a
	 * {@link ThreadLocalAwareTeam} to know the invoking {@link Thread} for
	 * interaction with {@link ThreadLocal} instances of the Application Server.
	 * 
	 * @return Identifier for the {@link ProcessState} containing this {@link Job}
	 * 
	 * @see ThreadLocalAwareTeam
	 */
	ProcessIdentifier getProcessIdentifier();

	/**
	 * Enables a {@link Team} to cancel the {@link Job} should it be overloaded with
	 * {@link Job} instances.
	 * 
	 * @param cause Reason by {@link Team} for canceling the {@link Job}.
	 */
	void cancel(Throwable cause);

}
