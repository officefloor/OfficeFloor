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

import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.WriteMessage;
import net.officefloor.plugin.socket.server.spi.WriteMessageListener;

/**
 * Implementation of
 * {@link net.officefloor.plugin.socket.server.spi.WriteMessage}.
 * 
 * @author Daniel
 */
class WriteMessageImpl extends AbstractMessage implements WriteMessage {

	/**
	 * {@link WriteMessageListener}.
	 */
	private final WriteMessageListener listener;

	/**
	 * Flag indicating if this {@link WriteMessage} is filled.
	 */
	private boolean isFilled = false;

	/**
	 * Flag indicating if this {@link WriteMessage} is written to the client.
	 */
	private boolean isWritten = false;

	/**
	 * Initiate.
	 * 
	 * @param connection
	 *            {@link Connection}.
	 * @param listener
	 *            {@link WriteMessageListener} for this {@link WriteMessage}.
	 */
	public WriteMessageImpl(ConnectionImpl<?> connection,
			WriteMessageListener listener) {
		super(connection);
		this.listener = listener;
	}

	/**
	 * Obtains if this {@link WriteMessage} is filled.
	 * 
	 * @return <code>true</code> if this {@link WriteMessage} is filled and
	 *         can be sent in entirity.
	 */
	boolean isFilled() {
		synchronized (this.getMessageLock()) {
			return this.isFilled;
		}
	}

	/**
	 * Flags that this {@link WriteMessage} has been written to the client.
	 */
	public void written() {
		synchronized (this.getMessageLock()) {
			// Return the message segments to pool
			if (this.head != null) {
				this.connection.getMessageSegmentPool().returnMessageSegment(
						this.head);

				// Unlink message segments
				this.head = null;
				this.tail = null;
			}

			// Remove this write message from being written
			this.connection.removeWriteMessage(this);

			// Flag written
			this.isWritten = true;

			// Notify message written
			if (this.listener != null) {
				this.listener.messageWritten(this);
			}
		}
	}

	/*
	 * ====================================================================
	 * WriteMessage
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.WriteMessage#send()
	 */
	public void write() {
		synchronized (this.getMessageLock()) {
			// Flag the message is filled
			this.isFilled = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.WriteMessage#isWritten()
	 */
	public boolean isWritten() {
		synchronized (this.getMessageLock()) {
			return this.isWritten;
		}
	}

}
