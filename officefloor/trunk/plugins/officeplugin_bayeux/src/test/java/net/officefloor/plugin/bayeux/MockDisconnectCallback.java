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
package net.officefloor.plugin.bayeux;

import org.junit.Assert;

import net.officefloor.plugin.bayeux.transport.disconnect.Disconnect;
import net.officefloor.plugin.bayeux.transport.disconnect.DisconnectCallback;

/**
 * Mock {@link DisconnectCallback}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockDisconnectCallback implements DisconnectCallback {

	/**
	 * {@link Disconnect}.
	 */
	private Disconnect disconnect = null;

	/**
	 * Obtains the {@link Disconnect}.
	 * 
	 * @return {@link Disconnect}.
	 */
	public Disconnect getDisconnect() {
		Assert.assertNotNull("Should have disconnect", this.disconnect);
		return this.disconnect;
	}

	/*
	 * =================== DisconnectCallback ======================
	 */

	@Override
	public void disconnect(Disconnect result) {
		// TODO implement DisconnectCallback.disconnect
		throw new UnsupportedOperationException(
				"TODO implement DisconnectCallback.disconnect");
	}

}