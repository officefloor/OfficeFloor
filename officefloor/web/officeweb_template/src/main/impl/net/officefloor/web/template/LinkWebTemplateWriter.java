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
package net.officefloor.web.template;

import java.io.IOException;

import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.web.template.parse.LinkParsedTemplateSectionContent;

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
	 * @param content
	 *            {@link LinkParsedTemplateSectionContent}.
	 * @param isLinkSecure
	 *            Indicates if the link is to be submitted over a secure
	 *            {@link ServerHttpConnection}.
	 */
	public LinkWebTemplateWriter(LinkParsedTemplateSectionContent content, boolean isLinkSecure) {
		this.isLinkSecure = isLinkSecure;
		this.linkPathSuffix = "-" + content.getName();
	}

	/*
	 * ================== WebTemplateWriter ===========================
	 */

	@Override
	public void write(ServerWriter writer, boolean isDefaultCharset, Object bean, ServerHttpConnection connection,
			String templatePath) throws HttpException {

		// Obtain the link path
		String clientLinkPath = connection.getServerLocation().createClientUrl(this.isLinkSecure, templatePath);

		try {
			// Write the content
			writer.write(clientLinkPath);
			writer.write(this.linkPathSuffix);

		} catch (IOException ex) {
			throw new HttpException(ex);
		}
	}

}