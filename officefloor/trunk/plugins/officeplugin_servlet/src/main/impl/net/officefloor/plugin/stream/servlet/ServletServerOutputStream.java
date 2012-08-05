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

package net.officefloor.plugin.stream.servlet;

import java.io.OutputStream;
import java.nio.ByteBuffer;

import net.officefloor.plugin.socket.server.protocol.WriteBuffer;
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
	 * Servlet {@link WriteBufferReceiver}.
	 */
	private static class ServletWriteBufferReceiver implements
			WriteBufferReceiver {

		/**
		 * {@link OutputStream}.
		 */
		private final OutputStream outputStream;

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
		public Object getLock() {
			// TODO implement WriteBufferReceiver.getLock
			throw new UnsupportedOperationException(
					"TODO implement WriteBufferReceiver.getLock");
		}

		@Override
		public WriteBuffer createWriteBuffer(byte[] data, int length) {
			// TODO implement WriteBufferReceiver.createWriteBuffer
			throw new UnsupportedOperationException(
					"TODO implement WriteBufferReceiver.createWriteBuffer");
		}

		@Override
		public WriteBuffer createWriteBuffer(ByteBuffer buffer) {
			// TODO implement WriteBufferReceiver.createWriteBuffer
			throw new UnsupportedOperationException(
					"TODO implement WriteBufferReceiver.createWriteBuffer");
		}

		@Override
		public void writeData(WriteBuffer[] data) {
			// TODO implement WriteBufferReceiver.writeData
			throw new UnsupportedOperationException(
					"TODO implement WriteBufferReceiver.writeData");
		}

		@Override
		public void close() {
			// TODO implement WriteBufferReceiver.close
			throw new UnsupportedOperationException(
					"TODO implement WriteBufferReceiver.close");
		}

		@Override
		public boolean isClosed() {
			// TODO implement WriteBufferReceiver.isClosed
			throw new UnsupportedOperationException(
					"TODO implement WriteBufferReceiver.isClosed");
		}
	}

}