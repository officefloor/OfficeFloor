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

import net.officefloor.plugin.socket.server.spi.ReadMessage;

/**
 * Implementation of {@link ReadMessage}.
 * 
 * @author Daniel
 */
class ReadMessageImpl extends AbstractMessage<ReadMessageImpl> implements
		ReadMessage {

	/**
	 * Initiate.
	 * 
	 * @param stream
	 *            {@link Stream}.
	 */
	ReadMessageImpl(Stream<ReadMessageImpl> stream) {
		super(stream, null);
	}

	/*
	 * ===================================================================
	 * ReadMessage
	 * ===================================================================
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ReadMessage#getNextReadMessage()
	 */
	@Override
	public ReadMessage getNextReadMessage() {
		return this.next;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.officefloor.plugin.socket.server.spi.ReadMessage#isDataAvailable()
	 */
	@Override
	public boolean isDataAvailable() {

		// Ensure valid state
		this.checkIOState();

		// Ensure a segment available
		if (this.tail == null) {
			// No segments, so no data
			return false;
		}

		// Determine if current message is last
		if (this.tail == this.currentReadSegment) {
			if (this.tail.getBuffer().position() == this.currentReadOffset) {
				// Current segment is last with all data read, therefore no data
				return false;
			}
		}

		// Segments available that have not been fully read
		return true;
	}

}
