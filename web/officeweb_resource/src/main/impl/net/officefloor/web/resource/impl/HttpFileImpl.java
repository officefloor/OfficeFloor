/*-
 * #%L
 * Web resources
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.web.resource.impl;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

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
	 * Resource {@link Path} to clean up. May be <code>null</code>.
	 */
	private final Path cleanupResourcePath;

	/**
	 * {@link FileChannel}.
	 */
	private final FileChannel file;
	
	/**
	 * Initiate an existing {@link HttpFile}.
	 * 
	 * @param path
	 *            Path.
	 * @param cleanupResourcePath
	 *            Resource {@link Path} to clean up. May be <code>null</code>.
	 * @param file
	 *            {@link FileChannel} to the file.
	 * @param contentEncoding
	 *            <code>Content-Encoding</code>.
	 * @param contentType
	 *            <code>Content-Type</code>.
	 * @param charset
	 *            {@link Charset}.
	 */
	public HttpFileImpl(String path, Path cleanupResourcePath, FileChannel file, HttpHeaderValue contentEncoding,
			HttpHeaderValue contentType, Charset charset) {
		super(path);
		this.cleanupResourcePath = cleanupResourcePath;
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

		// Close the file channel
		this.file.close();

		// Determine if clean up file
		if (this.cleanupResourcePath != null) {
			Files.deleteIfExists(this.cleanupResourcePath);
		}
	}

}
