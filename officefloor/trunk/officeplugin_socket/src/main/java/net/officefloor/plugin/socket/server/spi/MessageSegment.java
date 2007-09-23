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
package net.officefloor.plugin.socket.server.spi;

import java.nio.ByteBuffer;

/**
 * <p>
 * Segment of a {@link net.officefloor.plugin.socket.server.spi.Message}.
 * <p>
 * Provided by the Server Socket plugin.
 * 
 * @author Daniel
 */
public interface MessageSegment {

	/**
	 * Obtains the {@link java.nio.ByteBuffer} of this segment.
	 * 
	 * @return {@link ByteBuffer} of this segment.
	 */
	ByteBuffer getBuffer();

	/**
	 * Obtains the next {@link MessageSegment} {@link MessageSegment} in the
	 * listing of {@link MessageSegment} instances for the {@link Message}.
	 * 
	 * @return Next {@link MessageSegment} or <code>null</code> if no further
	 *         {@link MessageSegment} instances for the {@link Message}.
	 */
	MessageSegment getNextSegment();

	/**
	 * Obtains the previous {@link MessageSegment} in the listing of
	 * {@link MessageSegment} instances for the {@link Message}.
	 * 
	 * @return Previous {@link MessageSegment} or <code>null</code> if no
	 *         further {@link MessageSegment} instances for the {@link Message}.
	 */
	MessageSegment getPrevSegment();
}
