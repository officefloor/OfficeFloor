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
package net.officefloor.plugin.socket.server.http.response;

import java.io.IOException;
import java.nio.ByteBuffer;

import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.stream.OutputBufferStream;

/**
 * Tests the {@link HttpResponseWriter}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpResponseWriterTest extends OfficeFrameTestCase {

	/**
	 * {@link HttpResponseWriter} to test.
	 */
	private final HttpResponseWriter writer = new HttpResponseWriterImpl();

	/**
	 * Mock {@link ServerHttpConnection}.
	 */
	private final ServerHttpConnection connection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * Mock {@link HttpResponse}.
	 */
	private final HttpResponse response = this.createMock(HttpResponse.class);

	/**
	 * Mock {@link OutputBufferStream}.
	 */
	private final OutputBufferStream body = this
			.createMock(OutputBufferStream.class);

	/**
	 * Ensure can write contents.
	 */
	public void testWriteContents() throws IOException {

		final String contentEncoding = "deflate";
		final String contentType = "text/plain";
		final ByteBuffer contents = ByteBuffer.allocate(0);

		// Record
		this.recordReturn(this.connection, this.connection.getHttpResponse(),
				this.response);
		this.response.removeHeaders("Content-Encoding");
		this.recordReturn(this.response, this.response.addHeader(
				"Content-Encoding", contentEncoding), null);
		this.response.removeHeaders("Content-Type");
		this.recordReturn(this.response, this.response.addHeader(
				"Content-Type", contentType), null);
		this.recordReturn(this.response, this.response.getBody(), this.body);
		this.body.append(contents);

		// Test
		this.replayMockObjects();
		this.writer.writeContent(this.connection, contentEncoding, contentType,
				contents);
		this.verifyMockObjects();
	}
}