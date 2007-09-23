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
 * Pool of {@link net.officefloor.plugin.socket.server.spi.MessageSegment}
 * instances.
 * 
 * @author Daniel
 */
class MessageSegmentPool {

	/**
	 * Head of the listing of {@link MessageSegment} listing.
	 */
	private PooledMessageSegment head = null;

	/**
	 * Tail of the listing of {@link MessageSegment} listing.
	 */
	private PooledMessageSegment tail = null;

	/**
	 * Obtains a {@link MessageSegment} from this {@link MessageSegmentPool}.
	 * 
	 * @return {@link MessageSegment} from this {@link MessageSegmentPool}.
	 */
	MessageSegment getMessageSegment() {
		// Determine if have pooled segments
		PooledMessageSegment segment = null;
		synchronized (this) {
			if (this.head != null) {
				// Remove the first segment
				segment = this.head;
				this.head = (PooledMessageSegment) segment.getNextSegment();

				// Handle remaining references
				if (this.head != null) {
					// Flag not previous
					this.head.setPrevSegment(null);
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
			segment.setPrevSegment(null);
			segment.getBuffer().clear();

			// Return the sourced segment
			return segment;
		} else {
			// Create a new segment
			return this.createMessageSegment();
		}
	}

	/**
	 * Obtains a listing of {@link MessageSegment} instances.
	 * 
	 * @param number
	 *            Number of {@link MessageSegment} instances.
	 * @return Starting {@link MessageSegment} of the listing.
	 */
	synchronized MessageSegment getMessageSegments(int number) {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	/**
	 * <p>
	 * Returns a {@link MessageSegment} to this {@link MessageSegmentPool}.
	 * <p>
	 * Should the {@link MessageSegment} have next {@link MessageSegment}
	 * instances, they are also returned to this {@link MessageSegmentPool}.
	 * This aids returning
	 * {@link net.officefloor.plugin.socket.server.spi.Message} instances in
	 * bulk.
	 * 
	 * @param segment
	 *            Start {@link MessageSegment} listing to be returned to this
	 *            {@link MessageSegmentPool}.
	 */
	void returnMessageSegment(AbstractMessageSegment segment) {

		// Ensure have a segment
		if (segment == null) {
			return;
		}

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
					// Set as segment as first in list
					this.head = (PooledMessageSegment) segment;

					// Move to next segment in input list
					segment = segment.getNextSegment();

					// Initiate remaining list state
					this.head.setPrevSegment(null);
					this.tail = this.head;
					this.tail.setNextSegment(null);
				}
			}

			// Load to non-empty pool
			while (segment != null) {
				if (segment.canPool()) {
					// Append the message segment to pool
					segment.setPrevSegment(this.tail);
					this.tail.setNextSegment(segment);
					this.tail = (PooledMessageSegment) segment;
				}
			}

			// Flag no more segments
			this.tail.setNextSegment(null);
		}
	}

	/**
	 * Creates a new {@link PooledMessageSegment}.
	 * 
	 * @return New {@link PooledMessageSegment}.
	 */
	private PooledMessageSegment createMessageSegment() {
		// TODO provide ability to specify the buffer sizes
		return new PooledMessageSegment(ByteBuffer.allocateDirect(1024));
	}

}
