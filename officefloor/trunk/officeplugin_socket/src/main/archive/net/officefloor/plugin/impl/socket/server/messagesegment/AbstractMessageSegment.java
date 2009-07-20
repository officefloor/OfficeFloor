/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.impl.socket.server.messagesegment;

import java.nio.ByteBuffer;

import net.officefloor.plugin.impl.socket.server.PooledMessageSegment;
import net.officefloor.plugin.socket.server.spi.MessageSegment;

/**
 * Abstract implementation of {@link MessageSegment}.
 * 
 * @author Daniel Sagenschneider
 */
abstract class AbstractMessageSegment implements PooledMessageSegment {

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
	 * Indicates if this {@link MessageSegment} can be pooled.
	 * 
	 * @return <code>true</code> if this {@link MessageSegment} can be pooled.
	 */
	abstract boolean canPool();

	/*
	 * ====================================================================
	 * PooledMessageSegment
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.impl.socket.server.PooledMessageSegment#setNextSegment(net.officefloor.plugin.impl.socket.server.PooledMessageSegment)
	 */
	@Override
	public void setNextSegment(PooledMessageSegment next) {
		this.next = (AbstractMessageSegment) next;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.impl.socket.server.PooledMessageSegment#setPreviousSegment(net.officefloor.plugin.impl.socket.server.PooledMessageSegment)
	 */
	@Override
	public void setPreviousSegment(PooledMessageSegment previous) {
		this.prev = (AbstractMessageSegment) previous;
	}

}
