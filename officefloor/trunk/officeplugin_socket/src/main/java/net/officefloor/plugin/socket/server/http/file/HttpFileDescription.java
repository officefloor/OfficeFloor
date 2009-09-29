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

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.plugin.socket.server.http.HttpRequest;

/**
 * Description of a {@link HttpFile}.
 *
 * @author Daniel Sagenschneider
 */
public interface HttpFileDescription {

	/**
	 * <p>
	 * Obtains the extension of the {@link HttpFile} name.
	 * <p>
	 * This allows mapping file extensions to descriptions. The extension is
	 * taken directly from the {@link HttpRequest} path and subsequently may
	 * vary in case.
	 *
	 * @return Extension of the {@link HttpFile} name.
	 */
	String getExtension();

	/**
	 * <p>
	 * Obtains the contents of the {@link HttpFile}.
	 * <p>
	 * This allows interrogating the contents to determine/guess the
	 * description.
	 *
	 * @return Contents of the {@link HttpFile}.
	 */
	ByteBuffer getContents();

	/**
	 * This is to be invoked by the {@link HttpFileDescriber} to describe the
	 * <code>Content-Encoding</code> of the {@link HttpFile}.
	 *
	 * @param encoding
	 *            <code>Content-Encoding</code> of the {@link HttpFile}.
	 */
	void setContentEncoding(String encoding);

	/**
	 * This is to be invoked by the {@link HttpFileDescriber} to describe the
	 * <code>Content-Type</code> of the {@link HttpFile}.
	 *
	 * @param type
	 *            <code>Content-Type</code> of the {@link HttpFile}.
	 * @param charset
	 *            {@link Charset} or <code>null</code> if content is not text.
	 */
	void setContentType(String type, Charset charset);

}