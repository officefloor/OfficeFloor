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
package net.officefloor.plugin.impl.socket.server.messagesegment;

import java.nio.ByteBuffer;

import net.officefloor.plugin.impl.socket.server.MessageSegmentPool;
import net.officefloor.plugin.impl.socket.server.PooledMessageSegment;
import net.officefloor.plugin.socket.server.spi.MessageSegment;

/**
 * Abstract {@link MessageSegmentPool} that provides pooling of
 * {@link ByteBuffer} {@link MessageSegment} instances.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractBufferMessageSegmentPool implements
		MessageSegmentPool {

	/**
	 * Size of the {@link ByteBuffer} instances.
	 */
	private final int BUFFER_SIZE;

	/**
	 * Head of the listing of {@link MessageSegment} listing.
	 */
	private AbstractMessageSegment head = null;

	/**
	 * Tail of the listing of {@link MessageSegment} listing.
	 */
	private AbstractMessageSegment tail = null;

	/**
	 * Initiate.
	 * 
	 * @param bufferSize
	 *            Size of the {@link ByteBuffer} instances being pooled.
	 */
	public AbstractBufferMessageSegmentPool(int bufferSize) {
		this.BUFFER_SIZE = bufferSize;
	}

	/**
	 * Creates a new {@link ByteBufferPooledMessageSegment}.
	 * 
	 * @return New {@link ByteBufferPooledMessageSegment}.
	 */
	private ByteBufferPooledMessageSegment createMessageSegment() {
		return new ByteBufferPooledMessageSegment(this
				.createByteBuffer(BUFFER_SIZE));
	}

	/**
	 * Creates a new {@link ByteBuffer} that will be pooled.
	 * 
	 * @param bufferSize
	 *            Size of the {@link ByteBuffer}.
	 * @return New {@link ByteBuffer}.
	 */
	protected abstract ByteBuffer createByteBuffer(int bufferSize);

	/*
	 * ================= MessageSegmentPool ==============================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.plugin.impl.socket.server.MessageSegmentPool#
	 * getMessageSegmentBufferSize()
	 */
	@Override
	public int getMessageSegmentBufferSize() {
		return BUFFER_SIZE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.plugin.impl.socket.server.MessageSegmentPool#
	 * getMessageSegment(java.nio.ByteBuffer)
	 */
	@Override
	public PooledMessageSegment getMessageSegment(ByteBuffer buffer) {
		return new InstanceMessageSegment(buffer);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.plugin.impl.socket.server.MessageSegmentPool#
	 * getMessageSegment()
	 */
	@Override
	public PooledMessageSegment getMessageSegment() {
		// Determine if have pooled segments
		AbstractMessageSegment segment = null;
		synchronized (this) {
			if (this.head != null) {
				// Remove the first segment
				segment = this.head;
				this.head = (ByteBufferPooledMessageSegment) segment
						.getNextSegment();

				// Handle remaining references
				if (this.head != null) {
					// Flag not previous
					this.head.setPreviousSegment(null);
				} else {
					// No head, therefore no tail
					this.tail = null;
				}
			}
		}

		// Determine if sourced a segment
		if (segment != null) {
			// Clean the sourced segment
			segment.setNextSegment(null);
			segment.setPreviousSegment(null);
			segment.getBuffer().clear();

			// Return the sourced segment
			return segment;
		} else {
			// Create a new segment
			return this.createMessageSegment();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seenet.officefloor.plugin.impl.socket.server.MessageSegmentPool#
	 * returnMessageSegments
	 * (net.officefloor.plugin.impl.socket.server.PooledMessageSegment)
	 */
	@Override
	public void returnMessageSegments(PooledMessageSegment pooledSegment) {

		// Ensure have a segment
		if (pooledSegment == null) {
			return;
		}

		// Down case to appropriate type
		AbstractMessageSegment segment = (AbstractMessageSegment) pooledSegment;

		// Return to segments to pool
		synchronized (this) {

			// Handle if pool empty
			if (this.head == null) {
				// Obtain the first segment that is pooled
				while ((segment != null) && (!segment.canPool())) {
					segment = segment.getNextSegment();
				}

				// Determine if have pooled segment
				if (segment != null) {
					// Set as segment as first and only in list
					this.head = segment;
					this.head.setPreviousSegment(null);
					this.tail = this.head;
					this.tail.setNextSegment(null);

					// Move to next segment in input list
					segment = segment.getNextSegment();
				}
			}

			// Load to non-empty pool
			while (segment != null) {
				if (segment.canPool()) {
					// Append the message segment to pool
					segment.setPreviousSegment(this.tail);
					this.tail.setNextSegment(segment);
					this.tail = (ByteBufferPooledMessageSegment) segment;
				}

				// Move to next segment
				segment = segment.getNextSegment();
			}

			// Flag no more segments
			this.tail.setNextSegment(null);
		}
	}

}
