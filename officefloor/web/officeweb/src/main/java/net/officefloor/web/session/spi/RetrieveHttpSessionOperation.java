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
 * Operation to obtain details of retrieving a {@link HttpSession} from the
 * {@link HttpSessionStore}.
 *
 * @author Daniel Sagenschneider
 */
public interface RetrieveHttpSessionOperation {

	/**
	 * Obtains the session Id of the {@link HttpSession} to retrieve.
	 *
	 * @return Session Id of the {@link HttpSession} to retrieve.
	 */
	String getSessionId();

	/**
	 * Flags that the {@link HttpSession} was successfully retrieved from the
	 * {@link HttpSessionStore}.
	 *
	 * @param creationTime
	 *            Time the {@link HttpSession} was created in the
	 *            {@link HttpSessionStore}.
	 * @param expireTime
	 *            Time to expire the {@link HttpSession} should it be idle.
	 * @param attributes
	 *            Attributes for the retrieved {@link HttpSession}.
	 */
	void sessionRetrieved(long creationTime, long expireTime,
			Map<String, Serializable> attributes);

	/**
	 * <p>
	 * Flags that the {@link HttpSession} is not available in the
	 * {@link HttpSessionStore}.
	 * <p>
	 * Typically this is due to the {@link HttpSession} timing out and being
	 * invalidated.
	 */
	void sessionNotAvailable();

	/**
	 * Flags that failed to retrieve the {@link HttpSession} from the
	 * {@link HttpSessionStore}.
	 *
	 * @param cause
	 *            Cause of the failure.
	 */
	void failedToRetreiveSession(Throwable cause);

}