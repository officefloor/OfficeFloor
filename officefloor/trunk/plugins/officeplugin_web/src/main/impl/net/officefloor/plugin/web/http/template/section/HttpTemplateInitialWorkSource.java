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
package net.officefloor.plugin.web.http.template.section;

import java.io.IOException;
import java.nio.charset.Charset;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationDifferentiatorImpl;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.template.HttpTemplateWorkSource;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;
import net.officefloor.plugin.web.http.template.section.HttpTemplateInitialTask.Dependencies;
import net.officefloor.plugin.web.http.template.section.HttpTemplateInitialTask.Flows;

/**
 * {@link WorkSource} to provide the {@link HttpTemplateInitialTask}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateInitialWorkSource extends
		AbstractWorkSource<HttpTemplateInitialTask> {

	/**
	 * Property name for the {@link HttpTemplate} URI path.
	 */
	public static final String PROPERTY_TEMPLATE_URI = HttpTemplateWorkSource.PROPERTY_TEMPLATE_URI;

	/**
	 * Property name for a comma separated list of HTTP methods that will
	 * trigger a redirect before rendering the {@link HttpTemplate}.
	 */
	public static final String PROPERTY_RENDER_REDIRECT_HTTP_METHODS = "http.template.render.redirect.methods";

	/**
	 * Property name for the Content-Type of the {@link HttpTemplate}.
	 */
	public static final String PROPERTY_CONTENT_TYPE = "http.template.render.content.type";

	/**
	 * Property name for the {@link Charset} of the {@link HttpTemplate}.
	 */
	public static final String PROPERTY_CHARSET = "http.template.render.charset";

	/**
	 * Name of the {@link HttpTemplateInitialTask}.
	 */
	public static final String TASK_NAME = "TASK";

	/*
	 * ======================= WorkSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_TEMPLATE_URI, "URI Path");
	}

	@Override
	public void sourceWork(
			WorkTypeBuilder<HttpTemplateInitialTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the template URI path
		String templateUriPath = HttpTemplateWorkSource
				.getHttpTemplateUrlContinuationPath(context);

		// Determine if the template is secure
		boolean isSecure = HttpTemplateWorkSource.isHttpTemplateSecure(context);

		/*
		 * Only trigger redirect if not secure. If sent on secure connection but
		 * no need for secure, service anyway. This is to save establishing a
		 * new connection and a round trip when already have the request.
		 */
		Boolean isRequireSecure = (isSecure ? Boolean.TRUE : null);

		// Obtain the listing of render redirect HTTP methods
		String[] renderRedirectHttpMethods = null;
		String renderRedirectProperty = context.getProperty(
				PROPERTY_RENDER_REDIRECT_HTTP_METHODS, null);
		if (renderRedirectProperty != null) {
			// Obtain the render redirect HTTP methods
			renderRedirectHttpMethods = renderRedirectProperty.split(",");
			for (int i = 0; i < renderRedirectHttpMethods.length; i++) {
				renderRedirectHttpMethods[i] = renderRedirectHttpMethods[i]
						.trim();
			}
		}

		// Obtain the content type and charset
		String contentType = context.getProperty(PROPERTY_CONTENT_TYPE, null);
		String charsetName = context.getProperty(PROPERTY_CHARSET, null);
		Charset charset = null;
		if (charsetName != null) {
			charset = Charset.forName(charsetName);
		}

		// Create the HTTP Template initial task
		HttpTemplateInitialTask factory = new HttpTemplateInitialTask(
				templateUriPath, isSecure, renderRedirectHttpMethods,
				contentType, charset, charsetName);

		// Configure the task
		workTypeBuilder.setWorkFactory(factory);
		TaskTypeBuilder<Dependencies, Flows> task = workTypeBuilder
				.addTaskType("TASK", factory, Dependencies.class, Flows.class);
		task.addObject(ServerHttpConnection.class).setKey(
				Dependencies.SERVER_HTTP_CONNECTION);
		task.addObject(HttpApplicationLocation.class).setKey(
				Dependencies.HTTP_APPLICATION_LOCATION);
		task.addObject(HttpRequestState.class).setKey(
				Dependencies.REQUEST_STATE);
		task.addObject(HttpSession.class).setKey(Dependencies.HTTP_SESSION);
		task.addFlow().setKey(Flows.RENDER);
		task.addEscalation(IOException.class);
		task.setDifferentiator(new HttpUrlContinuationDifferentiatorImpl(
				templateUriPath, isRequireSecure));
	}

}