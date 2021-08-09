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

package net.officefloor.web.resource.spi;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import net.officefloor.server.http.HttpHeaderValue;
import net.officefloor.web.resource.HttpFile;

/**
 * Context for the {@link ResourceTransformer}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ResourceTransformerContext {

	/**
	 * Obtains the path identifying the resource.
	 * 
	 * @return Path identifying the resource.
	 */
	String getPath();

	/**
	 * Obtains the {@link Path} to the resource being transformed.
	 * 
	 * @return {@link Path} to the resource being transformed.
	 */
	Path getResource();

	/**
	 * <p>
	 * Creates a new file.
	 * <p>
	 * All files required for transform should be created via this method. This is
	 * to ensure the files are managed.
	 * 
	 * @return {@link Path} to the new file.
	 * @throws IOException If fails to create the new file.
	 */
	Path createFile() throws IOException;

	/**
	 * Obtains the <code>Content-Type</code> of the resource.
	 * 
	 * @return <code>Content-Type</code> of the resource.
	 */
	String getContentType();

	/**
	 * <p>
	 * Obtains the {@link Charset} for the resource.
	 * <p>
	 * Typically this is the {@link Charset} of the backing {@link ResourceSystem}.
	 * However, it may be changed by a previous {@link ResourceTransformer}.
	 * 
	 * @return {@link Charset} for the resource.
	 */
	Charset getCharset();

	/**
	 * Allows specifying a new <code>Content-Type</code> for the transformed
	 * resource.
	 * 
	 * @param contentType <code>Content-Type</code> for the transformed resource.
	 *                    This needs to include the {@link Charset} parameter if
	 *                    required.
	 * @param charset     {@link Charset} for the {@link HttpFile}. May be
	 *                    <code>null</code> to use/reset to the default
	 *                    {@link Charset} of the {@link ResourceSystem}.
	 */
	void setContentType(HttpHeaderValue contentType, Charset charset);

	/**
	 * Obtains the <code>Content-Encoding</code> of the resource.
	 * 
	 * @return <code>Content-Encoding</code> of the resource. May be
	 *         <code>null</code> if resource not yet encoded.
	 */
	String getContentEncoding();

	/**
	 * Specifies the <code>Content-Encoding</code> for the transformed resource.
	 * 
	 * @param contentEncoding <code>Content-Encoding</code> for the transformed
	 *                        resource.
	 * @throws IOException If <code>Content-Encoding</code> already specified by
	 *                     another {@link ResourceTransformer}.
	 * 
	 * @see #getContentEncoding()
	 */
	void setContentEncoding(HttpHeaderValue contentEncoding) throws IOException;

	/**
	 * <p>
	 * Specifies the {@link Path} to the transformed resource.
	 * <p>
	 * This is optional to invoke. Should a transform not be applied, then this
	 * should not be invoked.
	 * 
	 * @param resource {@link Path} to the transformed resource.
	 */
	void setTransformedResource(Path resource);

}
