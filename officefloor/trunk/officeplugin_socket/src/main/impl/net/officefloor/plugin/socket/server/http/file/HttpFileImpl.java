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
package net.officefloor.plugin.socket.server.http.file;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

/**
 * {@link HttpFile} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class HttpFileImpl implements HttpFile {

	/**
	 * Path.
	 */
	private String path;

	/**
	 * <code>Content-Encoding</code>.
	 */
	private String contentEncoding;

	/**
	 * <code>Content-Type</code>.
	 */
	private String contentType;

	/**
	 * Contents.
	 */
	private transient ByteBuffer contents;

	/**
	 * Initiate.
	 *
	 * @param path
	 *            Path.
	 * @param contentEncoding
	 *            <code>Content-Encoding</code>.
	 * @param contentType
	 *            <code>Content-Type</code>.
	 * @param contents
	 *            Contents.
	 */
	public HttpFileImpl(String path, String contentEncoding,
			String contentType, ByteBuffer contents) {
		this.path = path;
		this.contentEncoding = (contentEncoding == null ? "" : contentEncoding);
		this.contentType = (contentType == null ? "" : contentType);
		this.contents = contents;
	}

	/*
	 * ================ HttpFile ======================================
	 */

	@Override
	public String getPath() {
		return this.path;
	}

	@Override
	public String getContentEncoding() {
		return this.contentEncoding;
	}

	@Override
	public String getContentType() {
		return this.contentType;
	}

	@Override
	public ByteBuffer getContents() {
		return this.contents;
	}

	/*
	 * ===================== Serializable =============================
	 */

	/**
	 * Due to the {@link ByteBuffer} must manually handle serialising this
	 * {@link HttpFile}.
	 *
	 * @param stream
	 *            {@link ObjectOutputStream}.
	 * @throws IOException
	 *             {@link IOException}.
	 */
	private void writeObject(ObjectOutputStream stream) throws IOException {

		// Write details of file
		stream.writeObject(this.path);
		stream.writeObject(this.contentEncoding);
		stream.writeObject(this.contentType);

		// Write contents of file
		int contentLength = this.contents.remaining();
		stream.writeInt(contentLength);
		for (int i = this.contents.position(); i < this.contents.limit(); i++) {
			byte contentByte = this.contents.get(i);
			stream.writeByte(contentByte);
		}
	}

	/**
	 * Due to the {@link ByteBuffer} must manually handle serialising this
	 * {@link HttpFile}.
	 *
	 * @param stream
	 *            {@link ObjectInputStream}.
	 * @throws IOException
	 *             {@link IOException}.
	 * @throws ClassNotFoundException
	 *             {@link ClassNotFoundException}.
	 */
	private void readObject(ObjectInputStream stream) throws IOException,
			ClassNotFoundException {

		// Obtain details of file
		this.path = (String) stream.readObject();
		this.contentEncoding = (String) stream.readObject();
		this.contentType = (String) stream.readObject();

		// Obtain contents of file
		int contentLength = stream.readInt();
		byte[] contents = new byte[contentLength];
		stream.readFully(contents);

		// Specify the contents
		this.contents = ByteBuffer.wrap(contents).asReadOnlyBuffer();
	}

}