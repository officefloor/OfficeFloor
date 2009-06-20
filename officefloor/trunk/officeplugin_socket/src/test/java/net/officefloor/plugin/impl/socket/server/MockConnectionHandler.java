/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.impl.socket.server;

import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.ConnectionHandler;
import net.officefloor.plugin.socket.server.spi.IdleContext;
import net.officefloor.plugin.socket.server.spi.ReadContext;
import net.officefloor.plugin.socket.server.spi.WriteContext;

/**
 * Mock {@link ConnectionHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockConnectionHandler implements ConnectionHandler {

	/**
	 * {@link ConnectionHandler}.
	 */
	private ConnectionHandler delegate;

	/**
	 * Flag indicating if close.
	 */
	private boolean isClose = false;

	/**
	 * Specifies the delegate {@link ConnectionHandler}.
	 * 
	 * @param delegate
	 *            Delegate {@link ConnectionHandler}.
	 */
	public void setDelegateConnectionHandler(ConnectionHandler delegate) {
		this.delegate = delegate;
	}

	/**
	 * Flags to close {@link Connection}.
	 */
	public void flagClose() {
		this.isClose = true;
	}

	/*
	 * ================== ConnectionHandler ==============================
	 */

	@Override
	public void handleIdleConnection(IdleContext context) {

		// Handle close
		if (this.isClose) {
			context.setCloseConnection(true);
		}

		// Delegate
		if (this.delegate != null) {
			this.delegate.handleIdleConnection(context);
		}
	}

	@Override
	public void handleRead(ReadContext context) {

		// Handle close
		if (this.isClose) {
			context.setCloseConnection(true);
		}

		// Delegate
		if (this.delegate != null) {
			this.delegate.handleRead(context);
		}
	}

	@Override
	public void handleWrite(WriteContext context) {

		// Handle close
		if (this.isClose) {
			context.setCloseConnection(true);
		}

		// Delegate
		if (this.delegate != null) {
			this.delegate.handleWrite(context);
		}
	}

}