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

	private static final long GET_PUT_MASK = longMask("GET ");
	private static final long POST_HEAD_MASK = longMask("POST ");
	private static final long CONNECT_OPTIONS_MASK = longMask("CONNECT ");
	private static final long DELETE_MASK = longMask("DELETE ");

	private static final long GET_ = longBytes("GET ");
	private static final long PUT_ = longBytes("PUT ");
	private static final long POST_ = longBytes("POST ");
	private static final long HEAD_ = longBytes("HEAD ");
	private static final long CONNECT_ = longBytes("CONNECT ");
	private static final long OPTIONS_ = longBytes("OPTIONS ");
	private static final long DELETE_ = longBytes("DELETE ");

	private static final long HTTP_1_0 = longBytes("HTTP/1.0");
	private static final long HTTP_1_1 = longBytes("HTTP/1.1");

	private static final short CRLF = shortBytes("\r\n");

	private static final Supplier<HttpMethod> methodGet = () -> HttpMethod.GET;
	private static final Supplier<HttpMethod> methodPut = () -> HttpMethod.PUT;
	private static final Supplier<HttpMethod> methodPost = () -> HttpMethod.POST;
	private static final Supplier<HttpMethod> methodHead = () -> HttpMethod.HEAD;
	private static final Supplier<HttpMethod> methodConnect = () -> HttpMethod.CONNECT;
	private static final Supplier<HttpMethod> methodOptions = () -> HttpMethod.OPTIONS;
	private static final Supplier<HttpMethod> methodDelete = () -> HttpMethod.DELETE;

	private static final byte HTTP_SPACE = httpByte(" ");
	private static final byte HTTP_TAB = httpByte("\t");
	private static final ScanTarget SPACE_TARGET = new ScanTarget(HTTP_SPACE);
	private static final ScanTarget CR_TARGET = new ScanTarget(httpByte("\r"));
	private static final ScanTarget COLON_TARGET = new ScanTarget(httpByte(":"));

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
	 * Indicates if the value represents a HTTP white space.
	 * 
	 * @param value
	 *            Value to check if HTTP white space.
	 * @return <code>true</code> if value is HTTP white space.
	 */
	private static boolean isWhiteSpace(byte value) {
		return (value == HTTP_SPACE) || (value == HTTP_TAB);
	}

	/**
	 * {@link HttpRequestParserMetaData}.
	 */
	private final HttpRequestParserMetaData metaData;

	/**
	 * State of parsing.
	 */
	private static enum ParseState {
		NEW_REQUEST
	}

	private ParseState state = ParseState.NEW_REQUEST;

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

		// Handle based on state
		switch (this.state) {
		case NEW_REQUEST:

			// Reset for new request
			this.method = null;
			this.requestUri = null;
			this.version = null;
			this.headers = null;
			this.entity = null;

			// Determine if separating CRLF
			short checkCrLf = this.buildShort(() -> new Error("TODO implement"));
			if (checkCrLf == -1) {
				return false; // require further bytes
			}
			while (checkCrLf == CRLF) {

				// Skip separating CRLF
				this.skipBytes(2);

				// Read in the next short
				checkCrLf = this.buildShort(() -> new Error("TODO implement"));
				if (checkCrLf == -1) {
					return false; // require further bytes
				}
			}

			// Ensure first character is not white spacing
			byte character = (byte) ((checkCrLf & 0xff00) >> 8);
			if (isWhiteSpace(character)) {
				throw new HttpException(
						new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(), "Leading spaces for request invalid"));
			}

			/*
			 * Read in long (8 bytes) to determine known methods. Note that all
			 * known methods are less than 8 bytes. Furthermore, all requests
			 * are at least 9 bytes ("M / V\n\r\n\r" = 9).
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
									this.metaData.maxTextLength, () -> new HttpException(
											new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(), "Method too long")));
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
					() -> new HttpException(HttpStatus.REQUEST_URI_TOO_LARGE));
			if (uriSequence == null) {
				return false; // require further bytes
			}
			this.requestUri = () -> uriSequence
					.decodeUri((result) -> new HttpException(
							new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(), result + " for URI")))
					.toUriString((message) -> new Error());
			this.skipBytes(1); // skip the space

			// Determine if common version
			int crPosition = this.peekToTarget(CR_TARGET);
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
				StreamBufferByteSequence versionSequence = this.scanToTarget(CR_TARGET, this.metaData.maxTextLength,
						() -> new HttpException(
								new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(), "Version too long")));
				if (versionSequence == null) {
					return false; // require further bytes
				}
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
			checkCrLf = this.buildShort(() -> new Error("TODO implement"));
			if (checkCrLf == -1) {
				return false; // require further bytes
			}
			while (checkCrLf != CRLF) {

				// Ensure not too many headers
				if (this.headers.length() >= this.metaData.maxHeaderCount) {
					throw new HttpException(new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(), "Too Many Headers"));
				}

				// Ensure no leading spacing around header name
				byte headerNameFirstCharacter = this.buildByte(() -> new Error("TODO implement"));
				if (isWhiteSpace(headerNameFirstCharacter)) {
					throw new HttpException(new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(),
							"White spacing before HTTP header name"));
				}

				// Scan in the header name
				StreamBufferByteSequence headerName = this.scanToTarget(COLON_TARGET, this.metaData.maxTextLength,
						() -> new HttpException(
								new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(), "Header name too long")));
				if (headerName == null) {
					return false; // require further bytes
				}
				this.skipBytes(1); // move past ':'

				// Ensure have header name
				if (headerName.length() == 0) {
					throw new HttpException(
							new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(), "Missing header name"));
				}

				// Trim possible trailing space to name
				headerName.trim();

				// Scan in the header value
				StreamBufferByteSequence headerValue = this.scanToTarget(CR_TARGET, this.metaData.maxTextLength,
						() -> new HttpException(
								new HttpStatus(HttpStatus.BAD_REQUEST.getStatusCode(), "Header value too long")));
				if (headerValue == null) {
					return false; // require further bytes
				}

				// Add the header
				this.headers.addHttpHeader(headerName, headerValue);

				// Determine if Content Length
				if (MaterialisingHttpRequestHeaders.httpEqualsIgnoreCase("content-length", headerName)) {
					// Obtain the content length
					headerValue.trim(); // remove spacing
					if (headerValue.length() == 0) {
						throw new HttpException(new HttpStatus(HttpStatus.LENGTH_REQUIRED.getStatusCode(),
								"Content-Length header value must be an integer"));
					}
					contentLength = headerValue.toLong(
							(digit) -> new HttpException(new HttpStatus(HttpStatus.LENGTH_REQUIRED.getStatusCode(),
									"Content-Length header value must be an integer")));
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

			// Determine if content length too long
			if (contentLength > this.metaData.maxEntityLength) {
				throw new HttpException(new HttpStatus(HttpStatus.REQUEST_ENTITY_TOO_LARGE.getStatusCode(),
						"Request entity must be less than maximum of " + this.metaData.maxEntityLength + " bytes"));
			}

			// Build entity of content length
			this.entity = this.scanBytes(contentLength);
			if (this.entity == null) {
				return false; // require further bytes
			}

			// Reset for new request
			this.state = ParseState.NEW_REQUEST;

			// Have the request
			return true;
		}

		// Should never get here
		throw new HttpException(new HttpStatus(HttpStatus.INTERNAL_SERVER_ERROR.getStatusCode(),
				"Invalid internal state in parsing request"));
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