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
package net.officefloor.web.session.spi;

import java.io.Serializable;
import java.util.Map;

import net.officefloor.web.session.HttpSession;

/**
 * Operation to obtain details of creating a new {@link HttpSession} within the
 * {@link HttpSessionStore}.
 *
 * @author Daniel Sagenschneider
 */
public interface CreateHttpSessionOperation {

	/**
	 * Obtains the session Id of the new {@link HttpSession}.
	 *
	 * @return Session Id of the new {@link HttpSession}.
	 */
	String getSessionId();

	/**
	 * Flags that the {@link HttpSession} was successfully created within the
	 * {@link HttpSessionStore}.
	 *
	 * @param creationTime
	 *            Time the {@link HttpSession} was created within the
	 *            {@link HttpSessionStore}.
	 * @param expireTime
	 *            Time to expire the {@link HttpSession} should it be idle.
	 * @param attributes
	 *            {@link Map} to contain the {@link HttpSession} attributes.
	 */
	void sessionCreated(long creationTime, long expireTime,
			Map<String, Serializable> attributes);

	/**
	 * Flags that the session Id is the same as another {@link HttpSession}
	 * within the {@link HttpSessionStore}.
	 */
	void sessionIdCollision();

	/**
	 * Flags that failed to create the {@link HttpSession} within the
	 * {@link HttpSessionStore}.
	 *
	 * @param cause
	 *            Cause of the failure.
	 */
	void failedToCreateSession(Throwable cause);

}