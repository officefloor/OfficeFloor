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
package net.officefloor.frame.api.team.source;

import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.internal.structure.ProcessState;

/**
 * <p>
 * Listener of a {@link ProcessState} to allow associating state to the context
 * of the {@link ProcessState}. The {@link Thread} creating the
 * {@link ProcessState} will be the same {@link Thread} invoking the
 * {@link #processCreated(Object)} method.
 * <p>
 * An example use is for embedding {@link OfficeFloor} within an Application
 * Server and associating the {@link Thread} invoking {@link ProcessState} for
 * {@link ThreadLocal} instances of the Application Server.
 * <p>
 * The {@link #processCompleted(Object)} may be invoked by any {@link Thread}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ProcessContextListener {

	/**
	 * <p>
	 * Notifies that a {@link ProcessState} is created.
	 * <p>
	 * This will be invoked by the same {@link Thread} creating the
	 * {@link ProcessState}.
	 * 
	 * @param processIdentifier
	 *            {@link ProcessState} identifier.
	 */
	void processCreated(Object processIdentifier);

	/**
	 * Notifies that the {@link ProcessState} is completed.
	 * 
	 * @param processIdentifier
	 *            {@link ProcessState} identifier.
	 */
	void processCompleted(Object processIdentifier);

}