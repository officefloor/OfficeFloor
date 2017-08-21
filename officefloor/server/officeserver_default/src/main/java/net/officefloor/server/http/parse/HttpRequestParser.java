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
package net.officefloor.server.http.parse;

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * Parses a {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequestParser {

	/**
	 * Parses the {@link HttpRequest} from the data.
	 * 
	 * @param buffer
	 *            {@link StreamBuffer} containing the data to be parsed for the
	 *            {@link HttpRequest}.
	 * @param startIndex
	 *            Index within the data to start parsing.
	 * @return <code>true</code> if the {@link HttpRequest} has been fully
	 *         parsed. <code>false</code> indicates this method should be called
	 *         again when further data is available to obtain the full
	 *         {@link HttpRequest}.
	 * @throws HttpException
	 *             If failure to parse {@link HttpRequest}.
	 */
	boolean parse(StreamBuffer<ByteBuffer> buffer) throws HttpException;

	/**
	 * Determines if finished reading data from the {@link ByteBuffer}.
	 * 
	 * @return <code>true</code> if finished reading data from the
	 *         {@link ByteBuffer}. <code>false</code> to have the
	 *         {@link ByteBuffer} passed back into parsing to complete parsing
	 *         all the data.
	 */
	boolean isFinishedParsingBuffer();

	/**
	 * Resets for parsing another {@link HttpRequest}.
	 */
	void reset();

	/**
	 * Obtains the {@link Supplier} for the {@link HttpMethod}.
	 * 
	 * @return {@link Supplier} for the {@link HttpMethod}.
	 */
	Supplier<HttpMethod> getMethod();

	/**
	 * Obtains the {@link Supplier} for the request URI.
	 * 
	 * @return {@link Supplier} for the request URI.
	 */
	Supplier<String> getRequestURI();

	/**
	 * Obtains the {@link HttpVersion}.
	 * 
	 * @return {@link HttpVersion}.
	 */
	HttpVersion getVersion();

	/**
	 * Obtains the {@link HttpHeader} instances in the order supplied.
	 * 
	 * @return {@link HttpHeader} instances in the order supplied.
	 */
	NonMaterialisedHttpHeaders getHeaders();

	/**
	 * Obtains the {@link ByteSequence} to the entity data of the
	 * {@link HttpRequest}.
	 * 
	 * @return {@link ByteSequence} to the entity data of the
	 *         {@link HttpRequest}.
	 */
	ByteSequence getEntity();

}