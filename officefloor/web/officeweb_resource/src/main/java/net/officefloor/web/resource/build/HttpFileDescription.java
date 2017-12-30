/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.web.resource.build;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;

import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.web.resource.HttpFile;

/**
 * Allows providing a description of a {@link HttpFile}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpFileDescription {

	/**
	 * Obtains the name of the {@link HttpFile}.
	 * 
	 * @return Name of the {@link HttpFile}.
	 */
	String getName();

	/**
	 * <p>
	 * Obtains the contents of the {@link HttpFile}.
	 * <p>
	 * This allows interrogating the contents to determine/guess the
	 * description.
	 * 
	 * @return Contents of the {@link HttpFile}.
	 */
	InputStream getContents();

	/**
	 * Obtains the {@link Path} to the {@link HttpFile}.
	 * 
	 * @return {@link Path} to the {@link HttpFile}, or <code>null</code> if not
	 *         available (typically because {@link HttpFile} is from class
	 *         path).
	 */
	Path getPath();

	/**
	 * Describes the <code>Content-Encoding</code> of the {@link HttpFile}.
	 * 
	 * @param encoding
	 *            <code>Content-Encoding</code> of the {@link HttpFile}.
	 */
	void setContentEncoding(String encoding);

	/**
	 * Describes the <code>Content-Encoding</code> of the {@link HttpFile}.
	 * 
	 * @param encoding
	 *            <code>Content-Encoding</code> of the {@link HttpFile}.
	 */
	void setContentEncoding(HttpHeaderValue encoding);

	/**
	 * Describes the <code>Content-Type</code> of the {@link HttpFile}.
	 * 
	 * @param type
	 *            <code>Content-Type</code> of the {@link HttpFile}.
	 * @param charset
	 *            {@link Charset} or <code>null</code> if content is not text.
	 */
	void setContentType(String type, Charset charset);

	/**
	 * Describes the <code>Content-Type</code> of the {@link HttpFile}.
	 * 
	 * @param type
	 *            <code>Content-Type</code> of the {@link HttpFile}.
	 * @param charset
	 *            {@link Charset} or <code>null</code> if content is not text.
	 *            Note that the {@link Charset} will not be appended to the
	 *            <code>Content-Type</code>.
	 */
	void setContentType(HttpHeaderValue type, Charset charset);

}