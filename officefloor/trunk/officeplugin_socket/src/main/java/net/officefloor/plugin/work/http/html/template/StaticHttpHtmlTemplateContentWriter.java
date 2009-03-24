/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.plugin.work.http.html.template;

import java.io.Writer;
import java.nio.ByteBuffer;

import net.officefloor.plugin.socket.server.http.api.HttpResponse;

/**
 * {@link HttpHtmlTemplateContentWriter} to write static content.
 * 
 * @author Daniel
 */
public class StaticHttpHtmlTemplateContentWriter implements
		HttpHtmlTemplateContentWriter {

	/**
	 * Content to write to the {@link HttpResponse}.
	 */
	private final ByteBuffer content;

	/**
	 * Initiate.
	 * 
	 * @param staticContent
	 *            Static content to write.
	 */
	public StaticHttpHtmlTemplateContentWriter(String staticContent) {
		// Create byte buffer to contain the data
		byte[] data = staticContent.getBytes();
		this.content = ByteBuffer.allocateDirect(data.length);
		this.content.put(data);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.officefloor.work.http.html.template.HttpHtmlTemplateContentWriter
	 * #writeContent(java.lang.Object, java.io.Writer,
	 * net.officefloor.plugin.socket.server.http.api.HttpResponse)
	 */
	@Override
	public void writeContent(Object bean, Writer httpBody,
			HttpResponse httpResponse) {
		httpResponse.appendToBody(this.content);
	}

}
