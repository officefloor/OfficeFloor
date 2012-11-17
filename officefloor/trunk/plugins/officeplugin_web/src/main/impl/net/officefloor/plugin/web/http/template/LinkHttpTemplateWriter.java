/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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

package net.officefloor.plugin.web.http.template;

import java.io.IOException;

import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.stream.ServerWriter;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.template.parse.LinkHttpTemplateSectionContent;

/**
 * {@link HttpTemplateWriter} to write the link.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkHttpTemplateWriter implements HttpTemplateWriter {

	/**
	 * Prefix for the link.
	 */
	private final String linkPrefix;

	/**
	 * Suffix for the link.
	 */
	private final String linkSuffix;

	/**
	 * Indicates if the link is to be submitted over a secure
	 * {@link ServerHttpConnection}.
	 */
	private final boolean isLinkSecure;

	/**
	 * Initiate.
	 * 
	 * @param content
	 *            {@link LinkHttpTemplateSectionContent}.
	 * @param isLinkSecure
	 *            Indicates if the link is to be submitted over a secure
	 *            {@link ServerHttpConnection}.
	 */
	public LinkHttpTemplateWriter(LinkHttpTemplateSectionContent content,
			boolean isLinkSecure) {
		this.linkPrefix = "/";
		this.linkSuffix = "-" + content.getName()
				+ HttpTemplateWorkSource.LINK_URL_EXTENSION;
		this.isLinkSecure = isLinkSecure;
	}

	/*
	 * ================== HttpTemplateWriter ===========================
	 */

	@Override
	public void write(ServerWriter writer, String workName, Object bean,
			HttpApplicationLocation location) throws IOException {

		// Strip / if root work
		if (workName.startsWith("/")) {
			workName = workName.substring("/".length());
		}

		// Obtain the link path
		String applicationLinkPath = this.linkPrefix + workName
				+ this.linkSuffix;
		String clientLinkPath = location.transformToClientPath(
				applicationLinkPath, this.isLinkSecure);

		// Write the content
		writer.write(clientLinkPath);
	}
}