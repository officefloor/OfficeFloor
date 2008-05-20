/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
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
 * Test {@link Selector}.
 * 
 * @author Daniel
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
	 * =====================================================================
	 * TestSelector
	 * =====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.Selector#select(long)
	 */
	@Override
	public int select(long timeout) throws IOException {
		// Always the one key
		return 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.Selector#selectedKeys()
	 */
	@Override
	public Set<SelectionKey> selectedKeys() {
		return new HashSet<SelectionKey>(Arrays.asList(this.selectionKey));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.Selector#keys()
	 */
	@Override
	public Set<SelectionKey> keys() {
		return new HashSet<SelectionKey>(Arrays.asList(this.selectionKey));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.Selector#wakeup()
	 */
	@Override
	public Selector wakeup() {
		// Do nothing as mocking
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.Selector#isOpen()
	 */
	@Override
	public boolean isOpen() {
		TestCase.fail("Should not be invoked");
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.Selector#provider()
	 */
	@Override
	public SelectorProvider provider() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.Selector#select()
	 */
	@Override
	public int select() throws IOException {
		TestCase.fail("Should not be invoked");
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.Selector#selectNow()
	 */
	@Override
	public int selectNow() throws IOException {
		TestCase.fail("Should not be invoked");
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.Selector#close()
	 */
	@Override
	public void close() throws IOException {
		TestCase.fail("Should not be invoked");
	}

}
