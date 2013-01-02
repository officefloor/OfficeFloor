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
package net.officefloor.plugin.web.http.resource.war;

import java.io.File;
import java.nio.ByteBuffer;

import net.officefloor.plugin.web.http.resource.AbstractHttpFile;
import net.officefloor.plugin.web.http.resource.AbstractHttpFileDescription;
import net.officefloor.plugin.web.http.resource.HttpFile;

/**
 * War {@link HttpFile}.
 * 
 * @author Daniel Sagenschneider
 */
public class WarHttpFile extends AbstractHttpFile {

	/**
	 * {@link File}.
	 */
	private File file;

	/**
	 * Initiate.
	 * 
	 * @param resourcePath
	 *            Resource path.
	 * @param file
	 *            {@link File}.
	 * @param description
	 *            {@link AbstractHttpFileDescription}.
	 */
	public WarHttpFile(String resourcePath, File file,
			AbstractHttpFileDescription description) {
		super(resourcePath, description);
		this.file = file;
	}

	/*
	 * ================== AbstractHttpFile =========================
	 */

	@Override
	public ByteBuffer getContents() {
		return WarHttpResourceFactory.getHttpResourceContents(this.file);
	}

	/*
	 * ===================== Object ===================================
	 */

	@Override
	public String toString() {
		StringBuilder text = new StringBuilder();
		text.append(this.getClass().getSimpleName());
		text.append(": ");
		text.append(this.resourcePath);
		text.append(" (file: ");
		text.append(this.file.getAbsolutePath());
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

}