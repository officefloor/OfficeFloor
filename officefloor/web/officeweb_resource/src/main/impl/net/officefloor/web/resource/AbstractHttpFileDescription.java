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
package net.officefloor.web.resource;

import java.nio.charset.Charset;

import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpFileDescription;
import net.officefloor.web.resource.HttpResource;

/**
 * Abstract {@link HttpFileDescription}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractHttpFileDescription implements
		HttpFileDescription, HttpResource {

	/**
	 * {@link HttpResource} path.
	 */
	protected final String resourcePath;

	/**
	 * <code>Content-Encoding</code> for the {@link HttpFile}.
	 */
	public String contentEncoding = null;

	/**
	 * <code>Content-Type</code> for the {@link HttpFile}.
	 */
	public String contentType = null;

	/**
	 * {@link Charset} for the {@link HttpFile}.
	 */
	public Charset charset = null;

	/**
	 * Initiate.
	 * 
	 * @param resourcePath
	 *            {@link HttpResource} path.
	 */
	public AbstractHttpFileDescription(String resourcePath) {
		this.resourcePath = resourcePath;
	}

	/**
	 * Obtains the <code>Content-Encoding</code>.
	 * 
	 * @return Content encoding.
	 */
	public String getContentEncoding() {
		return this.contentEncoding;
	}

	/**
	 * Obtains the <code>Content-Type</code>.
	 * 
	 * @return Content type.
	 */
	public String getContentType() {
		return this.contentType;
	}

	/**
	 * Obtains the {@link Charset}.
	 * 
	 * @return {@link Charset}.
	 */
	public Charset getCharset() {
		return this.charset;
	}

	/*
	 * ================== HttpFileDescription ============================
	 */

	@Override
	public HttpResource getResource() {
		return this;
	}

	@Override
	public void setContentEncoding(String encoding) {
		this.contentEncoding = encoding;
	}

	@Override
	public void setContentType(String type, Charset charset) {
		this.contentType = type;
		this.charset = charset;
	}

	/*
	 * ======================= HttpResource ========================
	 */

	@Override
	public String getPath() {
		return this.resourcePath;
	}

	@Override
	public boolean isExist() {
		// Always exist as describing the existing file
		return true;
	}

}