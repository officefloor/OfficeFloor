/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2011 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.resource;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.plugin.web.http.resource.HttpFile;

/**
 * {@link HttpFile} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileImpl implements HttpFile {

	/**
	 * Resource path.
	 */
	private String resourcePath;

	/**
	 * Class path.
	 */
	private String classPath;

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
	 * Initiate an existing {@link HttpFile}.
	 * 
	 * @param resourcePath
	 *            Resource path.
	 * @param classPath
	 *            Class path.
	 * @param contentEncoding
	 *            <code>Content-Encoding</code>.
	 * @param contentType
	 *            <code>Content-Type</code>.
	 * @param charset
	 *            {@link Charset}.
	 * @param contents
	 *            Contents.
	 */
	public HttpFileImpl(String resourcePath, String classPath,
			String contentEncoding, String contentType, Charset charset) {
		this.resourcePath = resourcePath;
		this.classPath = classPath;
		this.contentEncoding = (contentEncoding == null ? "" : contentEncoding);
		this.contentType = (contentType == null ? "" : contentType);
		this.charset = charset;
	}

	/*
	 * ================ HttpFile ======================================
	 */

	@Override
	public String getPath() {
		return this.resourcePath;
	}

	@Override
	public boolean isExist() {
		// File always exists
		return true;
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
		return ClasspathHttpResourceFactory
				.getHttpResourceContents(this.classPath);
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
		if (!(obj instanceof HttpFileImpl)) {
			return false;
		}
		HttpFileImpl that = (HttpFileImpl) obj;

		// Return whether details same
		return (this.resourcePath.equals(that.getPath()))
				&& (this.classPath.equals(that.classPath))
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
		hash = (hash * 31) + this.resourcePath.hashCode();
		hash = (hash * 31) + this.classPath.hashCode();
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
		text.append(this.resourcePath);
		text.append(" (Class path: ");
		text.append(this.classPath);
		if (this.contentEncoding.length() > 0) {
			text.append(", Content-Encoding: ");
			text.append(this.contentEncoding);
		}
		if (this.contentType.length() > 0) {
			text.append(", Content-Type: ");
			text.append(this.contentType);
			if (this.charset != null) {
				text.append("; charset=");
				text.append(this.charset.name());
			}
		}
		text.append(")");
		return text.toString();
	}

	/*
	 * ===================== Serializable =============================
	 */

	/**
	 * Due to the {@link Charset} must manually handle serialising this
	 * {@link HttpFile}.
	 * 
	 * @param stream
	 *            {@link ObjectOutputStream}.
	 * @throws IOException
	 *             {@link IOException}.
	 */
	private void writeObject(ObjectOutputStream stream) throws IOException {

		// Write path and description of file
		stream.writeObject(this.resourcePath);
		stream.writeObject(this.classPath);
		stream.writeObject(this.contentEncoding);
		stream.writeObject(this.contentType);
		String charsetName = (this.charset == null ? null : this.charset.name());
		stream.writeObject(charsetName);
	}

	/**
	 * Due to the {@link Charset} must manually handle serialising this
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

		// Obtain path and description of file
		this.resourcePath = (String) stream.readObject();
		this.classPath = (String) stream.readObject();
		this.contentEncoding = (String) stream.readObject();
		this.contentType = (String) stream.readObject();
		String charsetName = (String) stream.readObject();
		this.charset = (charsetName == null ? null : Charset
				.forName(charsetName));
	}

}