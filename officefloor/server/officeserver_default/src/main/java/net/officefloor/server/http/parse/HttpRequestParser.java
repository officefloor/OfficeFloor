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

import java.util.function.Supplier;

import net.officefloor.server.buffer.StreamBufferByteSequence;
import net.officefloor.server.buffer.StreamBufferScanner;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpHeader;
import net.officefloor.server.http.HttpMethod;
import net.officefloor.server.http.HttpRequest;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.server.http.HttpVersion;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.impl.MaterialisingHttpRequestHeaders;
import net.officefloor.server.http.impl.NonMaterialisedHttpHeaders;
import net.officefloor.server.stream.impl.ByteSequence;

/**
 * {@link HttpRequest} parser.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRequestParser extends StreamBufferScanner {

	/**
	 * Meta-data for the {@link HttpRequestParser}.
	 */
	public static class HttpRequestParserMetaData {

		/**
		 * Maximum number of {@link HttpHeader} instances for a
		 * {@link HttpRequest}.
		 */
		public final int maxHeaderCount;

		/**
		 * Maximum number of bytes per TEXT.
		 */
		public final int maxTextLength;

		/**
		 * Maximum length of the entity. Requests with entities greater than
		 * this will fail parsing.
		 */
		public final long maxEntityLength;

		/**
		 * Initiate.
		 * 
		 * @param maxHeaderCount
		 *            Maximum number of {@link HttpHeader} instances for a
		 *            {@link HttpRequest}.
		 * @param maxTextLength
		 *            Maximum number of bytes per TEXT.
		 * @param maxEntityLength
		 *            Maximum length of the entity. Requests with entities
		 *            greater than this will fail parsing.
		 */
		public HttpRequestParserMetaData(int maxHeaderCount, int maxTextLength, long maxEntityLength) {
			this.maxHeaderCount = maxHeaderCount;
			this.maxTextLength = maxTextLength;
			this.maxEntityLength = maxEntityLength;
		}
	}

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

	private static short CRLF = shortBytes("\r\n");

	private static Supplier<HttpMethod> methodGet = () -> HttpMethod.GET;
	private static Supplier<HttpMethod> methodPut = () -> HttpMethod.PUT;
	private static Supplier<HttpMethod> methodPost = () -> HttpMethod.POST;
	private static Supplier<HttpMethod> methodHead = () -> HttpMethod.HEAD;
	private static Supplier<HttpMethod> methodConnect = () -> HttpMethod.CONNECT;
	private static Supplier<HttpMethod> methodOptions = () -> HttpMethod.OPTIONS;
	private static Supplier<HttpMethod> methodDelete = () -> HttpMethod.DELETE;

	private static ScanTarget SPACE_TARGET = new ScanTarget(httpByte(" "));
	private static ScanTarget CR_TARGET = new ScanTarget(httpByte("\r"));
	private static ScanTarget COLON_TARGET = new ScanTarget(httpByte(":"));

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
	 * {@link HttpRequestParserMetaData}.
	 */
	private final HttpRequestParserMetaData metaData;

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
	 * {@link NonMaterialisedHeaders}.
	 */
	private NonMaterialisedHeadersImpl headers = null;

	/**
	 * {@link ByteSequence} for the HTTP entity.
	 */
	private ByteSequence entity = null;

	/**
	 * Initiate.
	 * 
	 * @param metaData
	 *            {@link HttpRequestParserMetaData}.
	 */
	public HttpRequestParser(HttpRequestParserMetaData metaData) {
		this.metaData = metaData;
	}

	/**
	 * Parses the {@link HttpRequest}.
	 * 
	 * @return <code>true</code> should the {@link HttpRequest} be parsed.
	 *         Otherwise, <code>false</code> if further data is required.
	 * @throws HttpException
	 *             If invalid {@link HttpRequest}.
	 */
	public boolean parse() throws HttpException {

		/*
		 * Read in long (8 bytes) to determine known methods. Note that all
		 * known methods are less than 8 bytes. Furthermore, all requests are at
		 * least 9 bytes ("M / V\n\r\n\r" = 9).
		 */
		long bytes = this.buildLong(() -> new Error("TODO implement"));
		if (bytes == -1) {
			return false; // require further bytes
		}

		// Look for same length most common methods first: "GET ", "PUT "
		long checkGetPut = bytes & GET_PUT_MASK;
		if (checkGetPut == GET_) {
			this.method = methodGet;
			this.skipBytes(4); // after space

		} else if (checkGetPut == PUT_) {
			this.method = methodPut;
			this.skipBytes(4); // after space

		} else {
			// Look for next most common methods: "POST ", "HEAD "
			long checkPostHead = bytes & POST_HEAD_MASK;
			if (checkPostHead == POST_) {
				this.method = methodPost;
				this.skipBytes(5); // after space

			} else if (checkPostHead == HEAD_) {
				this.method = methodHead;
				this.skipBytes(5); // after space

			} else {
				// Check next common: "CONNECT ", "OPTIONS "
				long checkConnectOptions = bytes & CONNECT_OPTIONS_MASK;
				if (checkConnectOptions == CONNECT_) {
					this.method = methodConnect;
					this.skipBytes(8); // after space

				} else if (checkConnectOptions == OPTIONS_) {
					this.method = methodOptions;
					this.skipBytes(8); // after space

				} else {
					// Check for remaining common: "DELETE "
					long checkDelete = bytes & DELETE_MASK;
					if (checkDelete == DELETE_) {
						this.method = methodDelete;
						this.skipBytes(7); // after space

					} else {
						// Custom method, so find the space
						final StreamBufferByteSequence methodSequence = this.scanToTarget(SPACE_TARGET,
								this.metaData.maxTextLength, () -> new Error("TODO implement"));
						if (methodSequence == null) {
							return false; // require further bytes
						} else {
							// Create method from byte sequence
							this.method = () -> new HttpMethod(methodSequence.toHttpString());
							this.skipBytes(1); // move past space
						}
					}
				}
			}
		}

		// Obtain the request URI
		final StreamBufferByteSequence uriSequence = this.scanToTarget(SPACE_TARGET, this.metaData.maxTextLength,
				() -> new Error("TODO implement"));
		if (uriSequence == null) {
			return false; // require further bytes
		}
		this.requestUri = () -> uriSequence.decodeUri((result) -> new Error()).toUriString((message) -> new Error());
		this.skipBytes(1); // skip the space

		// Obtain the version
		int crPosition = this.peekToTarget(CR_TARGET);
		if (crPosition == -1) {
			return false; // require further bytes
		}

		// Determine if common version
		final int commonVersionLength = 8; // "HTTP/1.X"
		if (crPosition == commonVersionLength) {
			long checkVersion = this.buildLong(() -> new Error("TODO implement"));
			if (checkVersion == HTTP_1_1) {
				this.version = HttpVersion.HTTP_1_1;
				this.skipBytes(commonVersionLength);

			} else if (checkVersion == HTTP_1_0) {
				this.version = HttpVersion.HTTP_1_0;
				this.skipBytes(commonVersionLength);
			}
		}

		// If no common version, create custom version
		if (this.version == null) {
			StreamBufferByteSequence versionSequence = this.scanBytes(crPosition);
			String httpVersionText = versionSequence.toHttpString();
			this.version = new HttpVersion(httpVersionText);
		}

		// Ensure line delimited by CRLF
		short eoln = this.buildShort(() -> new Error("TODO implement"));
		if (eoln == -1) {
			return false; // require further bytes
		}
		if (eoln != CRLF) {
			throw new HttpException(HttpStatus.BAD_REQUEST);
		}
		this.skipBytes(2); // CRLF

		// Load the headers
		this.headers = new NonMaterialisedHeadersImpl(16);

		// Capture content length
		long contentLength = 0;

		// Determine if end of headers
		short checkCrLf = this.buildShort(() -> new Error("TODO implement"));
		if (checkCrLf == -1) {
			return false; // require further bytes
		}
		while (checkCrLf != CRLF) {

			// Scan in the header name
			StreamBufferByteSequence headerName = this.scanToTarget(COLON_TARGET, this.metaData.maxTextLength,
					() -> new Error("TODO implement"));
			if (headerName == null) {
				return false; // require further bytes
			}
			this.skipBytes(1); // move past ':'

			// Always trim header name for comparisons
			headerName.trim();

			// Scan in the header value
			StreamBufferByteSequence headerValue = this.scanToTarget(CR_TARGET, this.metaData.maxTextLength,
					() -> new Error("TODO implement"));
			if (headerValue == null) {
				return false; // require further bytes
			}

			// Add the header
			this.headers.addHttpHeader(headerName, headerValue);

			// Determine if Content Length
			if (MaterialisingHttpRequestHeaders.httpEqualsIgnoreCase("content-length", headerName)) {
				// Obtain the content length
				contentLength = headerValue.trim().toLong((digit) -> new Error("TODO implement"));
			}

			// Ensure next character is LF (after CR)
			checkCrLf = this.buildShort(() -> new Error("TODO implement"));
			if (checkCrLf == -1) {
				return false; // require further bytes
			}
			if (checkCrLf != CRLF) {
				throw new HttpException(HttpStatus.BAD_REQUEST);
			}
			this.skipBytes(2); // CRLF

			// Obtain the potential CRLF
			checkCrLf = this.buildShort(() -> new Error("TODO implement"));
			if (checkCrLf == -1) {
				return false; // require further bytes
			}
		}
		this.skipBytes(2); // CRLF

		// Build entity of content length
		this.entity = this.scanBytes(contentLength);
		if (this.entity == null) {
			return false; // require further bytes
		}

		// As here, have HTTP request
		return true;
	}

	/**
	 * Obtains the {@link Supplier} of the {@link HttpMethod}.
	 * 
	 * @return {@link Supplier} of the {@link HttpMethod}.
	 */
	public Supplier<HttpMethod> getMethod() {
		return this.method;
	}

	/**
	 * Obtains the {@link Supplier} of the request URI.
	 * 
	 * @return {@link Supplier} of the request URI.
	 */
	public Supplier<String> getRequestURI() {
		return this.requestUri;
	}

	/**
	 * Obtains the {@link HttpVersion}.
	 * 
	 * @return {@link HttpVersion}.
	 */
	public HttpVersion getVersion() {
		return this.version;
	}

	/**
	 * Obtains the {@link NonMaterialisedHttpHeaders}.
	 * 
	 * @return {@link NonMaterialisedHttpHeaders}.
	 */
	public NonMaterialisedHttpHeaders getHeaders() {
		return this.headers;
	}

	/**
	 * Obtains the entity {@link ByteSequence}.
	 * 
	 * @return {@link ByteSequence} for the entity.
	 */
	public ByteSequence getEntity() {
		return this.entity;
	}

}