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
import java.nio.charset.Charset;

import net.officefloor.plugin.stream.ServerWriter;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.template.parse.StaticHttpTemplateSectionContent;

/**
 * {@link HttpTemplateWriter} to write static content.
 * 
 * @author Daniel Sagenschneider
 */
public class StaticHttpTemplateWriter implements HttpTemplateWriter {

	/**
	 * Encoded content to write to the {@link ServerWriter}.
	 */
	private final byte[] encodedContent;

	/**
	 * Initiate.
	 * 
	 * @param staticContent
	 *            {@link StaticHttpTemplateSectionContent} to write.
	 * @param serverDefaultCharset
	 *            Default {@link Charset} for the Server.
	 * @throws IOException
	 *             If fails to prepare the static content.
	 */
	public StaticHttpTemplateWriter(
			StaticHttpTemplateSectionContent staticContent,
			Charset serverDefaultCharset) throws IOException {
		String content = staticContent.getStaticContent();

		// Pre-encode the static content for faster I/O
		this.encodedContent = content.getBytes(serverDefaultCharset);
	}

	/*
	 * ================ HttpTemplateWriter ===================
	 */

	@Override
	public void write(ServerWriter writer, Object bean,
			HttpApplicationLocation location) throws IOException {

		// Provide pre-encoded content
		writer.write(this.encodedContent);
	}

}