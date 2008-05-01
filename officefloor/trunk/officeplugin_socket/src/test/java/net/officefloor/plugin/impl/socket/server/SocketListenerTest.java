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

/**
 * Tests the {@link SocketListener}.
 * 
 * @author Daniel
 */
public class SocketListenerTest extends AbstractWriteRead {

	/**
	 * Validates read.
	 */
	public void testRead() throws Exception {
		final String TEXT = "test message";
		this.inputFromClient(TEXT);
		this.runSocketListener();
		this.validateConnectionRead(TEXT);
	}

	/**
	 * Validates writing.
	 */
	public void testWrite() throws Exception {
		final String TEXT = "test message";
		this.connectionWrite(TEXT);
		this.connectionFlush();
		this.runSocketListener(); // specifies writing
		this.runSocketListener(); // does the writing
		this.validateOutputToClient(TEXT);
	}

	/**
	 * Validates closes.
	 */
	public void testClose() throws Exception {
		this.flagCloseConnection();
		this.runSocketListener();
		assertFalse("Key should be cancelled", this.selectionKey.isValid());
		assertTrue("Channel should be closed", this.socketChannel.isClosed());
	}

	/**
	 * Validates request response.
	 */
	public void testRequestResponse() throws Exception {
		final String REQUEST = "request";
		final String RESPONSE = "response";
		this.inputFromClient(REQUEST);
		this.runSocketListener();
		this.validateConnectionRead(REQUEST);
		this.connectionWrite(RESPONSE);
		this.flagCloseConnection();
		this.runSocketListener(); // specifies writing
		this.runSocketListener(); // does the writing
		assertFalse("Key should be cancelled", this.selectionKey.isValid());
		assertTrue("Channel should be closed", this.socketChannel.isClosed());
	}

}
