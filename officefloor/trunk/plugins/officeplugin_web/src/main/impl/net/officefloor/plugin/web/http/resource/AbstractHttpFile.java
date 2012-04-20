/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
import java.nio.charset.Charset;

/**
 * Abstract {@link HttpFile} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpFile extends AbstractHttpResource implements
		HttpFile {

	/**
	 * <code>Content-Encoding</code>.
	 */
	protected String contentEncoding;

	/**
	 * <code>Content-Type</code>.
	 */
	protected String contentType;

	/**
	 * {@link Charset}.
	 */
	protected transient Charset charset;

	/**
	 * Initiate an existing {@link HttpFile}.
	 * 
	 * @param resourcePath
	 *            Resource path.
	 * @param description
	 *            {@link AbstractHttpFileDescription}.
	 */
	public AbstractHttpFile(String resourcePath,
			AbstractHttpFileDescription description) {
		super(resourcePath);
		String contentEncoding = description.getContentEncoding();
		this.contentEncoding = (contentEncoding == null ? "" : contentEncoding);
		String contentType = description.getContentType();
		this.contentType = (contentType == null ? "" : contentType);
		this.charset = description.getCharset();
	}

	/*
	 * ================ HttpFile ======================================
	 */

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
		if (!(obj instanceof AbstractHttpFile)) {
			return false;
		}
		AbstractHttpFile that = (AbstractHttpFile) obj;

		// Return whether details same
		return (this.getPath().equals(that.getPath()))
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
		int hash = super.hashCode();
		hash = (hash * 31) + this.contentEncoding.hashCode();
		hash = (hash * 31) + this.contentType.hashCode();
		if (this.charset != null) {
			hash = (hash * 31) + this.charset.hashCode();
		}
		return hash;
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
		stream.writeObject(this.contentEncoding);
		stream.writeObject(this.contentType);
		String charsetName = (this.charset == null ? null : this.charset.name());
		stream.writeObject(charsetName);

//		// Write state of implementation
//		this.writeImplementingObject(stream);
	}

//	/**
//	 * Writes the implementing object state.
//	 * 
//	 * @param stream
//	 *            {@link ObjectOutputStream}.
//	 * @throws IOException
//	 *             {@link IOException}.
//	 */
//	protected abstract void writeImplementingObject(ObjectOutputStream stream)
//			throws IOException;

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
		this.contentEncoding = (String) stream.readObject();
		this.contentType = (String) stream.readObject();
		String charsetName = (String) stream.readObject();
		this.charset = (charsetName == null ? null : Charset
				.forName(charsetName));

//		// Read state of implementation
//		this.readImplementingObject(stream);
	}

//	/**
//	 * Reads the implementing object state.
//	 * 
//	 * @param stream
//	 *            {@link ObjectInputStream}.
//	 * @throws IOException
//	 *             {@link IOException}.
//	 * @throws ClassNotFoundException
//	 *             {@link ClassNotFoundException}.
//	 */
//	protected abstract void readImplementingObject(ObjectInputStream stream)
//			throws IOException, ClassNotFoundException;

}