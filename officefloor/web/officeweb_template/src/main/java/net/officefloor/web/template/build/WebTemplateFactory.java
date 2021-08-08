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

package net.officefloor.web.template.build;

import java.io.Reader;

import net.officefloor.server.http.ServerHttpConnection;

/**
 * Factory for the creation of a {@link WebTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebTemplateFactory {

	/**
	 * Adds a {@link WebTemplate}.
	 * 
	 * @param isSecure
	 *            Indicates if requires secure {@link ServerHttpConnection} to
	 *            render the {@link WebTemplate}.
	 * @param applicationPath
	 *            Application path to the {@link WebTemplate}. May contain path
	 *            parameters.
	 * @param templateContent
	 *            {@link Reader} to the template content.
	 * @return {@link WebTemplate}.
	 */
	WebTemplate addTemplate(boolean isSecure, String applicationPath, Reader templateContent);

	/**
	 * Adds a {@link WebTemplate}.
	 * 
	 * @param isSecure
	 *            Indicates if requires secure {@link ServerHttpConnection} to
	 *            render the {@link WebTemplate}.
	 * @param applicationPath
	 *            Application path to the {@link WebTemplate}. May contain path
	 *            parameters.
	 * @param locationOfTemplate
	 *            Location of the template content.
	 * @return {@link WebTemplate}.
	 */
	WebTemplate addTemplate(boolean isSecure, String applicationPath, String locationOfTemplate);

}
