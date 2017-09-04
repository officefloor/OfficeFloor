/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import net.officefloor.server.BufferManagementSocketManagerTest;
import net.officefloor.server.RequestServicerFactory;
import net.officefloor.server.SocketManager;
import net.officefloor.server.SocketServicerFactory;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * Tests {@link SSLSocket} communication with {@link SocketManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class SslSocketManagerTest extends BufferManagementSocketManagerTest {

	private SslSocketServicerFactory<?> prevousSslSocketServicerFactory;

	@Override
	protected <R> SocketServicerFactory<R> adaptSocketServicerFactory(SocketServicerFactory<R> socketServicerFactory,
			RequestServicerFactory<R> requestServicerFactory, StreamBufferPool<ByteBuffer> bufferPool) {
		try {
			// Obtain the SSL context
			SSLContext sslContext = OfficeFloorDefaultSslContextSource.createServerSslContext(null);

			// Create the executor
			Executor executor = (task) -> new TestThread(task).start();

			// Create the SSL socket servicer
			SslSocketServicerFactory<R> sslSocketServicerFactory = new SslSocketServicerFactory<>(sslContext,
					socketServicerFactory, requestServicerFactory, bufferPool, executor);

			// Capture for adapting the request servicer factory
			this.prevousSslSocketServicerFactory = sslSocketServicerFactory;

			// Return the SSL socket servicer
			return sslSocketServicerFactory;

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	protected <R> RequestServicerFactory<R> adaptRequestServicerFactory(
			RequestServicerFactory<R> requestServicerFactory) {
		return (RequestServicerFactory<R>) this.prevousSslSocketServicerFactory;
	}

	@Override
	protected Socket createClient(int port) throws IOException {
		try {
			return OfficeFloorDefaultSslContextSource.createClientSslContext(null).getSocketFactory()
					.createSocket(InetAddress.getLocalHost(), port);
		} catch (Exception ex) {
			throw fail(ex);
		}
	}

}