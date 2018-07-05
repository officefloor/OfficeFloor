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
 * <p>
 * Enables a {@link Team} to execute the {@link Job} with the {@link Thread}
 * invoking the {@link ProcessState}.
 * <p>
 * An example use is for embedding {@link OfficeFloor} within an Application
 * Server and associating the {@link Thread} invoking the {@link ProcessState}
 * for {@link ThreadLocal} instances of the Application Server.
 * 
 * @author Daniel Sagenschneider
 */
public interface ThreadLocalAwareTeam extends Team {

	/**
	 * Sets the {@link ThreadLocalAwareContext} for the {@link Team}.
	 * 
	 * @param context
	 *            {@link ThreadLocalAwareContext} for the {@link Team}.
	 */
	void setThreadLocalAwareness(ThreadLocalAwareContext context);

}