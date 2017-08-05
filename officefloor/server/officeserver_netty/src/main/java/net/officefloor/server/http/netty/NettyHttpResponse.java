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
package net.officefloor.server.http.netty;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.stream.ServerOutputStream;
import net.officefloor.server.stream.ServerWriter;

/**
 * Netty {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class NettyHttpResponse implements HttpResponse {

	/**
	 * Default {@link Charset}.
	 */
	private static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

	/**
	 * {@link ChannelHandlerContext}.
	 */
	private final ChannelHandlerContext context;

	/**
	 * {@link HttpStatus}.
	 */
	private HttpStatus status = HttpStatus.OK;

	/**
	 * {@link HttpVersion}.
	 */
	private HttpVersion version = HttpVersion.HTTP_1_1;

	/**
	 * {@link NettyHttpHeader} instances.
	 */
	private List<NettyHttpHeader> headers = new LinkedList<>();

	/**
	 * {@link Charset}.
	 */
	private Charset charset = DEFAULT_CHARSET;

	/**
	 * Instantiate.
	 * 
	 * @param context
	 *            {@link ChannelHandlerContext}.
	 */
	public NettyHttpResponse(ChannelHandlerContext context) {
		this.context = context;
	}

	/*
	 * ================= HttpResponse =======================
	 */

	@Override
	public void setHttpVersion(HttpVersion version) {
		this.version = version;
	}

	@Override
	public HttpVersion getHttpVersion() {
		return this.version;
	}

	@Override
	public void setHttpStatus(HttpStatus status) {
		this.status = status;
	}

	@Override
	public HttpStatus getHttpStatus() {
		return this.status;
	}

	@Override
	public void reset() throws IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public HttpHeader addHeader(String name, String value) throws IllegalArgumentException {
		NettyHttpHeader header = new NettyHttpHeader(name, value);
		this.headers.add(header);
		return header;
	}

	@Override
	public HttpHeader getHeader(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpHeader[] getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeHeader(HttpHeader header) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeHeaders(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setContentType(String contentType, Charset charset) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Charset getContentCharset() {
		return this.charset;
	}

	@Override
	public ServerOutputStream getEntity() throws IOException {
		return new ServerOutputStream() {

			@Override
			public void write(int b) throws IOException {
				// TODO Auto-generated method stub

			}

			@Override
			public void write(ByteBuffer cachedBuffer) throws IOException {
				// TODO Auto-generated method stub

			}
		};
	}

	@Override
	public ServerWriter getEntityWriter() throws IOException {
		return new ServerWriter() {

			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				// TODO Auto-generated method stub

			}

			@Override
			public void flush() throws IOException {
				// TODO Auto-generated method stub

			}

			@Override
			public void close() throws IOException {
				// TODO Auto-generated method stub

			}

			@Override
			public void write(ByteBuffer encodedBytes) throws IOException {
				// TODO Auto-generated method stub

			}

			@Override
			public void write(byte[] encodedBytes) throws IOException {
				// TODO Auto-generated method stub

			}
		};
	}

	@Override
	public void send() throws IOException {

		// Create and write the response
		FullHttpResponse response = new DefaultFullHttpResponse(io.netty.handler.codec.http.HttpVersion.HTTP_1_1,
				HttpResponseStatus.valueOf(this.status.getStatusCode()), false);
		HttpHeaders headers = response.headers();
		for (NettyHttpHeader header : this.headers) {
			headers.set(header.name, header.value);
		}

		// TODO REMOVE
		headers.set("Server", "WoOF 3.0.0");
		headers.set("Content-Type", "text/html; charset=UTF-8");
		headers.set("Content-Length", "11");

		response.content().writeBytes("hello world".getBytes(this.charset));
		this.context.write(response);
	}

	/**
	 * {@link HttpHeader}.
	 */
	private static class NettyHttpHeader implements HttpHeader {

		/**
		 * Name.
		 */
		private final String name;

		/**
		 * Value.
		 */
		private final String value;

		/**
		 * Instantiate.
		 * 
		 * @param name
		 *            Name.
		 * @param value
		 *            Value.
		 */
		public NettyHttpHeader(String name, String value) {
			this.name = name;
			this.value = value;
		}

		/*
		 * ================= HttpHeader ===================
		 */

		@Override
		public String getName() {
			return this.name;
		}

		@Override
		public String getValue() {
			return this.value;
		}
	}

}
