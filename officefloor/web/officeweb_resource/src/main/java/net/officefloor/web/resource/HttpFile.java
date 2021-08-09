/*-
 * #%L
 * Web resources
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

package net.officefloor.web.resource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.spi.FileTypeDetector;

import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.web.resource.spi.ResourceSystem;
import net.officefloor.web.resource.spi.ResourceSystemContext;
import net.officefloor.web.resource.spi.ResourceTransformer;

/**
 * HTTP file.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpFile extends HttpResource {

	/**
	 * Obtains the <code>Content-Encoding</code> for this {@link HttpFile}.
	 * 
	 * @return <code>Content-Encoding</code> for this {@link HttpFile}.
	 * 
	 * @see ResourceTransformer
	 */
	HttpHeaderValue getContentEncoding();

	/**
	 * Obtains the <code>Content-Type</code> for this {@link HttpFile}.
	 * 
	 * @return <code>Content-Type</code> for this {@link HttpFile}.
	 * 
	 * @see FileTypeDetector
	 */
	HttpHeaderValue getContentType();

	/**
	 * Obtains the {@link Charset} for the contents.
	 * 
	 * @return {@link Charset} or <code>null</code> if contents are not text or
	 *         the {@link Charset} is unknown.
	 * 
	 * @see ResourceSystem
	 * @see ResourceSystemContext
	 */
	Charset getCharset();

	/**
	 * Writes the {@link HttpFile} to the {@link HttpResponse}.
	 * 
	 * @param response
	 *            {@link HttpResponse}.
	 * @throws IOException
	 *             If failure in writing the {@link HttpFile} to the
	 *             {@link HttpResponse}.
	 */
	void writeTo(HttpResponse response) throws IOException;

}
