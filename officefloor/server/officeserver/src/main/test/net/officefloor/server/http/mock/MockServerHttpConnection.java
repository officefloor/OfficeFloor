/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.http.mock;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * Mock {@link ServerHttpConnection}.
 * 
 * @author Daniel Sagenschneider
 */
public interface MockServerHttpConnection extends ServerHttpConnection {

	/**
	 * Sends the {@link HttpResponse}.
	 * 
	 * @param escalation
	 *            Optional {@link Escalation}. Should be <code>null</code> for
	 *            successful processing.
	 * @return {@link MockHttpResponse}.
	 */
	MockHttpResponse send(Throwable escalation);

}