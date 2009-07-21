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
package net.officefloor.plugin.socket.server;

import java.io.InputStream;

/**
 * Context for handling a read.
 *
 * @author Daniel Sagenschneider
 */
public interface ReadContext extends ConnectionHandlerContext {

	/**
	 * <p>
	 * Obtains the {@link InputStream} to obtain the read data from the client.
	 * <p>
	 * The end of stream is reached when no further data is immediately
	 * available from the client. If further data is required for the
	 * {@link Request} do not flag the {@link Request} as received and the
	 * {@link ConnectionHandler} will be invoked again when further data is
	 * available from the client.
	 *
	 * @return {@link InputStream}.
	 */
	InputStream getInputStream();

	/**
	 * <p>
	 * Flags that the {@link Request} has been received.
	 * <p>
	 * This will subsequently have the {@link Server} process the
	 * {@link Request}.
	 *
	 * @param requestSize
	 *            Specifies the size of the {@link Request} in bytes.
	 * @param attachment
	 *            Optional attachment for the {@link Request}. May be
	 *            <code>null</code>.
	 */
	void requestReceived(long requestSize, Object attachment);

	/**
	 * <p>
	 * Flags to stop reading after receiving a {@link Request}.
	 * <p>
	 * By default it will be <code>false</code> to continue reading data from
	 * the client.
	 *
	 * @param isContinue
	 *            <code>false</code> to continue reading for another
	 *            {@link Request}. <code>true</code> will ignore further data
	 *            read from client and close the {@link Connection} when
	 *            response written.
	 */
	void setStopReading(boolean isStop);

}