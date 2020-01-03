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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.Charset;

/**
 * {@link HttpRequest} entity utilities.
 * 
 * @author Daniel Sagenschneider
 */
public class EntityUtil {

	/**
	 * Obtains the HTTP entity content from the {@link HttpRequest}.
	 * 
	 * @param request {@link HttpRequest}.
	 * @param charset {@link Charset}. May be <code>null</code> to use default
	 *                {@link Charset}.
	 * @return HTTP entity content.
	 * @throws HttpException If fails to obtain entity content.
	 */
	public static String toString(HttpRequest request, Charset charset) throws HttpException {

		// Ensure have charset
		if (charset == null) {
			charset = ServerHttpConnection.DEFAULT_HTTP_ENTITY_CHARSET;
		}

		// Obtain the content
		StringWriter content = new StringWriter();
		try {
			InputStreamReader reader = new InputStreamReader(request.getEntity().createBrowseInputStream(), charset);
			for (int character = reader.read(); character != -1; character = reader.read()) {
				content.write(character);
			}
		} catch (IOException ex) {
			throw new HttpException(ex);
		}

		// Return the content
		return content.toString();
	}

}
