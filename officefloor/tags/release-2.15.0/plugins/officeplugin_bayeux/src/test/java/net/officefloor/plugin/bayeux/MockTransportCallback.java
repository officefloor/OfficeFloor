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

import net.officefloor.plugin.bayeux.transport.TransportCallback;
import net.officefloor.plugin.bayeux.transport.TransportMessage;
import net.officefloor.plugin.bayeux.transport.TransportResult;

import org.junit.Assert;

/**
 * Mock {@link TransportCallback}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockTransportCallback<R extends TransportResult> implements
		TransportCallback<R> {

	/**
	 * Successful result.
	 */
	private R successfulResult = null;

	/**
	 * Unsuccessful result.
	 */
	private R unsuccessfulResult = null;

	/**
	 * Obtains the successful result.
	 * 
	 * @return Successful result.
	 */
	public R getSuccessfulResult() {
		Assert.assertNotNull("Should be successful", this.successfulResult);
		Assert.assertNull("Should be no unsuccessful callback",
				this.unsuccessfulResult);
		return this.successfulResult;
	}

	/**
	 * Obtains the successful {@link TransportMessage}.
	 * 
	 * @return Successful {@link TransportMessage}.
	 */
	public TransportMessage getSuccessfulMessage() {
		R result = this.getSuccessfulResult();
		TransportMessage[] messages = result.getResponse();
		Assert.assertEquals("Should be only 1 successful message", 1,
				messages.length);
		return messages[0];
	}

	/**
	 * Obtains the unsuccessful result.
	 * 
	 * @return Unsuccessful result.
	 */
	public R getUnsuccessfulResult() {
		Assert.assertNotNull("Should be unsuccessful", this.unsuccessfulResult);
		Assert.assertNull("Should be no successful callback",
				this.successfulResult);
		return this.unsuccessfulResult;
	}

	/**
	 * Obtains the unsuccessful {@link TransportMessage}.
	 * 
	 * @return Unsuccessful {@link TransportMessage}.
	 */
	public TransportMessage getUnsuccessfulMessage() {
		R result = this.getUnsuccessfulResult();
		TransportMessage[] messages = result.getResponse();
		Assert.assertEquals("Should be only 1 unsuccessful message", 1,
				messages.length);
		return messages[0];
	}

	/*
	 * =============== TransportCallback =======================
	 */

	@Override
	public void successful(R result) {
		this.successfulResult = result;
	}

	@Override
	public void unsuccessful(R result) {
		// TODO implement HandshakeCallback.unsuccessful
		throw new UnsupportedOperationException(
				"TODO implement HandshakeCallback.unsuccessful");
	}

}