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
package net.officefloor.web.resource.impl;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.stream.FileCompleteCallback;
import net.officefloor.web.resource.HttpFile;

/**
 * Abstract {@link HttpFile} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileImpl extends AbstractHttpResource implements HttpFile, FileCompleteCallback {

	/**
	 * <code>Content-Encoding</code> {@link HttpHeaderName}.
	 */
	private static final HttpHeaderName CONTENT_ENCODING = new HttpHeaderName("content-encoding");

	/**
	 * <code>Content-Encoding</code> {@link HttpHeaderValue}.
	 */
	private final HttpHeaderValue contentEncoding;

	/**
	 * <code>Content-Type</code> {@link HttpHeaderValue}.
	 */
	private final HttpHeaderValue contentType;

	/**
	 * {@link Charset}.
	 */
	private final Charset charset;

	/**
	 * {@link FileChannel}.
	 */
	private final FileChannel file;

	/**
	 * Initiate an existing {@link HttpFile}.
	 * 
	 * @param path
	 *            Path.
	 * @param contentEncoding
	 *            <code>Content-Encoding</code>.
	 * @param contentType
	 *            <code>Content-Type</code>.
	 * @param charset
	 *            {@link Charset}.
	 */
	public HttpFileImpl(String path, FileChannel file, HttpHeaderValue contentEncoding, HttpHeaderValue contentType,
			Charset charset) {
		super(path);
		this.contentEncoding = contentEncoding;
		this.contentType = contentType;
		this.charset = charset;
		this.file = file;
	}

	/*
	 * ================ HttpFile ======================================
	 */

	@Override
	public boolean isExist() {
		return true;
	}

	@Override
	public HttpHeaderValue getContentEncoding() {
		return this.contentEncoding;
	}

	@Override
	public HttpHeaderValue getContentType() {
		return this.contentType;
	}

	@Override
	public Charset getCharset() {
		return this.charset;
	}

	/**
	 * Writes the {@link HttpFile} to the {@link HttpResponse}.
	 * 
	 * @param response
	 *            {@link HttpResponse}
	 * @throws IOException
	 *             If fails to write the {@link HttpFile}.
	 */
	@Override
	public void writeTo(HttpResponse response) throws IOException {

		// Reset the HTTP response for writing the file
		response.reset();

		// Provide the details of the file
		if (this.contentEncoding != null) {
			response.getHeaders().addHeader(CONTENT_ENCODING, this.contentEncoding);
		}
		if (this.contentType != null) {
			response.setContentType(this.contentType, this.charset);
		}

		// Write the HTTP file content to response
		response.getEntityWriter().write(this.file, this);
	}

	/*
	 * ================ FileCompleteCallback =====================
	 */

	@Override
	public void complete(FileChannel file, boolean isWritten) throws IOException {
		// Nothing required, as just keeps reference to this alive
	}

	/*
	 * ===================== Closeable ==========================
	 */

	@Override
	public void close() throws IOException {
	}

}