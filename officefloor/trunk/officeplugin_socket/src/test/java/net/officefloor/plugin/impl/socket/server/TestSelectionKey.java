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

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

import junit.framework.TestCase;

/**
 * Test {@link SelectionKey}.
 * 
 * @author Daniel
 */
public class TestSelectionKey extends SelectionKey {

	/**
	 * Ready and interested ops (ie always ready).
	 */
	private int readyAndInterestOps = SelectionKey.OP_READ;

	/**
	 * Flag indicating if valid and not cancelled.
	 */
	private boolean isValidNotCancelled = true;

	/*
	 * ========================================================================
	 * SelectionKey
	 * ========================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.SelectionKey#isValid()
	 */
	@Override
	public boolean isValid() {
		return this.isValidNotCancelled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.SelectionKey#readyOps()
	 */
	@Override
	public int readyOps() {
		return this.readyAndInterestOps;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.SelectionKey#interestOps()
	 */
	@Override
	public int interestOps() {
		return this.readyAndInterestOps;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.SelectionKey#interestOps(int)
	 */
	@Override
	public SelectionKey interestOps(int ops) {
		this.readyAndInterestOps = ops;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.SelectionKey#cancel()
	 */
	@Override
	public void cancel() {
		this.isValidNotCancelled = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.SelectionKey#selector()
	 */
	@Override
	public Selector selector() {
		TestCase.fail("Should not be invoked");
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.nio.channels.SelectionKey#channel()
	 */
	@Override
	public SelectableChannel channel() {
		TestCase.fail("Should not be invoked");
		return null;
	}

}
