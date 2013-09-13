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

import net.officefloor.plugin.bayeux.transport.handshake.HandshakeCallback;
import net.officefloor.plugin.bayeux.transport.handshake.SuccessfulHandshake;
import net.officefloor.plugin.bayeux.transport.handshake.UnsuccessfulHandshake;

import org.junit.Assert;

/**
 * Mock {@link HandshakeCallback}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockHandshakeCallback implements HandshakeCallback {

	/**
	 * {@link SuccessfulHandshake} result.
	 */
	private SuccessfulHandshake successfulResult = null;

	/**
	 * {@link UnsuccessfulHandshake}.
	 */
	private UnsuccessfulHandshake unsuccessfulResult = null;

	/**
	 * Obtains the {@link SuccessfulHandshake}.
	 * 
	 * @return {@link SuccessfulHandshake}.
	 */
	public SuccessfulHandshake getSuccessfulHandshake() {
		Assert.assertNotNull("Should be successful handshake",
				this.successfulResult);
		Assert.assertNull("Should be no unsuccessful callback",
				this.unsuccessfulResult);
		return this.successfulResult;
	}

	/**
	 * Obtains the {@link UnsuccessfulHandshake}.
	 * 
	 * @return {@link UnsuccessfulHandshake}.
	 */
	public UnsuccessfulHandshake getUnsuccessfulHandshake() {
		Assert.assertNotNull("Should be unsuccessful handshake",
				this.unsuccessfulResult);
		Assert.assertNull("Should be no successful callback",
				this.successfulResult);
		return this.unsuccessfulResult;
	}

	/*
	 * =============== HandshakeCallback =======================
	 */

	@Override
	public void successful(SuccessfulHandshake result) {
		// TODO implement HandshakeCallback.successful
		throw new UnsupportedOperationException(
				"TODO implement HandshakeCallback.successful");
	}

	@Override
	public void unsuccessful(UnsuccessfulHandshake result) {
		// TODO implement HandshakeCallback.unsuccessful
		throw new UnsupportedOperationException(
				"TODO implement HandshakeCallback.unsuccessful");
	}

}