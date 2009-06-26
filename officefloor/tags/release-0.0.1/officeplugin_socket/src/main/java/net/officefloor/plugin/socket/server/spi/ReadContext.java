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
 * Context for handling a read.
 * 
 * @author Daniel Sagenschneider
 */
public interface ReadContext extends ConnectionHandlerContext {

	/**
	 * Obtains the {@link ReadMessage} that just received read data.
	 * 
	 * @return {@link ReadMessage} that is being read from the
	 *         {@link Connection}.
	 */
	ReadMessage getReadMessage();

	/**
	 * <p>
	 * Flags that the read is complete for the {@link ReadMessage}.
	 * <p>
	 * This will subsequently invoke the
	 * {@link Server#processReadMessage(ReadMessage)} with the
	 * {@link ReadMessage}.
	 * 
	 * @param isComplete
	 *            <code>true</code> if the {@link ReadMessage} contains all data
	 *            necessary.
	 */
	void setReadComplete(boolean isComplete);

	/**
	 * Flags to continue reading after completing a read.
	 * 
	 * @param isContinue
	 *            <code>true</code> on setting {@link #setReadComplete(boolean)}
	 *            to <code>true</code> start a new {@link ReadMessage} to
	 *            continue listening on the {@link Connection}.
	 */
	void setContinueReading(boolean isContinue);

}
