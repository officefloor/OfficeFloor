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
package net.officefloor.plugin.web.http.session;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.spi.managedobject.AsynchronousManagedObject;

/**
 * Administration interface for the {@link HttpSession}.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpSessionAdministration {

	/**
	 * Triggers invalidating the {@link HttpSession}.
	 *
	 * @param isRequireNewSession
	 *            <code>true</code> to have a new {@link HttpSession} created.
	 * @throws Throwable
	 *             If immediate failure in invalidating the {@link HttpSession}.
	 */
	void invalidate(boolean isRequireNewSession) throws Throwable;

	/**
	 * Triggers storing the {@link HttpSession}.
	 *
	 * @throws Throwable
	 *             If immediate failure in storing the {@link HttpSession}.
	 */
	void store() throws Throwable;

	/**
	 * <p>
	 * Indicates if the invalidate or store operation are complete.
	 * <p>
	 * As is an {@link AsynchronousManagedObject}, the next time a new
	 * {@link ManagedFunction} is run the operation should be complete. This method enables
	 * determining if completed immediately and there were no failures of the
	 * operation.
	 *
	 * @return <code>true</code> if the invalidate or store operation is
	 *         complete.
	 * @throws Throwable
	 *             Possible failure in invalidating or storing the
	 *             {@link HttpSession}.
	 */
	boolean isOperationComplete() throws Throwable;

}