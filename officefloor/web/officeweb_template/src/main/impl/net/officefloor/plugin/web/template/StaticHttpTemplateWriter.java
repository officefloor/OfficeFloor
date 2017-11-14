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
package net.officefloor.plugin.web.template;

import java.io.IOException;
import java.nio.charset.Charset;

import net.officefloor.plugin.web.template.HttpTemplateWriter;
import net.officefloor.plugin.web.template.parse.StaticHttpTemplateSectionContent;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.web.state.HttpApplicationState;

/**
 * {@link HttpTemplateWriter} to write static content.
 * 
 * @author Daniel Sagenschneider
 */
public class StaticHttpTemplateWriter implements HttpTemplateWriter {

	/**
	 * Static content as text.
	 */
	private final String textContent;

	/**
	 * Encoded content to write to the {@link ServerWriter}.
	 */
	private final byte[] encodedContent;

	/**
	 * Initiate.
	 * 
	 * @param staticContent
	 *            {@link StaticHttpTemplateSectionContent} to write.
	 * @param charset
	 *            {@link Charset} for the template.
	 * @throws IOException
	 *             If fails to prepare the static content.
	 */
	public StaticHttpTemplateWriter(StaticHttpTemplateSectionContent staticContent, Charset charset)
			throws IOException {
		this.textContent = staticContent.getStaticContent();

		// Pre-encode the static content for faster I/O
		this.encodedContent = this.textContent.getBytes(charset);
	}

	/*
	 * ================ HttpTemplateWriter ===================
	 */

	@Override
	public void write(ServerWriter writer, boolean isDefaultCharset, Object bean, ServerHttpConnection connection,
			HttpApplicationState applicationState) throws IOException {

		// Use pre-encoded content if using default charset
		if (isDefaultCharset) {
			// Provide pre-encoded content
			writer.write(this.encodedContent);

		} else {
			// Provide the content (with appropriate charset)
			writer.write(this.textContent);
		}
	}

}