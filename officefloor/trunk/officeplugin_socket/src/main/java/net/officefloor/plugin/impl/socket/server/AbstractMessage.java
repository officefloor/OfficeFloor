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

import java.nio.ByteBuffer;

import net.officefloor.plugin.socket.server.spi.Connection;
import net.officefloor.plugin.socket.server.spi.Message;
import net.officefloor.plugin.socket.server.spi.MessageSegment;

/**
 * Abstract {@link net.officefloor.plugin.socket.server.spi.Message}.
 * 
 * @author Daniel
 */
abstract class AbstractMessage implements Message {

	/**
	 * {@link Connection} for this {@link Message}.
	 */
	final ConnectionImpl<?> connection;

	/**
	 * Head of the {@link MessageSegment} list.
	 */
	AbstractMessageSegment head = null;

	/**
	 * Tail of the {@link MessageSegment} list.
	 */
	AbstractMessageSegment tail = null;

	/**
	 * Number of {@link MessageSegment} instances on this {@link Message}.
	 */
	int segmentCount = 0;

	/**
	 * Initiate.
	 * 
	 * @param connection
	 *            {@link Connection} for this {@link Message}.
	 */
	AbstractMessage(ConnectionImpl<?> connection) {
		this.connection = connection;
	}

	/**
	 * Obtains the lock for this {@link Message}.
	 * 
	 * @return Lock for this {@link Message}.
	 */
	Object getMessageLock() {
		// Always lock on the connection
		return this.connection;
	}

	/*
	 * ====================================================================
	 * Message
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Message#getConnection()
	 */
	public ConnectionImpl<?> getConnection() {
		return this.connection;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Message#getSegmentCount()
	 */
	public int getSegmentCount() {
		synchronized (this.getMessageLock()) {
			return this.segmentCount;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Message#getFirstSegment()
	 */
	public MessageSegment getFirstSegment() {
		synchronized (this.getMessageLock()) {
			return this.head;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.Message#getLastSegment()
	 */
	public MessageSegment getLastSegment() {
		synchronized (this.getMessageLock()) {
			return this.tail;
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
	 * @see net.officefloor.plugin.socket.server.spi.WriteMessage#appendSegment()
	 */
	public MessageSegment appendSegment() {
		synchronized (this.getMessageLock()) {
			// Obtain the message segment
			AbstractMessageSegment messageSegment = (AbstractMessageSegment) this.connection
					.getMessageSegmentPool().getMessageSegment();

			// Append to end of linked list
			if (this.head == null) {
				// Empty linked list (first entry)
				this.head = messageSegment;
			} else {
				this.tail.setNextSegment(messageSegment);
				messageSegment.setPrevSegment(this.tail);
			}
			this.tail = messageSegment;

			// Increment the number of segments
			this.segmentCount++;

			// Return the message segment
			return messageSegment;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.WriteMessage#appendSegments(int)
	 */
	public MessageSegment appendSegments(int number) {
		synchronized (this.getMessageLock()) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("TODO implement");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.WriteMessage#appendSegment(java.nio.ByteBuffer)
	 */
	public MessageSegment appendSegment(ByteBuffer byteBuffer) {
		synchronized (this.getMessageLock()) {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException("TODO implement");
		}
	}

}
