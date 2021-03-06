/*-
 * #%L
 * HttpServlet adapter for OfficeFloor HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
