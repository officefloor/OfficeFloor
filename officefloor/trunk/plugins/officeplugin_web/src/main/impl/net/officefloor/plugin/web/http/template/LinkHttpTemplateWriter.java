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
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationWorkSource;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.parse.LinkHttpTemplateSectionContent;

/**
 * {@link HttpTemplateWriter} to write the link.
 * 
 * @author Daniel Sagenschneider
 */
public class LinkHttpTemplateWriter implements HttpTemplateWriter {

	/**
	 * Obtains the {@link HttpTemplate} link URI path.
	 * 
	 * @param templateUriPath
	 *            {@link HttpTemplate} URI path.
	 * @param linkName
	 *            Name of the link.
	 * @param templateUriSuffix
	 *            {@link HttpTemplate} URI suffix. May be <code>null</code> for
	 *            no suffix.
	 * @return {@link HttpTemplate} link URI path.
	 * @throws InvalidHttpRequestUriException
	 *             Should the resulting URI be invalid.
	 */
	public static String getTemplateLinkUriPath(String templateUriPath,
			String linkName, String templateUriSuffix)
			throws InvalidHttpRequestUriException {

		// Create the link URI path
		String linkUriPath = templateUriPath + "-" + linkName
				+ (templateUriSuffix == null ? "" : templateUriSuffix);
		linkUriPath = HttpUrlContinuationWorkSource
				.getApplicationUriPath(linkUriPath);

		// Return the link URI path
		return linkUriPath;
	}

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
	 * @throws InvalidHttpRequestUriException
	 *             If the link URI path is invalid.
	 */
	public LinkHttpTemplateWriter(LinkHttpTemplateSectionContent content,
			String templateUriPath, String templateUriSuffix,
			boolean isLinkSecure) throws InvalidHttpRequestUriException {
		this.isLinkSecure = isLinkSecure;

		// Create the link URI path
		this.linkUriPath = getTemplateLinkUriPath(templateUriPath,
				content.getName(), templateUriSuffix);
	}

	/*
	 * ================== HttpTemplateWriter ===========================
	 */

	@Override
	public void write(ServerWriter writer, Object bean,
			HttpApplicationLocation location) throws IOException {

		// Obtain the link path
		String clientLinkPath = location.transformToClientPath(
				this.linkUriPath, this.isLinkSecure);

		// Write the content
		writer.write(clientLinkPath);
	}

}