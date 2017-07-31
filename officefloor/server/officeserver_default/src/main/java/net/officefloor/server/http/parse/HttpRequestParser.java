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

import java.io.IOException;
import java.util.List;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.conversation.HttpEntity;

/**
 * Parses a {@link HttpRequest}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpRequestParser {

	/**
	 * Parses the {@link HttpRequest} from the data.
	 * 
	 * @param data
	 *            Data to be parsed for the {@link HttpRequest}.
	 * @param startIndex
	 *            Index within the data to start parsing.
	 * @return <code>true</code> if the {@link HttpRequest} has been fully
	 *         parsed. <code>false</code> indicates this method should be called
	 *         again when further data is available to obtain the full
	 *         {@link HttpRequest}.
	 * @throws IOException
	 *             If fails to read bytes.
	 * @throws HttpRequestParseException
	 *             If failure to parse {@link HttpRequest}.
	 * 
	 * @see #nextByteToParseIndex()
	 */
	boolean parse(byte[] data, int startIndex) throws IOException,
			HttpRequestParseException;

	/**
	 * <p>
	 * Obtains the index of the next byte to parse from the previous
	 * {@link #parse(byte[], int)}.
	 * <p>
	 * Should all bytes be consumed this will return <code>-1</code>. Note that
	 * if {@link #parse(byte[], int)} returns <code>false</code>, this will
	 * always return <code>-1</code> as all bytes are to be consumed.
	 * 
	 * @return Index of the next byte to parse or <code>-1</code> if all bytes
	 *         consumed.
	 */
	int nextByteToParseIndex();

	/**
	 * Resets for parsing another {@link HttpRequest}.
	 */
	void reset();

	/**
	 * Obtains the method.
	 * 
	 * @return Method.
	 */
	String getMethod();

	/**
	 * Obtains the request URI.
	 * 
	 * @return Request URI.
	 */
	String getRequestURI();

	/**
	 * Obtains the HTTP version.
	 * 
	 * @return HTTP version.
	 */
	String getHttpVersion();

	/**
	 * Obtains the {@link HttpHeader} instances in the order supplied.
	 * 
	 * @return {@link HttpHeader} instances in the order supplied.
	 */
	List<HttpHeader> getHeaders();

	/**
	 * Obtains the {@link HttpEntity} of the {@link HttpRequest}.
	 * 
	 * @return {@link HttpEntity} of the {@link HttpRequest}.
	 */
	HttpEntity getEntity();

}