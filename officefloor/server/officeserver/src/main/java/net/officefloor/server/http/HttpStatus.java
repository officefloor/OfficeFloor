/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.server.http;

import net.officefloor.server.stream.StreamBuffer;
import net.officefloor.server.stream.StreamBufferPool;

/**
 * HTTP status.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpStatus {

	// 1xx
	private static final int SC_CONTINUE = 100;
	private static final int SC_SWITCHING_PROTOCOLS = 101;

	// 2xx
	private static final int SC_OK = 200;
	private static final int SC_CREATED = 201;
	private static final int SC_ACCEPTED = 202;
	private static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;
	private static final int SC_NO_CONTENT = 204;
	private static final int SC_RESET_CONTENT = 205;
	private static final int SC_PARTIAL_CONTENT = 206;

	// 3xx
	private static final int SC_MULTIPLE_CHOICES = 300;
	private static final int SC_MOVED_PERMANENTLY = 301;
	private static final int SC_FOUND = 302;
	private static final int SC_SEE_OTHER = 303;
	private static final int SC_NOT_MODIFIED = 304;
	private static final int SC_USE_PROXY = 305;
	private static final int SC_TEMPORARY_REDIRECT = 307;

	// 4xx
	private static final int SC_BAD_REQUEST = 400;
	private static final int SC_UNAUTHORIZED = 401;
	private static final int SC_PAYMENT_REQUIRED = 402;
	private static final int SC_FORBIDDEN = 403;
	private static final int SC_NOT_FOUND = 404;
	private static final int SC_METHOD_NOT_ALLOWED = 405;
	private static final int SC_NOT_ACCEPTABLE = 406;
	private static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
	private static final int SC_REQUEST_TIME_OUT = 408;
	private static final int SC_CONFLICT = 409;
	private static final int SC_GONE = 410;
	private static final int SC_LENGTH_REQUIRED = 411;
	private static final int SC_PRECONDITION_FAILED = 412;
	private static final int SC_REQUEST_ENTITY_TOO_LARGE = 413;
	private static final int SC_REQUEST_URI_TOO_LARGE = 414;
	private static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
	private static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
	private static final int SC_EXPECTATION_FAILED = 417;

	// 5xx
	private static final int SC_INTERNAL_SERVER_ERROR = 500;
	private static final int SC_NOT_IMPLEMENTED = 501;
	private static final int SC_BAD_GATEWAY = 502;
	private static final int SC_SERVICE_UNAVAILABLE = 503;
	private static final int SC_GATEWAY_TIME_OUT = 504;
	private static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;

	/**
	 * Obtains the {@link HttpStatus} for the status code.
	 * 
	 * @param statusCode Status code.
	 * @return {@link HttpStatus}.
	 */
	public static HttpStatus getHttpStatus(int statusCode) {
		switch (statusCode) {

		// 1xx
		case SC_CONTINUE:
			return CONTINUE;
		case SC_SWITCHING_PROTOCOLS:
			return SWITCHING_PROTOCOLS;

		// 2xx
		case SC_OK:
			return OK;
		case SC_CREATED:
			return CREATED;
		case SC_ACCEPTED:
			return ACCEPTED;
		case SC_NON_AUTHORITATIVE_INFORMATION:
			return NON_AUTHORITATIVE_INFORMATION;
		case SC_NO_CONTENT:
			return NO_CONTENT;
		case SC_RESET_CONTENT:
			return RESET_CONTENT;
		case SC_PARTIAL_CONTENT:
			return PARTIAL_CONTENT;

		// 3xx
		case SC_MULTIPLE_CHOICES:
			return MULTIPLE_CHOICES;
		case SC_MOVED_PERMANENTLY:
			return MOVED_PERMANENTLY;
		case SC_FOUND:
			return FOUND;
		case SC_SEE_OTHER:
			return SEE_OTHER;
		case SC_NOT_MODIFIED:
			return NOT_MODIFIED;
		case SC_USE_PROXY:
			return USE_PROXY;
		case SC_TEMPORARY_REDIRECT:
			return TEMPORARY_REDIRECT;

		// 4xx
		case SC_BAD_REQUEST:
			return BAD_REQUEST;
		case SC_UNAUTHORIZED:
			return UNAUTHORIZED;
		case SC_PAYMENT_REQUIRED:
			return PAYMENT_REQUIRED;
		case SC_FORBIDDEN:
			return FORBIDDEN;
		case SC_NOT_FOUND:
			return NOT_FOUND;
		case SC_METHOD_NOT_ALLOWED:
			return METHOD_NOT_ALLOWED;
		case SC_NOT_ACCEPTABLE:
			return NOT_ACCEPTABLE;
		case SC_PROXY_AUTHENTICATION_REQUIRED:
			return PROXY_AUTHENTICATION_REQUIRED;
		case SC_REQUEST_TIME_OUT:
			return REQUEST_TIME_OUT;
		case SC_CONFLICT:
			return CONFLICT;
		case SC_GONE:
			return GONE;
		case SC_LENGTH_REQUIRED:
			return LENGTH_REQUIRED;
		case SC_PRECONDITION_FAILED:
			return PRECONDITION_FAILED;
		case SC_REQUEST_ENTITY_TOO_LARGE:
			return REQUEST_ENTITY_TOO_LARGE;
		case SC_REQUEST_URI_TOO_LARGE:
			return REQUEST_URI_TOO_LARGE;
		case SC_UNSUPPORTED_MEDIA_TYPE:
			return UNSUPPORTED_MEDIA_TYPE;
		case SC_REQUESTED_RANGE_NOT_SATISFIABLE:
			return REQUESTED_RANGE_NOT_SATISFIABLE;
		case SC_EXPECTATION_FAILED:
			return EXPECTATION_FAILED;

		// 5xx
		case SC_INTERNAL_SERVER_ERROR:
			return INTERNAL_SERVER_ERROR;
		case SC_NOT_IMPLEMENTED:
			return NOT_IMPLEMENTED;
		case SC_BAD_GATEWAY:
			return BAD_GATEWAY;
		case SC_SERVICE_UNAVAILABLE:
			return SERVICE_UNAVAILABLE;
		case SC_GATEWAY_TIME_OUT:
			return GATEWAY_TIME_OUT;
		case SC_HTTP_VERSION_NOT_SUPPORTED:
			return HTTP_VERSION_NOT_SUPPORTED;

		default:
			// Non standard status
			return new HttpStatus(statusCode, "");
		}
	}

	/**
	 * {@link HttpStatus} {@link Enum}.
	 */
	public enum HttpStatusEnum {

		/**
		 * 1xx
		 */
		CONTINUE(SC_CONTINUE, "Continue"),

		SWITCHING_PROTOCOLS(SC_SWITCHING_PROTOCOLS, "Switching Protocols"),

		/**
		 * 2xx
		 */
		OK(SC_OK, "OK"),

		CREATED(SC_CREATED, "Created"),

		ACCEPTED(SC_ACCEPTED, "Accepted"),

		NON_AUTHORITATIVE_INFORMATION(SC_NON_AUTHORITATIVE_INFORMATION, "Non-Authoritative Information"),

		NO_CONTENT(SC_NO_CONTENT, "No Content"),

		RESET_CONTENT(SC_RESET_CONTENT, "Reset Content"),

		PARTIAL_CONTENT(SC_PARTIAL_CONTENT, "Partial Content"),

		/**
		 * 3xx
		 */
		MULTIPLE_CHOICES(SC_MULTIPLE_CHOICES, "Multiple Choices"),

		MOVED_PERMANENTLY(SC_MOVED_PERMANENTLY, "Moved Permanently"),

		FOUND(SC_FOUND, "Found"),

		SEE_OTHER(SC_SEE_OTHER, "See Other"),

		NOT_MODIFIED(SC_NOT_MODIFIED, "Not Modified"),

		USE_PROXY(SC_USE_PROXY, "Use Proxy"),

		TEMPORARY_REDIRECT(SC_TEMPORARY_REDIRECT, "Temporary Redirect"),

		/**
		 * 4xx
		 */
		BAD_REQUEST(SC_BAD_REQUEST, "Bad Request"),

		UNAUTHORIZED(SC_UNAUTHORIZED, "Unauthorized"),

		PAYMENT_REQUIRED(SC_PAYMENT_REQUIRED, "Payment Required"),

		FORBIDDEN(SC_FORBIDDEN, "Forbidden"),

		NOT_FOUND(SC_NOT_FOUND, "Not Found"),

		METHOD_NOT_ALLOWED(SC_METHOD_NOT_ALLOWED, "Method Not Allowed"),

		NOT_ACCEPTABLE(SC_NOT_ACCEPTABLE, "Not Acceptable"),

		PROXY_AUTHENTICATION_REQUIRED(SC_PROXY_AUTHENTICATION_REQUIRED, "Proxy Authentication Required"),

		REQUEST_TIME_OUT(SC_REQUEST_TIME_OUT, "Request Time-out"),

		CONFLICT(SC_CONFLICT, "Conflict"),

		GONE(SC_GONE, "Gone"),

		LENGTH_REQUIRED(SC_LENGTH_REQUIRED, "Length Required"),

		PRECONDITION_FAILED(SC_PRECONDITION_FAILED, "Precondition Failed"),

		REQUEST_ENTITY_TOO_LARGE(SC_REQUEST_ENTITY_TOO_LARGE, "Request Entity Too Large"),

		REQUEST_URI_TOO_LARGE(SC_REQUEST_URI_TOO_LARGE, "Request-URI Too Large"),

		UNSUPPORTED_MEDIA_TYPE(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type"),

		REQUESTED_RANGE_NOT_SATISFIABLE(SC_REQUESTED_RANGE_NOT_SATISFIABLE, "Requested range not satisfiable"),

		EXPECTATION_FAILED(SC_EXPECTATION_FAILED, "Expectation Failed"),

		/**
		 * 5xx
		 */
		INTERNAL_SERVER_ERROR(SC_INTERNAL_SERVER_ERROR, "Internal Server Error"),

		NOT_IMPLEMENTED(SC_NOT_IMPLEMENTED, "Not Implemented"),

		BAD_GATEWAY(SC_BAD_GATEWAY, "Bad Gateway"),

		SERVICE_UNAVAILABLE(SC_SERVICE_UNAVAILABLE, "Service Unavailable"),

		GATEWAY_TIME_OUT(SC_GATEWAY_TIME_OUT, "Gateway Time-out"),

		HTTP_VERSION_NOT_SUPPORTED(SC_HTTP_VERSION_NOT_SUPPORTED, "HTTP Version not supported"),

		/**
		 * Non standard {@link HttpStatus}.
		 */
		OTHER(-1, "Unknown");

		/**
		 * {@link HttpStatus}.
		 */
		private final HttpStatus httpStatus;

		/**
		 * Instantiate.
		 * 
		 * @param httpVersionName Name of the {@link HttpVersion}.
		 */
		private HttpStatusEnum(int status, String statusMessage) {
			this.httpStatus = new HttpStatus(status, statusMessage, this);
		}

		/**
		 * <p>
		 * Obtains the singleton {@link HttpStatus} for this {@link HttpStatusEnum}.
		 * <p>
		 * Note for {@link HttpStatusEnum#OTHER} this returns <code>null</code>.
		 * 
		 * @return {@link HttpVersion}.
		 */
		public HttpStatus getHttpStatus() {
			return this.httpStatus;
		}
	}

	// 1xx
	public static final HttpStatus CONTINUE = HttpStatusEnum.CONTINUE.getHttpStatus();
	public static final HttpStatus SWITCHING_PROTOCOLS = HttpStatusEnum.SWITCHING_PROTOCOLS.getHttpStatus();

	// 2xx
	public static final HttpStatus OK = HttpStatusEnum.OK.getHttpStatus();
	public static final HttpStatus CREATED = HttpStatusEnum.CREATED.getHttpStatus();
	public static final HttpStatus ACCEPTED = HttpStatusEnum.ACCEPTED.getHttpStatus();
	public static final HttpStatus NON_AUTHORITATIVE_INFORMATION = HttpStatusEnum.NON_AUTHORITATIVE_INFORMATION
			.getHttpStatus();
	public static final HttpStatus NO_CONTENT = HttpStatusEnum.NO_CONTENT.getHttpStatus();
	public static final HttpStatus RESET_CONTENT = HttpStatusEnum.RESET_CONTENT.getHttpStatus();
	public static final HttpStatus PARTIAL_CONTENT = HttpStatusEnum.PARTIAL_CONTENT.getHttpStatus();

	// 3xx
	public static final HttpStatus MULTIPLE_CHOICES = HttpStatusEnum.MULTIPLE_CHOICES.getHttpStatus();
	public static final HttpStatus MOVED_PERMANENTLY = HttpStatusEnum.MOVED_PERMANENTLY.getHttpStatus();
	public static final HttpStatus FOUND = HttpStatusEnum.FOUND.getHttpStatus();
	public static final HttpStatus SEE_OTHER = HttpStatusEnum.SEE_OTHER.getHttpStatus();
	public static final HttpStatus NOT_MODIFIED = HttpStatusEnum.NOT_MODIFIED.getHttpStatus();
	public static final HttpStatus USE_PROXY = HttpStatusEnum.USE_PROXY.getHttpStatus();
	public static final HttpStatus TEMPORARY_REDIRECT = HttpStatusEnum.TEMPORARY_REDIRECT.getHttpStatus();

	// 4xx
	public static final HttpStatus BAD_REQUEST = HttpStatusEnum.BAD_REQUEST.getHttpStatus();
	public static final HttpStatus UNAUTHORIZED = HttpStatusEnum.UNAUTHORIZED.getHttpStatus();
	public static final HttpStatus PAYMENT_REQUIRED = HttpStatusEnum.PAYMENT_REQUIRED.getHttpStatus();
	public static final HttpStatus FORBIDDEN = HttpStatusEnum.FORBIDDEN.getHttpStatus();
	public static final HttpStatus NOT_FOUND = HttpStatusEnum.NOT_FOUND.getHttpStatus();
	public static final HttpStatus METHOD_NOT_ALLOWED = HttpStatusEnum.METHOD_NOT_ALLOWED.getHttpStatus();
	public static final HttpStatus NOT_ACCEPTABLE = HttpStatusEnum.NOT_ACCEPTABLE.getHttpStatus();
	public static final HttpStatus PROXY_AUTHENTICATION_REQUIRED = HttpStatusEnum.PROXY_AUTHENTICATION_REQUIRED
			.getHttpStatus();
	public static final HttpStatus REQUEST_TIME_OUT = HttpStatusEnum.REQUEST_TIME_OUT.getHttpStatus();
	public static final HttpStatus CONFLICT = HttpStatusEnum.CONFLICT.getHttpStatus();
	public static final HttpStatus GONE = HttpStatusEnum.GONE.getHttpStatus();
	public static final HttpStatus LENGTH_REQUIRED = HttpStatusEnum.LENGTH_REQUIRED.getHttpStatus();
	public static final HttpStatus PRECONDITION_FAILED = HttpStatusEnum.PRECONDITION_FAILED.getHttpStatus();
	public static final HttpStatus REQUEST_ENTITY_TOO_LARGE = HttpStatusEnum.REQUEST_ENTITY_TOO_LARGE.getHttpStatus();
	public static final HttpStatus REQUEST_URI_TOO_LARGE = HttpStatusEnum.REQUEST_URI_TOO_LARGE.getHttpStatus();
	public static final HttpStatus UNSUPPORTED_MEDIA_TYPE = HttpStatusEnum.UNSUPPORTED_MEDIA_TYPE.getHttpStatus();
	public static final HttpStatus REQUESTED_RANGE_NOT_SATISFIABLE = HttpStatusEnum.REQUESTED_RANGE_NOT_SATISFIABLE
			.getHttpStatus();
	public static final HttpStatus EXPECTATION_FAILED = HttpStatusEnum.EXPECTATION_FAILED.getHttpStatus();

	// 5xx
	public static final HttpStatus INTERNAL_SERVER_ERROR = HttpStatusEnum.INTERNAL_SERVER_ERROR.getHttpStatus();
	public static final HttpStatus NOT_IMPLEMENTED = HttpStatusEnum.NOT_IMPLEMENTED.getHttpStatus();
	public static final HttpStatus BAD_GATEWAY = HttpStatusEnum.BAD_GATEWAY.getHttpStatus();
	public static final HttpStatus SERVICE_UNAVAILABLE = HttpStatusEnum.SERVICE_UNAVAILABLE.getHttpStatus();
	public static final HttpStatus GATEWAY_TIME_OUT = HttpStatusEnum.GATEWAY_TIME_OUT.getHttpStatus();
	public static final HttpStatus HTTP_VERSION_NOT_SUPPORTED = HttpStatusEnum.HTTP_VERSION_NOT_SUPPORTED
			.getHttpStatus();

	/**
	 * Status code.
	 */
	private final int statusCode;

	/**
	 * Status message.
	 */
	private final String statusMessage;

	/**
	 * HTTP encoded content for this {@link HttpStatus}.
	 */
	private final byte[] byteContent;

	/**
	 * {@link HttpStatusEnum}.
	 */
	private final HttpStatusEnum httpStatusEnum;

	/**
	 * Instantiate a dynamic {@link HttpStatus}.
	 * 
	 * @param statusCode    Status code.
	 * @param statusMessage Status message.
	 */
	public HttpStatus(int statusCode, String statusMessage) {
		this(statusCode, statusMessage, HttpStatusEnum.OTHER);
	}

	/**
	 * Instantiate.
	 * 
	 * @param statusCode     Status code.
	 * @param statusMessage  Status message.
	 * @param httpStatusEnum {@link HttpStatusEnum}.
	 */
	private HttpStatus(int statusCode, String statusMessage, HttpStatusEnum httpStatusEnum) {
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
		this.byteContent = (this.statusCode + " " + this.statusMessage).getBytes(ServerHttpConnection.HTTP_CHARSET);
		this.httpStatusEnum = httpStatusEnum;
	}

	/**
	 * Equals without the type checking.
	 * 
	 * @param httpStatus {@link HttpStatus}.
	 * @return <code>true</code> if same {@link HttpStatus}.
	 */
	public boolean isEqual(HttpStatus httpStatus) {
		return this.statusCode == httpStatus.statusCode;
	}

	/**
	 * Obtains the {@link HttpStatusEnum} for this {@link HttpVersion}.
	 * 
	 * @return {@link HttpStatusEnum}.
	 */
	public HttpStatusEnum getEnum() {
		return this.httpStatusEnum;
	}

	/**
	 * Obtains the status code.
	 * 
	 * @return Status code.
	 */
	public int getStatusCode() {
		return this.statusCode;
	}

	/**
	 * Obtains the status message.
	 * 
	 * @return Status message.
	 */
	public String getStatusMessage() {
		return this.statusMessage;
	}

	/**
	 * Writes this {@link HttpStatus} to the {@link StreamBuffer}.
	 * 
	 * @param <B>        Buffer type.
	 * @param head       Head {@link StreamBuffer} of the linked list of
	 *                   {@link StreamBuffer} instances.
	 * @param bufferPool {@link StreamBufferPool}.
	 */
	public <B> void write(StreamBuffer<B> head, StreamBufferPool<B> bufferPool) {
		StreamBuffer.write(this.byteContent, head, bufferPool);
	}

	/*
	 * ================= Object ===============
	 */

	@Override
	public int hashCode() {
		return this.statusCode;
	}

	@Override
	public boolean equals(Object obj) {

		// Determine if appropriate type
		if (!(obj instanceof HttpStatus)) {
			return false;
		}
		HttpStatus that = (HttpStatus) obj;

		// Determine if equal
		return this.isEqual(that);
	}

	@Override
	public String toString() {
		return this.statusCode + " " + this.statusMessage;
	}

}
