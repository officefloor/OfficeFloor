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
package net.officefloor.plugin.bayeux.transport.handshake;

import org.cometd.bayeux.server.BayeuxServer;

/**
 * Callback on result of handshake with {@link BayeuxServer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HandshakeCallback {

	/**
	 * Notifies the handshake was successful.
	 * 
	 * @param result
	 *            Details of the successful handshake.
	 */
	void successful(SuccessfulHandshake result);

	/**
	 * Notifies the handshake was unsuccessful.
	 * 
	 * @param result
	 *            Details of the unsuccessful handshake.
	 */
	void unsuccessful(UnsuccessfulHandshake result);

}