/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;

/**
 * Mock {@link Selector}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockSelector extends Selector {

	/**
	 * {@link SelectionKey}.
	 */
	private SelectionKey selectionKey;

	/**
	 * Flag indicating if {@link Selector} is closed.
	 */
	private boolean isClosed = false;

	/**
	 * Initiate.
	 * 
	 * @param selectionKey
	 *            {@link SelectionKey} to be returned.
	 */
	public MockSelector(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}

	/**
	 * Indicates if closed.
	 * 
	 * @return <code>true</code> if closed.
	 */
	public boolean isClosed() {
		return this.isClosed;
	}

	/**
	 * Ensures that this {@link Selector} is not closed.
	 */
	private void ensureNotClosed() {
		TestCase.assertFalse("Selector is closed", this.isClosed);
	}

	/*
	 * ================= Selector ================================
	 */

	@Override
	public int select(long timeout) throws IOException {
		this.ensureNotClosed();

		// Remove selection key if no longer valid
		if (!(this.selectionKey.isValid())) {
			this.selectionKey = null;
		}

		// Always the one key
		return 1;
	}

	@Override
	public Set<SelectionKey> selectedKeys() {
		this.ensureNotClosed();
		return this.getKeySet();
	}

	@Override
	public Set<SelectionKey> keys() {
		this.ensureNotClosed();
		return this.getKeySet();
	}

	/**
	 * Obtains the {@link SelectionKey} {@link Set}.
	 * 
	 * @return {@link SelectionKey} {@link Set}.
	 */
	private Set<SelectionKey> getKeySet() {
		return new HashSet<SelectionKey>(
				Arrays.asList(this.selectionKey == null ? new SelectionKey[0]
						: new SelectionKey[] { this.selectionKey }));
	}

	@Override
	public Selector wakeup() {
		this.ensureNotClosed();

		// Do nothing as mocking
		return this;
	}

	@Override
	public boolean isOpen() {
		TestCase.fail("Should not be invoked");
		return false;
	}

	@Override
	public SelectorProvider provider() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	@Override
	public int select() throws IOException {
		TestCase.fail("Should not be invoked");
		return -1;
	}

	@Override
	public int selectNow() throws IOException {
		TestCase.fail("Should not be invoked");
		return -1;
	}

	@Override
	public void close() throws IOException {
		this.isClosed = true;
	}

}