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

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * <p>
 * HTTP file.
 * <p>
 * All {@link HttpFile} implementations must be {@link Serializable} to enable
 * them to be serialised into caches.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpFile extends Serializable {

	/**
	 * <p>
	 * Obtains the Request URI path to this {@link HttpFile}.
	 * <p>
	 * The path is canonical to allow using it as a key for caching this
	 * {@link HttpFile}.
	 *
	 * @return Request URI path to this {@link HttpFile}.
	 */
	String getPath();

	/**
	 * Obtains the <code>Content-Encoding</code> for this {@link HttpFile}.
	 *
	 * @return <code>Content-Encoding</code> for this {@link HttpFile}.
	 */
	String getContentEncoding();

	/**
	 * Obtains the <code>Content-Type</code> for this {@link HttpFile}.
	 *
	 * @return <code>Content-Type</code> for this {@link HttpFile}.
	 */
	String getContentType();

	/**
	 * <p>
	 * Obtains the contents of this {@link HttpFile}.
	 * <p>
	 * The {@link ByteBuffer} will typically be read-only to prevent changes to
	 * the contents.
	 *
	 * @return Contents of this {@link HttpFile}.
	 */
	ByteBuffer getContents();

}