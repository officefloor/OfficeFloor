/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.socket.server.tcp.source;

import java.io.InputStream;

import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.socket.server.tcp.ServerTcpConnection;

/**
 * Provides for processing a {@link Messages} response.
 * 
 * @author Daniel Sagenschneider
 */
public class MessageWork {

	/**
	 * Start time of creating this {@link Work}.
	 */
	private final long startTime = System.currentTimeMillis();

	/**
	 * Obtains the input index of the message and responds with the message.
	 * 
	 * @param connection
	 *            {@link ServerTcpConnection}.
	 * @param taskContext
	 *            {@link TaskContext}.
	 */
	public void service(ServerTcpConnection connection,
			TaskContext<?, ?, ?> taskContext) throws Throwable {
		try {

			// Ensure not waiting too long
			long waitTimeInSeconds = (System.currentTimeMillis() - this.startTime) / 1000;
			if ((waitTimeInSeconds) > 20) {
				throw new Exception("Waited too long for a message ("
						+ waitTimeInSeconds + " seconds)");
			}

			// Obtain the index
			InputStream inputStream = connection.getInputStream();
			switch ((int) inputStream.available()) {
			case -1:
				// Connection prematurely closed
				throw new Exception("Connection prematurely closed");

			case 0:
				// No message, wait for one to come
				connection.waitOnClientData();
				taskContext.setComplete(false);
				return;
			}

			// Obtain the index
			int index = inputStream.read();

			// Handle message
			switch (index) {
			case -1:
				// Close connection (do not process further messages)
				connection.getOutputStream().close();
				break;

			default:
				// Obtain the message
				String message = Messages.getMessage(index);

				// Write the bytes followed by 0 terminator
				byte[] messageBytes = message.getBytes();
				byte[] data = new byte[messageBytes.length + 1];
				for (int i = 0; i < messageBytes.length; i++) {
					data[i] = messageBytes[i];
				}
				data[data.length - 1] = 0;

				// Write the response
				connection.getOutputStream().write(data);

				// Invoke flow to process another message when arrives
				connection.waitOnClientData();
				taskContext.setComplete(false);

				break;
			}

		} catch (Throwable ex) {
			// Indicate failure and close connection
			ex.printStackTrace();
			connection.getOutputStream().close();

			// Propagate
			throw ex;
		}
	}

}