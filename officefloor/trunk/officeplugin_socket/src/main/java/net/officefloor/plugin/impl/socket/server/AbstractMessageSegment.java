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

import net.officefloor.plugin.socket.server.spi.MessageSegment;

/**
 * Abstract implementation of
 * {@link net.officefloor.plugin.socket.server.spi.MessageSegment}.
 * 
 * @author Daniel
 */
abstract class AbstractMessageSegment implements MessageSegment {

	/**
	 * {@link ByteBuffer} for this {@link MessageSegment}.
	 */
	private final ByteBuffer buffer;

	/**
	 * Next {@link MessageSegment}.
	 */
	private AbstractMessageSegment next = null;

	/**
	 * Previous {@link MessageSegment}.
	 */
	private AbstractMessageSegment prev = null;

	/**
	 * Initiate.
	 * 
	 * @param buffer
	 *            {@link ByteBuffer}.
	 */
	AbstractMessageSegment(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	/**
	 * Specifies the next {@link MessageSegment}.
	 * 
	 * @param next
	 *            Next {@link MessageSegment}.
	 */
	void setNextSegment(AbstractMessageSegment next) {
		this.next = next;
	}

	/**
	 * Specifies the previous {@link MessageSegment}.
	 * 
	 * @param prev
	 *            Previous {@link MessageSegment}.
	 */
	void setPrevSegment(AbstractMessageSegment prev) {
		this.prev = prev;
	}

	/**
	 * Indicates if this {@link MessageSegment} can be pooled.
	 * 
	 * @return <code>true</code> if this {@link MessageSegment} can be pooled.
	 */
	abstract boolean canPool();

	/*
	 * ====================================================================
	 * MessageSegment
	 * ====================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.MessageSegment#getBuffer()
	 */
	public ByteBuffer getBuffer() {
		return this.buffer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.MessageSegment#getNextSegment()
	 */
	public AbstractMessageSegment getNextSegment() {
		return this.next;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.MessageSegment#getPrevSegment()
	 */
	public AbstractMessageSegment getPrevSegment() {
		return this.prev;
	}

}
