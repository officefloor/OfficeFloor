/*-
 * #%L
 * Web Template
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

package net.officefloor.web.template.section;

import java.nio.charset.Charset;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.web.template.build.WebTemplate;

/**
 * Interface to write the template content to {@link ServerWriter}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplateWriter {

	/**
	 * Writes the template content to the {@link ServerWriter}.
	 * 
	 * @param writer
	 *            {@link ServerWriter} to receive the template content.
	 * @param isDefaultCharset
	 *            Indicates if the default {@link Charset} for outputting the
	 *            template is being used. While the {@link Charset} may be
	 *            programmatically changed, it is expected in the majority of
	 *            cases to be using the default {@link Charset} configured for
	 *            the template. This flag allows static content to be cached in
	 *            bytes (using default {@link Charset}) for improved
	 *            performance.
	 * @param bean
	 *            Bean to potentially obtain data. May be <code>null</code> if
	 *            template contents does not require a bean.
	 * @param connection
	 *            {@link ServerHttpConnection}.
	 * @param templatePath
	 *            Current path for the {@link WebTemplate}. As the path may be
	 *            dynamic (contain path parameters), this is the path to be used
	 *            by links in rending of the {@link WebTemplate}.
	 * @throws HttpException
	 *             If fails to write content.
	 */
	void write(ServerWriter writer, boolean isDefaultCharset, Object bean, ServerHttpConnection connection,
			String templatePath) throws HttpException;

}
