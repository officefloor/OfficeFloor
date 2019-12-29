/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.web.template.section;

import java.io.IOException;
import java.nio.charset.Charset;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.server.http.HttpResponse;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.stream.ServerWriter;
import net.officefloor.web.template.parse.ParsedTemplateSection;

/**
 * {@link ManagedFunction} to write the {@link ParsedTemplateSection}.
 * 
 * @author Daniel Sagenschneider
 */
public class WebTemplateFunction extends StaticManagedFunction<Indexed, None> {

	/**
	 * {@link WebTemplateWriter} instances to write the content.
	 */
	private final WebTemplateWriter[] contentWriters;

	/**
	 * Flag indicating if a bean.
	 */
	private final boolean isBean;

	/**
	 * Default {@link Charset} for the template.
	 */
	private final Charset defaultCharset;

	/**
	 * Initiate.
	 * 
	 * @param contentWriters {@link WebTemplateWriter} instances to write the
	 *                       content.
	 * @param isBean         Flag indicating if a bean.
	 * @param charset        Default {@link Charset} for the template.
	 */
	public WebTemplateFunction(WebTemplateWriter[] contentWriters, boolean isBean, Charset charset) {
		this.contentWriters = contentWriters;
		this.isBean = isBean;
		this.defaultCharset = charset;
	}

	/*
	 * ======================= ManagedFunction ================================
	 */

	@Override
	public void execute(ManagedFunctionContext<Indexed, None> context) throws IOException {

		// Obtain the bean dependency
		Object bean;
		if (this.isBean) {
			// Obtain the bean
			bean = context.getObject(1);

			// No bean, no content
			if (bean == null) {
				return;
			}

		} else {
			// No bean
			bean = null;
		}

		// Obtain the dependencies
		ServerHttpConnection connection = (ServerHttpConnection) context.getObject(0);

		// Obtain the writer
		HttpResponse response = connection.getResponse();
		ServerWriter writer = response.getEntityWriter();

		// Determine if using default char set
		boolean isDefaultCharset = (this.defaultCharset.name().equalsIgnoreCase(response.getContentCharset().name()));

		// Determine the template path
		String path = connection.getRequest().getUri();
		int endPathIndex = path.indexOf('?');
		if (endPathIndex >= 0) {
			path = path.substring(0, endPathIndex);
		}

		// Write the contents
		for (WebTemplateWriter contentWriter : this.contentWriters) {
			contentWriter.write(writer, isDefaultCharset, bean, connection, path);
		}
	}

}