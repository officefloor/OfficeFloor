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
package net.officefloor.server.http.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Constants for HTTP.
 *
 * @author Daniel Sagenschneider
 */
public class HttpStatus {

	/**
	 * 1xx status.
	 */
	public static final int SC_CONTINUE = 100;
	public static final int SC_SWITCHING_PROTOCOLS = 101;

	/**
	 * 2xx status.
	 */
	public static final int SC_OK = 200;
	public static final int SC_CREATED = 201;
	public static final int SC_ACCEPTED = 202;
	public static final int SC_NON_AUTHORITATIVE_INFORMATION = 203;
	public static final int SC_NO_CONTENT = 204;
	public static final int SC_RESET_CONTENT = 205;
	public static final int SC_PARTIAL_CONTENT = 206;

	/**
	 * 3xx status.
	 */
	public static final int SC_MULTIPLE_CHOICES = 300;
	public static final int SC_MOVED_PERMANENTLY = 301;
	public static final int SC_FOUND = 302;
	public static final int SC_SEE_OTHER = 303;
	public static final int SC_NOT_MODIFIED = 304;
	public static final int SC_USE_PROXY = 305;
	public static final int SC_TEMPORARY_REDIRECT = 307;

	/**
	 * 4xx status.
	 */
	public static final int SC_BAD_REQUEST = 400;
	public static final int SC_UNAUTHORIZED = 401;
	public static final int SC_PAYMENT_REQUIRED = 402;
	public static final int SC_FORBIDDEN = 403;
	public static final int SC_NOT_FOUND = 404;
	public static final int SC_METHOD_NOT_ALLOWED = 405;
	public static final int SC_NOT_ACCEPTABLE = 406;
	public static final int SC_PROXY_AUTHENTICATION_REQUIRED = 407;
	public static final int SC_REQUEST_TIME_OUT = 408;
	public static final int SC_CONFLICT = 409;
	public static final int SC_GONE = 410;
	public static final int SC_LENGTH_REQUIRED = 411;
	public static final int SC_PRECONDITION_FAILED = 412;
	public static final int SC_REQUEST_ENTITY_TOO_LARGE = 413;
	public static final int SC_REQUEST_URI_TOO_LARGE = 414;
	public static final int SC_UNSUPPORTED_MEDIA_TYPE = 415;
	public static final int SC_REQUESTED_RANGE_NOT_SATISFIABLE = 416;
	public static final int SC_EXPECTATION_FAILED = 417;

	/**
	 * 5xx status.
	 */
	public static final int SC_INTERNAL_SERVER_ERROR = 500;
	public static final int SC_NOT_IMPLEMENTED = 501;
	public static final int SC_BAD_GATEWAY = 502;
	public static final int SC_SERVICE_UNAVAILABLE = 503;
	public static final int SC_GATEWAY_TIME_OUT = 504;
	public static final int SC_HTTP_VERSION_NOT_SUPPORTED = 505;

	/**
	 * Messages for the various status codes.
	 */
	private static final Map<Integer, String> statusMessages;

	/**
	 * Loads the status codes.
	 */
	static {
		statusMessages = new HashMap<Integer, String>();

		// 1xx
		statusMessages.put(SC_CONTINUE, "Continue");
		statusMessages.put(SC_SWITCHING_PROTOCOLS, "Switching Protocols");

		// 2xx
		statusMessages.put(SC_OK, "OK");
		statusMessages.put(SC_CREATED, "Created");
		statusMessages.put(SC_ACCEPTED, "Accepted");
		statusMessages.put(SC_NON_AUTHORITATIVE_INFORMATION,
				"Non-Authoritative Information");
		statusMessages.put(SC_NO_CONTENT, "No Content");
		statusMessages.put(SC_RESET_CONTENT, "Reset Content");
		statusMessages.put(SC_PARTIAL_CONTENT, "Partial Content");

		// 3xx
		statusMessages.put(SC_MULTIPLE_CHOICES, "Multiple Choices");
		statusMessages.put(SC_MOVED_PERMANENTLY, "Moved Permanently");
		statusMessages.put(SC_FOUND, "Found");
		statusMessages.put(SC_SEE_OTHER, "See Other");
		statusMessages.put(SC_NOT_MODIFIED, "Not Modified");
		statusMessages.put(SC_USE_PROXY, "Use Proxy");
		statusMessages.put(SC_TEMPORARY_REDIRECT, "Temporary Redirect");

		// 4xx
		statusMessages.put(SC_BAD_REQUEST, "Bad Request");
		statusMessages.put(SC_UNAUTHORIZED, "Unauthorized");
		statusMessages.put(SC_PAYMENT_REQUIRED, "Payment Required");
		statusMessages.put(SC_FORBIDDEN, "Forbidden");
		statusMessages.put(SC_NOT_FOUND, "Not Found");
		statusMessages.put(SC_METHOD_NOT_ALLOWED, "Method Not Allowed");
		statusMessages.put(SC_NOT_ACCEPTABLE, "Not Acceptable");
		statusMessages.put(SC_PROXY_AUTHENTICATION_REQUIRED,
				"Proxy Authentication Required");
		statusMessages.put(SC_REQUEST_TIME_OUT, "Request Time-out");
		statusMessages.put(SC_CONFLICT, "Conflict");
		statusMessages.put(SC_GONE, "Gone");
		statusMessages.put(SC_LENGTH_REQUIRED, "Length Required");
		statusMessages.put(SC_PRECONDITION_FAILED, "Precondition Failed");
		statusMessages.put(SC_REQUEST_ENTITY_TOO_LARGE,
				"Request Entity Too Large");
		statusMessages.put(SC_REQUEST_URI_TOO_LARGE, "Request-URI Too Large");
		statusMessages.put(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type");
		statusMessages.put(SC_REQUESTED_RANGE_NOT_SATISFIABLE,
				"Requested range not satisfiable");
		statusMessages.put(SC_EXPECTATION_FAILED, "Expectation Failed");

		// 5xx
		statusMessages.put(SC_INTERNAL_SERVER_ERROR, "Internal Server Error");
		statusMessages.put(SC_NOT_IMPLEMENTED, "Not Implemented");
		statusMessages.put(SC_BAD_GATEWAY, "Bad Gateway");
		statusMessages.put(SC_SERVICE_UNAVAILABLE, "Service Unavailable");
		statusMessages.put(SC_GATEWAY_TIME_OUT, "Gateway Time-out");
		statusMessages.put(SC_HTTP_VERSION_NOT_SUPPORTED,
				"HTTP Version not supported");
	}

	/**
	 * Obtains the default status message for the input status code.
	 *
	 * @param statusCode
	 *            Status code.
	 * @return Default status message for the status code.
	 */
	public static String getStatusMessage(int statusCode) {
		return statusMessages.get(statusCode);
	}

	/**
	 * Access only via static methods.
	 */
	private HttpStatus() {
	}

}