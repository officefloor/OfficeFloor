/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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

package net.officefloor.plugin.socket.server.impl;

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import junit.framework.TestCase;

/**
 * Mock {@link SelectionKey}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockSelectionKey extends SelectionKey {

	/**
	 * Ready and interested ops (ie always ready).
	 */
	private int readyAndInterestOps = SelectionKey.OP_READ;

	/**
	 * Flag indicating if valid and not cancelled.
	 */
	private boolean isValidNotCancelled = true;

	/*
	 * ==================== SelectionKey ========================
	 */

	@Override
	public boolean isValid() {
		return this.isValidNotCancelled;
	}

	@Override
	public int readyOps() {
		return this.readyAndInterestOps;
	}

	@Override
	public int interestOps() {
		return this.readyAndInterestOps;
	}

	@Override
	public SelectionKey interestOps(int ops) {
		this.readyAndInterestOps = ops;
		return this;
	}

	@Override
	public void cancel() {
		this.isValidNotCancelled = false;
	}

	@Override
	public Selector selector() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public SelectableChannel channel() {
		TestCase.fail("Should not be invoked");
		return null;
	}

}