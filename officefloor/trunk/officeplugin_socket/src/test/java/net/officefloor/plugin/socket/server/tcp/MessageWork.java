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
package net.officefloor.plugin.socket.server.tcp;

import java.io.OutputStream;

import net.officefloor.frame.test.ReflectiveFlow;
import net.officefloor.plugin.socket.server.tcp.api.ServerTcpConnection;

/**
 * Provides for processing a {@link Messages} response.
 * 
 * @author Daniel
 */
public class MessageWork {

	/**
	 * Obtains the input index of the message and responds with the message.
	 * 
	 * @param connection
	 *            {@link ServerTcpConnection}.
	 * @param flow
	 *            {@link ReflectiveFlow} to invoke to continue processing.
	 */
	public void service(ServerTcpConnection connection, ReflectiveFlow flow)
			throws Throwable {
		try {

			// Obtain the index
			byte[] buffer = new byte[1];
			int readSize = connection.read(buffer);
			if (readSize == 0) {

				// No message, wait for one to come
				connection.waitOnClientData();
				flow.doFlow(null);
				return;
			}
			int index = (int) buffer[0];

			// Handle message
			switch (index) {
			case -1:
				// Close connection (do not process further messages)
				connection.close();
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
				OutputStream outputStream = connection.getOutputStream();
				outputStream.write(data);
				outputStream.flush();

				// Invoke flow to process another message when arrives
				connection.waitOnClientData();
				flow.doFlow(null);

				break;
			}

		} catch (Throwable ex) {
			// Indicate failure and close connection
			ex.printStackTrace();
			connection.close();

			// Propagate
			throw ex;
		}
	}

}