/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http;

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
	public static final int _100 = 100;
	public static final int _101 = 101;

	/**
	 * 2xx status.
	 */
	public static final int _200 = 200;
	public static final int _201 = 201;
	public static final int _202 = 202;
	public static final int _203 = 203;
	public static final int _204 = 204;
	public static final int _205 = 205;
	public static final int _206 = 206;

	/**
	 * 3xx status.
	 */
	public static final int _300 = 300;
	public static final int _301 = 301;
	public static final int _302 = 302;
	public static final int _303 = 303;
	public static final int _304 = 304;
	public static final int _305 = 305;
	public static final int _307 = 307;

	/**
	 * 4xx status.
	 */
	public static final int _400 = 400;
	public static final int _401 = 401;
	public static final int _402 = 402;
	public static final int _403 = 403;
	public static final int _404 = 404;
	public static final int _405 = 405;
	public static final int _406 = 406;
	public static final int _407 = 407;
	public static final int _408 = 408;
	public static final int _409 = 409;
	public static final int _410 = 410;
	public static final int _411 = 411;
	public static final int _412 = 412;
	public static final int _413 = 413;
	public static final int _414 = 414;
	public static final int _415 = 415;
	public static final int _416 = 416;
	public static final int _417 = 417;

	/**
	 * 5xx status.
	 */
	public static final int _500 = 500;
	public static final int _501 = 501;
	public static final int _502 = 502;
	public static final int _503 = 503;
	public static final int _504 = 504;
	public static final int _505 = 505;

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
		statusMessages.put(_100, "Continue");
		statusMessages.put(_101, "Switching Protocols");

		// 2xx
		statusMessages.put(_200, "OK");
		statusMessages.put(_201, "Created");
		statusMessages.put(_202, "Accepted");
		statusMessages.put(_203, "Non-Authoritative Information");
		statusMessages.put(_204, "No Content");
		statusMessages.put(_205, "Reset Content");
		statusMessages.put(_206, "Partial Content");

		// 3xx
		statusMessages.put(_300, "Multiple Choices");
		statusMessages.put(_301, "Moved Permanently");
		statusMessages.put(_302, "Found");
		statusMessages.put(_303, "See Other");
		statusMessages.put(_304, "Not Modified");
		statusMessages.put(_305, "Use Proxy");
		statusMessages.put(_307, "Temporary Redirect");

		// 4xx
		statusMessages.put(_400, "Bad Request");
		statusMessages.put(_401, "Unauthorized");
		statusMessages.put(_402, "Payment Required");
		statusMessages.put(_403, "Forbidden");
		statusMessages.put(_404, "Not Found");
		statusMessages.put(_405, "Method Not Allowed");
		statusMessages.put(_406, "Not Acceptable");
		statusMessages.put(_407, "Proxy Authentication Required");
		statusMessages.put(_408, "Request Time-out");
		statusMessages.put(_409, "Conflict");
		statusMessages.put(_410, "Gone");
		statusMessages.put(_411, "Length Required");
		statusMessages.put(_412, "Precondition Failed");
		statusMessages.put(_413, "Request Entity Too Large");
		statusMessages.put(_414, "Request-URI Too Large");
		statusMessages.put(_415, "Unsupported Media Type");
		statusMessages.put(_416, "Requested range not satisfiable");
		statusMessages.put(_417, "Expectation Failed");

		// 5xx
		statusMessages.put(_500, "Internal Server Error");
		statusMessages.put(_501, "Not Implemented");
		statusMessages.put(_502, "Bad Gateway");
		statusMessages.put(_503, "Service Unavailable");
		statusMessages.put(_504, "Gateway Time-out");
		statusMessages.put(_505, "HTTP Version not supported");
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