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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import net.officefloor.plugin.socket.server.http.response.HttpResponseWriter;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.template.HttpTemplateWriter;
import net.officefloor.plugin.web.http.template.parse.StaticHttpTemplateSectionContent;

/**
 * {@link HttpTemplateWriter} to write static content.
 * 
 * @author Daniel Sagenschneider
 */
public class StaticHttpTemplateWriter implements HttpTemplateWriter {

	/**
	 * No <code>Content-Encoding</code>.
	 */
	private static final String NO_CONTENT_ENCODING = "";

	/**
	 * <code>Content-Type</code>.
	 */
	private final String contentType;

	/**
	 * {@link Charset}.
	 */
	private final Charset charset;

	/**
	 * Content to write to the {@link HttpResponseWriter}.
	 */
	private final ByteBuffer content;

	/**
	 * Initiate.
	 * 
	 * @param staticContent
	 *            {@link StaticHttpTemplateSectionContent} to write.
	 * @param contentType
	 *            <code>Content-Type</code> of the static content.
	 * @param charset
	 *            {@link Charset} to prepare static content.
	 * @throws IOException
	 *             If fails to prepare the static content.
	 */
	public StaticHttpTemplateWriter(
			StaticHttpTemplateSectionContent staticContent, String contentType,
			Charset charset) throws IOException {
		this.contentType = contentType;
		this.charset = charset;

		// Obtain the prepared data
		ByteArrayOutputStream dataBuffer = new ByteArrayOutputStream();
		Writer writer = new OutputStreamWriter(dataBuffer, charset);
		writer.write(staticContent.getStaticContent());
		writer.flush();
		byte[] data = dataBuffer.toByteArray();

		// Load the contents
		ByteBuffer buffer = ByteBuffer.allocateDirect(data.length);
		buffer.put(data);
		buffer.flip();

		// Specify prepared static content
		this.content = buffer.asReadOnlyBuffer();
	}

	/*
	 * ================ HttpTemplateWriter ===================
	 */

	@Override
	public void write(HttpResponseWriter writer, String workName, Object bean,
			HttpApplicationLocation location) throws IOException {

		// Duplicate to not move original buffer position when written
		ByteBuffer writeContent = this.content.duplicate();

		// Write the content
		writer.write(NO_CONTENT_ENCODING, this.contentType, this.charset,
				writeContent);
	}

}