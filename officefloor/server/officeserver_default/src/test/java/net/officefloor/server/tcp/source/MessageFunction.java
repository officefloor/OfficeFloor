/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.server.tcp.source;

import java.io.InputStream;
import java.io.OutputStream;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.server.tcp.ServerTcpConnection;

/**
 * Provides for processing a {@link Messages} response.
 * 
 * @author Daniel Sagenschneider
 */
public class MessageFunction {

	/**
	 * Start time of creating this {@link ManagedFunction}.
	 */
	private final long startTime = System.currentTimeMillis();

	/**
	 * Obtains the input index of the message and responds with the message.
	 * 
	 * @param connection
	 *            {@link ServerTcpConnection}.
	 */
	public void service(ServerTcpConnection connection) throws Throwable {
		try {

			// Ensure not waiting too long
			long waitTimeInSeconds = (System.currentTimeMillis() - this.startTime) / 1000;
			if ((waitTimeInSeconds) > 20) {
				throw new Exception("Waited too long for a message (" + waitTimeInSeconds + " seconds)");
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
				System.arraycopy(messageBytes, 0, data, 0, messageBytes.length);
				data[data.length - 1] = 0;

				// Write the response
				OutputStream output = connection.getOutputStream();
				output.write(data);
				output.flush();

				// Invoke flow to process another message when arrives
				connection.waitOnClientData();
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