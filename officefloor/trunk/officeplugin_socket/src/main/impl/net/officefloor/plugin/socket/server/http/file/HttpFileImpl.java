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
import java.nio.charset.Charset;

/**
 * {@link HttpFile} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class HttpFileImpl implements HttpFile {

	/**
	 * {@link ByteBuffer} for empty {@link HttpFile}.
	 */
	private static final ByteBuffer EMPTY_CONTENTS = ByteBuffer.allocate(0);

	/**
	 * Path.
	 */
	private String path;

	/**
	 * Indicates if the {@link HttpFile} exists.
	 */
	private boolean isExist;

	/**
	 * <code>Content-Encoding</code>.
	 */
	private String contentEncoding;

	/**
	 * <code>Content-Type</code>.
	 */
	private String contentType;

	/**
	 * {@link Charset}.
	 */
	private transient Charset charset;

	/**
	 * Contents.
	 */
	private transient ByteBuffer contents;

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
	 * @param contents
	 *            Contents.
	 */
	public HttpFileImpl(String path, String contentEncoding,
			String contentType, Charset charset, ByteBuffer contents) {
		this.path = path;
		this.isExist = true;
		this.contentEncoding = (contentEncoding == null ? "" : contentEncoding);
		this.contentType = (contentType == null ? "" : contentType);
		this.charset = charset;
		this.contents = (contents == null ? EMPTY_CONTENTS : contents);
	}

	/**
	 * Initiate a non-existing {@link HttpFile}.
	 *
	 * @param path
	 *            Path.
	 */
	public HttpFileImpl(String path) {
		this.path = path;
		this.isExist = false;
		this.contentEncoding = "";
		this.contentType = "";
		this.charset = null;
		this.contents = EMPTY_CONTENTS;
	}

	/*
	 * ================ HttpFile ======================================
	 */

	@Override
	public String getPath() {
		return this.path;
	}

	@Override
	public boolean isExist() {
		return this.isExist;
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
	public Charset getCharset() {
		return this.charset;
	}

	@Override
	public ByteBuffer getContents() {
		return this.contents;
	}

	/*
	 * ===================== Object ===================================
	 */

	@Override
	public boolean equals(Object obj) {

		// Check if same object
		if (this == obj) {
			return true;
		}

		// Ensure same type
		if (!(obj instanceof HttpFile)) {
			return false;
		}
		HttpFile that = (HttpFile) obj;

		// Return whether details same
		return (this.path.equals(that.getPath()))
				&& (this.contentEncoding.equals(that.getContentEncoding()))
				&& (this.contentType.equals(that.getContentType()) && isCharsetMatch(
						this.charset, that.getCharset()));
	}

	/**
	 * Returns whether the {@link Charset} matches the other {@link Charset}.
	 *
	 * @param a
	 *            {@link Charset}.
	 * @param b
	 *            {@link Charset}.
	 * @return <code>true</code> if match.
	 */
	private static boolean isCharsetMatch(Charset a, Charset b) {
		if (a != null) {
			// Have a, so match if equal
			return a.equals(b);
		} else {
			// Only match if b also null
			return (b == null);
		}
	}

	@Override
	public int hashCode() {
		int hash = this.getClass().hashCode();
		hash = (hash * 31) + this.path.hashCode();
		hash = (hash * 31) + this.contentEncoding.hashCode();
		hash = (hash * 31) + this.contentType.hashCode();
		if (this.charset != null) {
			hash = (hash * 31) + this.charset.hashCode();
		}
		return hash;
	}

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder();
		text.append(this.getClass().getSimpleName());
		text.append(": ");
		text.append(this.path);
		text.append(" (Exist: ");
		text.append(this.isExist);
		text.append(", Content-Encoding: ");
		text.append(this.contentEncoding);
		text.append(", Content-Type: ");
		text.append(this.contentType);
		if (this.charset != null) {
			text.append("; charset=");
			text.append(this.charset.name());
		}
		text.append(")");
		return text.toString();
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

		// Write path and whether exists
		stream.writeObject(this.path);
		stream.writeBoolean(this.isExist);

		// If not exist, write no further details
		if (!this.isExist) {
			return;
		}

		// Write details of the file
		stream.writeObject(this.contentEncoding);
		stream.writeObject(this.contentType);
		String charsetName = (this.charset == null ? null : this.charset.name());
		stream.writeObject(charsetName);

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

		// Obtain path and whether file exists
		this.path = (String) stream.readObject();
		this.isExist = stream.readBoolean();

		// Handle based on whether exists
		if (!this.isExist) {
			// Not exist, so set not exist details of file
			this.contentEncoding = "";
			this.contentType = "";
			this.charset = null;
			this.contents = EMPTY_CONTENTS;

		} else {
			// Exists, so obtain details of file
			this.contentEncoding = (String) stream.readObject();
			this.contentType = (String) stream.readObject();
			String charsetName = (String) stream.readObject();
			this.charset = (charsetName == null ? null : Charset
					.forName(charsetName));

			// Obtain contents of file
			int contentLength = stream.readInt();
			byte[] contents = new byte[contentLength];
			stream.readFully(contents);

			// Specify the contents
			this.contents = ByteBuffer.wrap(contents).asReadOnlyBuffer();
		}
	}

}