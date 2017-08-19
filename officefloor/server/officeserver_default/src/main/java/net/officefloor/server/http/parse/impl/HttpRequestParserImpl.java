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
package net.officefloor.server.http.parse.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.conversation.HttpEntity;
import net.officefloor.server.http.conversation.impl.HttpEntityImpl;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.impl.SerialisableHttpHeader;
import net.officefloor.server.http.parse.HttpRequestParseException;
import net.officefloor.server.http.parse.HttpRequestParser;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.ByteSequence;
import net.officefloor.server.stream.impl.ServerInputStreamImpl;

/**
 * {@link HttpRequestParser} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestParserImpl implements HttpRequestParser {

	private static long GET_PUT_MASK = longMask("GET ");

	private static long GET_ = longBytes("GET ");

	private static Supplier<HttpMethod> methodGet = () -> HttpMethod.GET;

	private static long PUT_ = longBytes("PUT ");

	private static Supplier<HttpMethod> methodPut = () -> HttpMethod.PUT;
	
	static {
		System.out.println("GET_PUT_MASK: " + Long.toHexString(GET_PUT_MASK));
		System.out.println("GET_: " + Long.toHexString(GET_));
		System.out.println("PUT_:" + Long.toHexString(PUT_));
	}

	/**
	 * Obtains the long mask for the {@link String} value.
	 * 
	 * @param text
	 *            String value.
	 * @return long value with bytes at top of long.
	 */
	private static long longMask(String text) {
		byte[] httpBytes = text.getBytes(ServerHttpConnection.HTTP_CHARSET);
		long mask = 0;
		for (int i = 0; i < httpBytes.length; i++) {
			mask <<= 8; // move bytes up by a byte
			mask |= 0xff; // include bytes in mask
		}
		for (int i = httpBytes.length; i < 8; i++) {
			mask <<= 8; // move bytes to not include bytes
		}
		return mask;
	}

	/**
	 * Obtains the long value for the {@link String} value.
	 * 
	 * @param text
	 *            String value.
	 * @return long value with bytes at top of long.
	 */
	private static long longBytes(String text) {
		byte[] httpBytes = text.getBytes(ServerHttpConnection.HTTP_CHARSET);
		long value = 0;
		for (int i = 0; i < httpBytes.length; i++) {
			value <<= 8; // move bytes up by a byte
			value |= httpBytes[i]; // include bytes in value
		}
		for (int i = httpBytes.length; i < 8; i++) {
			value <<= 8; // move bytes to leave zero for matching
		}
		return value;
	}

	/**
	 * {@link Supplier} for the {@link HttpMethod}.
	 */
	private Supplier<HttpMethod> method = null;

	/**
	 * Initiate.
	 * 
	 * @param maxHeaderCount
	 *            Maximum number of {@link HttpHeader} instances for a
	 *            {@link HttpRequest}.
	 * @param maxTextLength
	 *            Maximum number of bytes per TEXT.
	 * @param maxEntityLength
	 *            Maximum length of the entity. Requests with entities greater
	 *            than this will fail parsing.
	 */
	public HttpRequestParserImpl(int maxHeaderCount, int maxTextLength, long maxEntityLength) {
	}

	/*
	 * ================= HttpRequestParser ======================
	 */

	private static enum ParseState {
		NEW_REQUEST
	}

	@Override
	public boolean parse(ByteBuffer data) throws IOException, HttpRequestParseException {

		// Determine remaining length
		int remaining = data.position();

		// Set the starting position
		int position = 0;

		// Ensure have minimum bytes for request
		// ("M / V\n\r\n\r" = 9)
		if (remaining < 9) {
			return false; // not enough bytes yet for parsing
		}

		/*
		 * Read in long (8 bytes) to determine known methods. Note that all
		 * known methods are less than 8 bytes.
		 */
		long bytes = data.getLong(position);

		// Look for same length most common method first: "GET ", "PUT "
		long checkGetPut = bytes & GET_PUT_MASK;
		if (checkGetPut == GET_) {
			this.method = methodGet;
			
		} else if (checkGetPut == PUT_) {
			this.method = methodPut;
			
		} else {
			// Look for next most common methods: "POST ", "HEAD "
			long checkPostHead = bytes & POST_HEAD_MASK;
			if (checkPostHead == POST_) {
				this.method = methodPost;
				
			} else if (checkPostHead = HEAD_) {
				this.method = methodHead;
				
			}
		}

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isFinishedParsingBuffer() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub

	}

	@Override
	public Supplier<HttpMethod> getMethod() {
		return this.method;
	}

	@Override
	public Supplier<String> getRequestURI() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpVersion getVersion() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public NonMaterialisedHttpHeaders getHeaders() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ByteSequence getEntity() {
		// TODO Auto-generated method stub
		return null;
	}

}