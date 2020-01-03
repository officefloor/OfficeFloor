/*-
 * #%L
 * CORS Tutorial
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

package net.officefloor.tutorial.corshttpserver;

import net.officefloor.plugin.section.clazz.Next;
import net.officefloor.server.http.HttpHeaderName;
import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponseHeaders;
import net.officefloor.server.http.ServerHttpConnection;

/**
 * CORS handling.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class Cors {

	private static HttpHeaderName ALLOW_ORIGIN = new HttpHeaderName("Access-Control-Allow-Origin");

	private static HttpHeaderName ALLOW_METHODS = new HttpHeaderName("Access-Control-Allow-Methods");

	private static HttpHeaderName ALLOW_HEADERS = new HttpHeaderName("Access-Control-Allow-Headers");

	public static HttpHeaderValue ALL = new HttpHeaderValue("*");

	@Next("service")
	public static void cors(ServerHttpConnection connection) {
		HttpResponseHeaders headers = connection.getResponse().getHeaders();
		headers.addHeader(ALLOW_ORIGIN, ALL);
		headers.addHeader(ALLOW_METHODS, ALL);
		headers.addHeader(ALLOW_HEADERS, ALL);
	}

}
// END SNIPPET: tutorial
