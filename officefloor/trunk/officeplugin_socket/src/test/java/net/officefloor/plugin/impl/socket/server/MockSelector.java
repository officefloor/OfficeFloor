/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.impl.socket.server;

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
	private final SelectionKey selectionKey;

	/**
	 * Initiate.
	 * 
	 * @param selectionKey
	 *            {@link SelectionKey} to be returned.
	 */
	public MockSelector(SelectionKey selectionKey) {
		this.selectionKey = selectionKey;
	}

	/*
	 * ================= Selector ================================
	 */

	@Override
	public int select(long timeout) throws IOException {
		// Always the one key
		return 1;
	}

	@Override
	public Set<SelectionKey> selectedKeys() {
		return new HashSet<SelectionKey>(Arrays.asList(this.selectionKey));
	}

	@Override
	public Set<SelectionKey> keys() {
		return new HashSet<SelectionKey>(Arrays.asList(this.selectionKey));
	}

	@Override
	public Selector wakeup() {
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
		TestCase.fail("Should not be invoked");
	}

}