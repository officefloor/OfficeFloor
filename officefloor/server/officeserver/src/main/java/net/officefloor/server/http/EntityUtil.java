/*-
 * #%L
 * HTTP Server
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
