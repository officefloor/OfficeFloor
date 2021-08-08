/*-
 * #%L
 * HttpServlet adapter for OfficeFloor HTTP Server
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

package net.officefloor.server.http.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * {@link ByteSequence} for the {@link HttpServletRequest} entity.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpServletEntityByteSequence implements ByteSequence {

	/**
	 * {@link HttpServletRequest}.
	 */
	private final HttpServletRequest request;

	/**
	 * Bytes.
	 */
	private byte[] bytes;

	/**
	 * Instantiate.
	 * 
	 * @param request {@link HttpServletRequest}.
	 * @throws IOException If fails to ready request entity.
	 */
	public HttpServletEntityByteSequence(HttpServletRequest request) throws IOException {
		this.request = request;
	}

	/**
	 * Ensure the bytes are loaded.
	 */
	private void ensureBytesLoaded() {

		// Determine if already loaded
		if (this.bytes != null) {
			return;
		}

		// Load the bytes
		try {
			InputStream requestEntity = this.request.getInputStream();
			int bytesRead = 0;
			byte[] transfer = new byte[1024];
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			do {
				bytesRead = requestEntity.read(transfer);
				if (bytesRead > 0) {
					buffer.write(transfer, 0, bytesRead);
				}
			} while (bytesRead == transfer.length);
			this.bytes = buffer.toByteArray();
		} catch (IOException ex) {
			// Failed to service (as must obtain entity)
			throw new HttpException(ex);
		}
	}

	/*
	 * ================== ByteSequence =======================
	 */

	@Override
	public byte byteAt(int index) {
		this.ensureBytesLoaded();
		return this.bytes[index];
	}

	@Override
	public int length() {
		this.ensureBytesLoaded();
		return this.bytes.length;
	}

}
