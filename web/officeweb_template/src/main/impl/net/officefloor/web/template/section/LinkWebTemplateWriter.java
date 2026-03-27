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

import java.io.IOException;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;

/**
 * {@link WebTemplateWriter} to write the link.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkWebTemplateWriter implements WebTemplateWriter {

	/**
	 * Indicates if the link is to be submitted over a secure
	 * {@link ServerHttpConnection}.
	 */
	private final boolean isLinkSecure;

	/**
	 * Suffix to the path for this link.
	 */
	private final String linkPathSuffix;

	/**
	 * Initiate.
	 * 
	 * @param linkName
	 *            Link name.
	 * @param isLinkSecure
	 *            Indicates if the link is to be submitted over a secure
	 *            {@link ServerHttpConnection}.
	 * @param linkSeparator
	 *            Link separator {@link Character}.
	 */
	public LinkWebTemplateWriter(String linkName, boolean isLinkSecure, char linkSeparator) {
		this.isLinkSecure = isLinkSecure;
		this.linkPathSuffix = String.valueOf(linkSeparator) + linkName;
	}

	/*
	 * ================== WebTemplateWriter ===========================
	 */

	@Override
	public void write(ServerWriter writer, boolean isDefaultCharset, Object bean, ServerHttpConnection connection,
			String templatePath) throws HttpException {

		// Obtain the link path (determining if require secure link)
		if (this.isLinkSecure && (!connection.isSecure())) {
			templatePath = connection.getServerLocation().createClientUrl(this.isLinkSecure, templatePath);
		}

		try {
			// Write the content
			writer.write(templatePath);
			writer.write(this.linkPathSuffix);

		} catch (IOException ex) {
			throw new HttpException(ex);
		}
	}

}
