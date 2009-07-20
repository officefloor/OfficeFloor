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
package net.officefloor.plugin.socket.server.spi;

import java.nio.ByteBuffer;

/**
 * <p>
 * Segment of a {@link Message}.
 * <p>
 * Provided by the Server Socket plug-in.
 * 
 * @author Daniel Sagenschneider
 */
public interface MessageSegment {

	/**
	 * Obtains the {@link ByteBuffer} of this segment.
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
