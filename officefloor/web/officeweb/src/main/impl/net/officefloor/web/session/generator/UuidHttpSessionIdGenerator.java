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
package net.officefloor.web.session.generator;

import java.util.UUID;

import net.officefloor.web.session.HttpSession;
import net.officefloor.web.session.spi.FreshHttpSession;
import net.officefloor.web.session.spi.HttpSessionIdGenerator;

/**
 * {@link HttpSessionIdGenerator} that uses {@link UUID#randomUUID()} to
 * generate a {@link HttpSession} Id.
 *
 * @author Daniel Sagenschneider
 */
public class UuidHttpSessionIdGenerator implements HttpSessionIdGenerator {

	/*
	 * ================== HttpSessionIdGenerator =======================
	 */

	@Override
	public void generateSessionId(FreshHttpSession session) {

		// Generate the random UUID to obtain the Session Id
		UUID uuid = UUID.randomUUID();
		String sessionId = uuid.toString();

		// Load the Session Id
		session.setSessionId(sessionId);
	}

}