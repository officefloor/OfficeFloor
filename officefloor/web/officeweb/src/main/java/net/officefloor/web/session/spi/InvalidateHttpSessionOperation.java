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
package net.officefloor.web.session.spi;

import net.officefloor.web.session.HttpSession;

/**
 * Operation to obtain details of invalidating a {@link HttpSession} within the
 * {@link HttpSessionStore}.
 *
 * @author Daniel Sagenschneider
 */
public interface InvalidateHttpSessionOperation {

	/**
	 * Obtains the session Id of the {@link HttpSession} to invalidate.
	 *
	 * @return Session Id of the {@link HttpSession} to invalidate.
	 */
	String getSessionId();

	/**
	 * Flags the {@link HttpSession} was invalidated successfully within the
	 * {@link HttpSessionStore}.
	 */
	void sessionInvalidated();

	/**
	 * Flags failed to invalidate the {@link HttpSession} within the
	 * {@link HttpSessionStore}.
	 *
	 * @param cause
	 *            Cause of the failure.
	 */
	void failedToInvalidateSession(Throwable cause);

}