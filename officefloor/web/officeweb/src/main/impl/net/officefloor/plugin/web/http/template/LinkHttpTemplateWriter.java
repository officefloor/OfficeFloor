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
package net.officefloor.plugin.web.http.template;

import java.io.IOException;

import net.officefloor.plugin.web.escalation.InvalidRequestUriHttpException;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.parse.LinkHttpTemplateSectionContent;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;

/**
 * {@link HttpTemplateWriter} to write the link.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkHttpTemplateWriter implements HttpTemplateWriter {

	/**
	 * Link URI path.
	 */
	private final String linkUriPath;

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
	 * @param templateUriPath
	 *            {@link HttpTemplate} URI path.
	 * @param templateUriSuffix
	 *            {@link HttpTemplate} URI suffix. May be <code>null</code> for
	 *            no suffix.
	 * @param isLinkSecure
	 *            Indicates if the link is to be submitted over a secure
	 *            {@link ServerHttpConnection}.
	 * @throws InvalidRequestUriHttpException
	 *             If the link URI path is invalid.
	 */
	public LinkHttpTemplateWriter(LinkHttpTemplateSectionContent content,
			String templateUriPath, String templateUriSuffix,
			boolean isLinkSecure) throws InvalidRequestUriHttpException {
		this.isLinkSecure = isLinkSecure;

		// Create the link URI path
		this.linkUriPath = HttpTemplateManagedFunctionSource
				.getHttpTemplateLinkUrlContinuationPath(templateUriPath,
						content.getName(), templateUriSuffix);
	}

	/*
	 * ================== HttpTemplateWriter ===========================
	 */

	@Override
	public void write(ServerWriter writer, boolean isDefaultCharset,
			Object bean, HttpApplicationLocation location) throws IOException {

		// Obtain the link path
		String clientLinkPath = location.transformToClientPath(
				this.linkUriPath, this.isLinkSecure);

		// Write the content
		writer.write(clientLinkPath);
	}

}