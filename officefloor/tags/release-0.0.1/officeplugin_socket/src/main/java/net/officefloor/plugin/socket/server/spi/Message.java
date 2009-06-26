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

/**
 * <p>
 * Represents a message to be communicated.
 * <p>
 * <b>All changes to a {@link Message} or a {@link MessageSegment} must be done
 * within a <code>synchronized</code> block on lock
 * {@link Connection#getLock()}.</b>
 * <p>
 * Provided by the Server Socket plugin.
 * 
 * @author Daniel Sagenschneider
 */
public interface Message {

	/**
	 * Obtains the {@link Connection} this {@link Message} is in.
	 * 
	 * @return {@link Connection} this {@link Message} is in.
	 */
	Connection getConnection();

	/**
	 * Obtains the number of {@link MessageSegment} instances on this
	 * {@link Message}.
	 * 
	 * @return Number of {@link MessageSegment} instances on this
	 *         {@link Message}.
	 */
	int getSegmentCount();

	/**
	 * Obtains the first {@link MessageSegment} of this {@link Message}.
	 * 
	 * @return First {@link MessageSegment} of this {@link Message}.
	 */
	MessageSegment getFirstSegment();

	/**
	 * Obtains the last {@link MessageSegment} of this {@link Message}.
	 * 
	 * @return last {@link MessageSegment} of this {@link Message}.
	 */
	MessageSegment getLastSegment();

}
