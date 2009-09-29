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
import java.nio.charset.Charset;

import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.socket.server.http.HttpRequest;

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
	 * @return Canonical request URI path to this {@link HttpFile}.
	 */
	String getPath();

	/**
	 * <p>
	 * Indicates if this {@link HttpFile} exists. Should this {@link HttpFile}
	 * not exist, only the path will be available.
	 * <p>
	 * This allows for caching of {@link HttpFile} instances not existing.
	 * <p>
	 * It also enables implementations of {@link ManagedObjectSource} instances
	 * to provide the {@link HttpFile} from a {@link HttpRequest} dependency -
	 * can always provide an instance with this indicating if exists.
	 *
	 * @return <code>true</code> if this {@link HttpFile} exists.
	 */
	boolean isExist();

	/**
	 * Obtains the <code>Content-Encoding</code> for this {@link HttpFile}.
	 *
	 * @return <code>Content-Encoding</code> for this {@link HttpFile}.
	 */
	String getContentEncoding();

	/**
	 * <p>
	 * Obtains the <code>Content-Type</code> for this {@link HttpFile}.
	 * <p>
	 * The value should omit the <code>charset</code> attribute.
	 *
	 * @return <code>Content-Type</code> for this {@link HttpFile}.
	 */
	String getContentType();

	/**
	 * Obtains the {@link Charset} for the contents.
	 *
	 * @return {@link Charset} or <code>null</code> if contents are not text or
	 *         the {@link Charset} is unknown.
	 */
	Charset getCharset();

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