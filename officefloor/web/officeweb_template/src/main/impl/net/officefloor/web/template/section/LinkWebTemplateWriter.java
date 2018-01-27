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