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
package net.officefloor.plugin.stream.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import net.officefloor.plugin.socket.server.impl.ArrayWriteBuffer;
import net.officefloor.plugin.socket.server.impl.BufferWriteBuffer;
import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
import net.officefloor.plugin.socket.server.protocol.WriteBufferEnum;
import net.officefloor.plugin.stream.ServerOutputStream;
import net.officefloor.plugin.stream.WriteBufferReceiver;
import net.officefloor.plugin.stream.impl.ServerOutputStreamImpl;

/**
 * {@link ServerOutputStream} wrapping an {@link OutputStream}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletServerOutputStream extends ServerOutputStreamImpl {

	/**
	 * Initiate.
	 * 
	 * @param outputStream
	 *            Wrapped {@link OutputStream}.
	 */
	public ServletServerOutputStream(OutputStream outputStream) {
		super(new ServletWriteBufferReceiver(outputStream), 1024);
	}

	/**
	 * Initiate.
	 * 
	 * @param outputStream
	 *            Wrapped {@link OutputStream}.
	 * @param momento
	 *            Momento for the state.
	 */
	public ServletServerOutputStream(OutputStream outputStream,
			Serializable momento) {
		super(new ServletWriteBufferReceiver(outputStream), 1024, momento);
	}

	/**
	 * Servlet {@link WriteBufferReceiver}.
	 */
	private static class ServletWriteBufferReceiver implements
			WriteBufferReceiver {

		/**
		 * {@link OutputStream}.
		 */
		private final OutputStream outputStream;

		/**
		 * Indicates if closed.
		 */
		private volatile boolean isClosed = false;

		/**
		 * Initiate.
		 * 
		 * @param outputStream
		 *            {@link OutputStream}.
		 */
		public ServletWriteBufferReceiver(OutputStream outputStream) {
			this.outputStream = outputStream;
		}

		/*
		 * ====================== WriteBufferReceiver ======================
		 */

		@Override
		public Object getWriteLock() {
			return this.outputStream;
		}

		@Override
		public WriteBuffer createWriteBuffer(byte[] data, int length) {
			return new ArrayWriteBuffer(data, length);
		}

		@Override
		public WriteBuffer createWriteBuffer(ByteBuffer buffer) {
			return new BufferWriteBuffer(buffer);
		}

		@Override
		public void writeData(WriteBuffer[] data) throws IOException {

			// Write the data to the output stream
			for (WriteBuffer buffer : data) {
				WriteBufferEnum type = buffer.getType();
				switch (type) {

				case BYTE_ARRAY:
					// Write the data
					this.outputStream.write(buffer.getData(), 0,
							buffer.length());
					break;

				case BYTE_BUFFER:
					// Write the data
					ByteBuffer byteBuffer = buffer.getDataBuffer();
					byte[] writeData = new byte[byteBuffer.remaining()];
					byteBuffer.get(writeData);
					this.outputStream.write(writeData);
					break;

				default:
					throw new IllegalStateException("Unknown buffer type: "
							+ type);
				}
			}
		}

		@Override
		public void close() throws IOException {
			this.isClosed = true;
			this.outputStream.close();
		}

		@Override
		public boolean isClosed() {
			return this.isClosed;
		}
	}

}