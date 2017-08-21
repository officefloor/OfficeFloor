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

import java.nio.ByteBuffer;
import java.util.function.Supplier;

import net.officefloor.server.buffer.StreamBufferByteSequence;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.http.parse.HttpRequestParser;
import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * {@link HttpRequestParser} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestParserImpl implements HttpRequestParser {

	private static long GET_PUT_MASK = longMask("GET ");
	private static long POST_HEAD_MASK = longMask("POST ");
	private static long CONNECT_OPTIONS_MASK = longMask("CONNECT ");
	private static long DELETE_MASK = longMask("DELETE ");

	private static long GET_ = longBytes("GET ");
	private static long PUT_ = longBytes("PUT ");
	private static long POST_ = longBytes("POST ");
	private static long HEAD_ = longBytes("HEAD ");
	private static long CONNECT_ = longBytes("CONNECT ");
	private static long OPTIONS_ = longBytes("OPTIONS ");
	private static long DELETE_ = longBytes("DELETE ");

	private static long HTTP_1_0 = longBytes("HTTP/1.0");
	private static long HTTP_1_1 = longBytes("HTTP/1.1");

	private static short CRLF = shortBytes("\n\r");

	private static Supplier<HttpMethod> methodGet = () -> HttpMethod.GET;
	private static Supplier<HttpMethod> methodPut = () -> HttpMethod.PUT;
	private static Supplier<HttpMethod> methodPost = () -> HttpMethod.POST;
	private static Supplier<HttpMethod> methodHead = () -> HttpMethod.HEAD;
	private static Supplier<HttpMethod> methodConnect = () -> HttpMethod.CONNECT;
	private static Supplier<HttpMethod> methodOptions = () -> HttpMethod.OPTIONS;
	private static Supplier<HttpMethod> methodDelete = () -> HttpMethod.DELETE;

	private static byte HTTP_SPACE = httpByte(" ");
	private static long MASK_SPACE = ByteBufferScanner.createScanByteMask(HTTP_SPACE);
	private static byte HTTP_CR = httpByte("\r");
	private static long MASK_CR = ByteBufferScanner.createScanByteMask(HTTP_CR);
	private static byte HTTP_LF = httpByte("\n");
	private static byte HTTP_COLON = httpByte(":");
	private static long MASK_COLON = ByteBufferScanner.createScanByteMask(HTTP_COLON);

	/**
	 * Obtains the HTTP byte for the {@link String} value.
	 * 
	 * @param text
	 *            String value.
	 * @return byte value.
	 */
	private static byte httpByte(String text) {
		return text.getBytes(ServerHttpConnection.HTTP_CHARSET)[0];
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
	 * Obtains the short value for the {@link String} value.
	 * 
	 * @param text
	 *            String value.
	 * @return Short value with bytes at top of long.
	 */
	private static short shortBytes(String text) {
		byte[] httpBytes = text.getBytes(ServerHttpConnection.HTTP_CHARSET);
		short value = 0;
		for (int i = 0; i < httpBytes.length; i++) {
			value <<= 8; // move bytes up by a byte
			value |= httpBytes[i]; // include bytes in value
		}
		for (int i = httpBytes.length; i < 2; i++) {
			value <<= 8; // move bytes to leave zero for matching
		}
		return value;
	}

	/**
	 * {@link Supplier} for the {@link HttpMethod}.
	 */
	private Supplier<HttpMethod> method = null;

	/**
	 * {@link Supplier} for the request URI.
	 */
	private Supplier<String> requestUri = null;

	/**
	 * {@link HttpVersion}.
	 */
	private HttpVersion version = null;

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

	@Override
	public boolean parse(StreamBuffer<ByteBuffer> buffer) throws HttpException {

		// Obtain the byte buffer containing the data
		ByteBuffer data = buffer.getPooledBuffer();

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
			position += 4; // after space

		} else if (checkGetPut == PUT_) {
			this.method = methodPut;
			position += 4; // after space

		} else {
			// Look for next most common methods: "POST ", "HEAD "
			long checkPostHead = bytes & POST_HEAD_MASK;
			if (checkPostHead == POST_) {
				this.method = methodPost;
				position += 5; // after space

			} else if (checkPostHead == HEAD_) {
				this.method = methodHead;
				position += 5; // after space

			} else {
				// Check next common: "CONNECT ", "OPTIONS "
				long checkConnectOptions = bytes & CONNECT_OPTIONS_MASK;
				if (checkConnectOptions == CONNECT_) {
					this.method = methodConnect;
					position += 8; // after space

				} else if (checkConnectOptions == OPTIONS_) {
					this.method = methodOptions;
					position += 8; // after space

				} else {
					// Check for remaining common: "DELETE "
					long checkDelete = bytes & DELETE_MASK;
					if (checkDelete == DELETE_) {
						this.method = methodDelete;
						position += 7; // after space

					} else {
						// Custom method, so find the space
						int spacePosition = ByteBufferScanner.scanToByte(data, position, HTTP_SPACE, MASK_SPACE);
						if (spacePosition == -1) {
							// TODO create byte sequence for current content

							// Not enough data
							return false;

						} else {
							// Create method from byte sequence
							int methodByteLength = spacePosition - position;
							if (methodByteLength == 0) {
								// No method provided
								// TODO handle no method provided
							}
							final StreamBufferByteSequence methodSequence = new StreamBufferByteSequence(buffer,
									position, methodByteLength);
							this.method = () -> new HttpMethod(methodSequence.toHttpString());
						}
					}
				}
			}
		}

		// Obtain the path
		int spacePosition = ByteBufferScanner.scanToByte(data, position, HTTP_SPACE, MASK_SPACE);
		if (spacePosition == -1) {
			return false; // need more data to complete path
		}

		// Create the path
		final StreamBufferByteSequence uriSequence = new StreamBufferByteSequence(buffer, position,
				spacePosition - position);
		this.requestUri = () -> uriSequence.decodeUri((result) -> new Error()).toUriString((message) -> new Error());
		position = spacePosition + 1; // +1 to more past space

		// Obtain the version
		int crPosition = ByteBufferScanner.scanToByte(data, position, HTTP_CR, MASK_CR);
		if (crPosition == -1) {
			return false; // need more data to complete version
		}

		// Determine if common version
		final int commonVersionLength = 8; // HTTP/1.X
		int versionByteLength = crPosition - position;
		if (versionByteLength == commonVersionLength) {
			long checkVersion = data.getLong(position);
			if (checkVersion == HTTP_1_1) {
				this.version = HttpVersion.HTTP_1_1;
				position += commonVersionLength;

			} else if (checkVersion == HTTP_1_0) {
				this.version = HttpVersion.HTTP_1_0;
				position += commonVersionLength;
			}
		}

		// If no common version, create custom version
		if (this.version == null) {
			StreamBufferByteSequence versionSequence = new StreamBufferByteSequence(buffer, position,
					crPosition - position);
			String httpVersionText = versionSequence.toHttpString();
			this.version = new HttpVersion(httpVersionText);
		}

		// Ensure next character is LF (after CR)
		int lfPosition = crPosition + 1;
		byte character = data.get(lfPosition);
		if (character != HTTP_LF) {
			throw new HttpException(HttpStatus.BAD_REQUEST);
		}
		position = lfPosition + 1;

		// Determine if end of headers
		short checkCrLf = data.getShort(position);
		if (checkCrLf == CRLF) {
			// End of header section
			System.out.println("TODO handle end of header section");

		} else {
			// Scan in the header name
			int colonPosition = ByteBufferScanner.scanToByte(data, position, HTTP_COLON, MASK_COLON);
			if (colonPosition == -1) {
				return false; // need more data to complete header name
			}

			// Scan in the header value
			position = colonPosition + 1;
			crPosition = ByteBufferScanner.scanToByte(data, position, HTTP_CR, MASK_CR);
			if (crPosition == -1) {
				return false; // need more data to complete header value
			}

			// Ensure next character is LF (after CR)
			lfPosition = crPosition + 1;
			character = data.get(lfPosition);
			if (character != HTTP_LF) {
				throw new HttpException(HttpStatus.BAD_REQUEST);
			}
			position = lfPosition + 1;
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
		return this.requestUri;
	}

	@Override
	public HttpVersion getVersion() {
		return this.version;
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