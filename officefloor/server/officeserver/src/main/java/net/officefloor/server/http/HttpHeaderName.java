/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.server.http;

import java.io.IOException;

import net.officefloor.server.stream.ServerWriter;

/**
 * Means to provide common {@link HttpHeader} names in already encoded HTTP
 * bytes for faster writing.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpHeaderName {

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * Pre-encoded bytes of name ready for HTTP output.
	 */
	private final byte[] encodedName;

	/**
	 * Instantiate.
	 * 
	 * @param name
	 *            {@link HttpHeaderName}.
	 */
	public HttpHeaderName(String name) {
		this.name = name.toLowerCase(); // case insensitive
		this.encodedName = this.name.getBytes(ServerHttpConnection.HTTP_CHARSET);
	}

	/**
	 * Obtains the name.
	 * 
	 * @return Name.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Writes the name to the {@link ServerWriter}.
	 * 
	 * @param writer
	 *            {@link ServerWriter}.
	 * @throws IOException
	 *             If fails to write the name.
	 */
	public void writeName(ServerWriter writer) throws IOException {
		writer.write(this.encodedName);
	}

}