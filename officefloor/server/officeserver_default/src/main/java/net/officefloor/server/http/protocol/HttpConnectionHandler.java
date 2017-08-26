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
package net.officefloor.server.http.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import net.officefloor.server.ConnectionHandler;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.ProcessAwareServerHttpConnectionManagedObject;
import net.officefloor.server.http.parse.HttpRequestParser;
import net.officefloor.server.http.parse.HttpRequestParser.HttpRequestParserMetaData;
import net.officefloor.server.stream.BufferPool;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * HTTP {@link ConnectionHandler}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpConnectionHandler implements ConnectionHandler {

	/**
	 * Indicates if secure connection
	 */
	private final boolean isSecure;

	/**
	 * {@link HttpRequestParser}.
	 */
	private final HttpRequestParser parser;

	/**
	 * {@link BufferPool}.
	 */
	private final BufferPool<ByteBuffer> bufferPool;

	/**
	 * List of previous {@link StreamBuffer} instances.
	 */
	private List<StreamBuffer<ByteBuffer>> previousBuffers = null;

	/**
	 * Current {@link StreamBuffer}.
	 */
	private StreamBuffer<ByteBuffer> currentBuffer = null;

	/**
	 * Initiate.
	 * 
	 * @param isSecure
	 *            Indicates if secure connection.
	 * @param bufferPool
	 *            {@link BufferPool}.
	 * @param parserMetaData
	 *            {@link HttpRequestParserMetaData}.
	 */
	public HttpConnectionHandler(boolean isSecure, BufferPool<ByteBuffer> bufferPool,
			HttpRequestParserMetaData parserMetaData) {
		this.isSecure = isSecure;
		this.parser = new HttpRequestParser(parserMetaData);
		this.bufferPool = bufferPool;
	}

	/*
	 * ================ ConnectionHandler ==============================
	 */

	@Override
	public void handleRead(StreamBuffer<ByteBuffer> buffer) throws IOException {
		try {

			// Determine if new buffer
			if (this.currentBuffer != buffer) {

				// New buffer, so move current to previous
				if (this.currentBuffer != null) {
					if (this.previousBuffers == null) {
						this.previousBuffers = new LinkedList<>();
					}
					this.previousBuffers.add(this.currentBuffer);
				}

				// Use new buffer
				this.currentBuffer = buffer;
				this.parser.appendStreamBuffer(buffer);
			}

			// Parse the read content (until no further requests)
			while (this.parser.parse()) {

				// Received the full HTTP request to start processing
				Supplier<HttpMethod> methodSupplier = this.parser.getMethod();
				Supplier<String> requestUriSupplier = this.parser.getRequestURI();
				HttpVersion version = this.parser.getVersion();
				NonMaterialisedHttpHeaders requestHeaders = this.parser.getHeaders();
				ByteSequence requestEntity = this.parser.getEntity();

				// Create the server HTTP connection
				ProcessAwareServerHttpConnectionManagedObject<ByteBuffer> serverHttpConnection = new ProcessAwareServerHttpConnectionManagedObject<>(
						this.isSecure, methodSupplier, requestUriSupplier, version, requestHeaders, requestEntity,
						null, this.bufferPool);

			}

		} catch (HttpException ex) {
			// Process failed parsing (close connection when response sent)
		}
	}

}