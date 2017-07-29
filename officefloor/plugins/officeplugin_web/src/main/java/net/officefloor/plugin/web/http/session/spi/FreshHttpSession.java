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
package net.officefloor.plugin.web.http.session.spi;

import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Newly created {@link HttpSession} requiring a session Id.
 *
 * @author Daniel Sagenschneider
 */
public interface FreshHttpSession {

	/**
	 * <p>
	 * Obtains the {@link ServerHttpConnection} requiring a new
	 * {@link HttpSession}.
	 * <p>
	 * Typically this should not be required to generate session Ids.
	 *
	 * @return {@link ServerHttpConnection} requiring a new {@link HttpSession}.
	 */
	ServerHttpConnection getConnection();

	/**
	 * Specifies the Id generated for the {@link HttpSession}.
	 *
	 * @param sessionId
	 *            Id generated for the {@link HttpSession}.
	 */
	void setSessionId(String sessionId);

	/**
	 * Flags failure in generating the {@link HttpSession} Id.
	 *
	 * @param failure
	 *            Failure in generating the {@link HttpSession} Id.
	 */
	void failedToGenerateSessionId(Throwable failure);

}