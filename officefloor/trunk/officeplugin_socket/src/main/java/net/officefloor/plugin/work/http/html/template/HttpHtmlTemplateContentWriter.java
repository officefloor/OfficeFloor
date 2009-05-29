/*
 * Office Floor, Application Server
 * Copyright (C) 2005-2009 Daniel Sagenschneider
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 */
package net.officefloor.plugin.work.http.html.template;

import java.io.IOException;
import java.io.Writer;

import net.officefloor.plugin.socket.server.http.api.HttpResponse;
import net.officefloor.plugin.work.http.HttpException;
import net.officefloor.plugin.work.http.html.template.parse.TemplateSection;

/**
 * Interface to write contents to {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpHtmlTemplateContentWriter {

	/**
	 * Writes the content to the {@link HttpResponse}.
	 * 
	 * @param bean
	 *            Bean to potentially obtain data. May be <code>null</code> if
	 *            {@link TemplateSection} does not require a bean.
	 * @param httpBody
	 *            {@link Writer} to the body of the {@link HttpResponse}. May be
	 *            <code>null</code> if {@link TemplateSection} does not require
	 *            a bean.
	 * @param httpResponse
	 *            {@link HttpResponse}.
	 * @throws IOException
	 *             If fails to write content.
	 * @throws HttpException
	 *             If internal server error in writing template content.
	 */
	void writeContent(Object bean, Writer httpBody, HttpResponse httpResponse)
			throws IOException, HttpException;
}
