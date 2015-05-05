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
import java.util.HashSet;
import java.util.Set;

import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.util.AbstractSingleTask;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.route.HttpRouteTask;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.template.parse.HttpTemplate;

/**
 * Initial {@link Task} to ensure appropriate conditions for rendering the
 * {@link HttpTemplate}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateInitialTask
		extends
		AbstractSingleTask<HttpTemplateInitialTask, HttpTemplateInitialTask.Dependencies, HttpTemplateInitialTask.Flows> {

	/**
	 * Keys for the {@link HttpTemplateInitialTask} dependencies.
	 */
	public static enum Dependencies {
		SERVER_HTTP_CONNECTION, HTTP_APPLICATION_LOCATION, REQUEST_STATE, HTTP_SESSION
	}

	/**
	 * Keys for the {@link HttpTemplateInitialTask} flows.
	 */
	public static enum Flows {
		RENDER
	}

	/**
	 * Default HTTP methods to redirect before rendering the
	 * {@link HttpTemplate}.
	 */
	public static final String[] DEFAULT_RENDER_REDIRECT_HTTP_METHODS = new String[] {
			"POST", "PUT" };

	/**
	 * URI path for the {@link HttpTemplate}.
	 */
	private final String templateUriPath;

	/**
	 * Indicates if a secure {@link ServerHttpConnection} is required.
	 */
	private final boolean isRequireSecure;

	/**
	 * HTTP methods to redirect before rendering the {@link HttpTemplate}.
	 */
	private final Set<String> renderRedirectHttpMethods;

	/**
	 * Content-type for the {@link HttpTemplate}. May be <code>null</code>.
	 */
	private final String contentType;

	/**
	 * {@link Charset} for the {@link HttpTemplate}. May be <code>null</code>.
	 */
	private final Charset charset;

	/**
	 * Name of the {@link Charset} for the {@link HttpTemplate}. Provided only
	 * if {@link #charset} provided, otherwise <code>null</code>.
	 */
	private final String charsetName;

	/**
	 * Initiate.
	 * 
	 * @param templateUriPath
	 *            URI path for the {@link HttpTemplate}.
	 * @param isRequireSecure
	 *            Indicates if a secure {@link ServerHttpConnection} is
	 *            required.
	 * @param renderRedirectHttpMethods
	 *            Listing of HTTP methods that require a redirect before
	 *            rendering the {@link HttpTemplate}.
	 * @param contentType
	 *            Content-type for the {@link HttpTemplate}. May be
	 *            <code>null</code>.
	 * @param charset
	 *            Charset for {@link HttpTemplate}. May be <code>null</code>.
	 * @param charsetName
	 *            Name of the {@link Charset} for the {@link HttpTemplate}.
	 *            Provided only if {@link #charset} provided, otherwise
	 *            <code>null</code>.
	 */
	public HttpTemplateInitialTask(String templateUriPath,
			boolean isRequireSecure, String[] renderRedirectHttpMethods,
			String contentType, Charset charset, String charsetName) {
		this.templateUriPath = templateUriPath;
		this.isRequireSecure = isRequireSecure;
		this.contentType = contentType;
		this.charset = charset;
		this.charsetName = charsetName;

		// Add the render redirect HTTP methods
		Set<String> methods = new HashSet<String>();
		for (String method : (renderRedirectHttpMethods == null ? DEFAULT_RENDER_REDIRECT_HTTP_METHODS
				: renderRedirectHttpMethods)) {
			methods.add(method.trim().toUpperCase());
		}
		this.renderRedirectHttpMethods = methods;
	}

	/*
	 * ======================= Task ===============================
	 */

	@Override
	public Object doTask(
			TaskContext<HttpTemplateInitialTask, Dependencies, Flows> context)
			throws IOException {

		// Obtain the dependencies
		ServerHttpConnection connection = (ServerHttpConnection) context
				.getObject(Dependencies.SERVER_HTTP_CONNECTION);
		HttpApplicationLocation location = (HttpApplicationLocation) context
				.getObject(Dependencies.HTTP_APPLICATION_LOCATION);
		HttpRequestState requestState = (HttpRequestState) context
				.getObject(Dependencies.REQUEST_STATE);
		HttpSession session = (HttpSession) context
				.getObject(Dependencies.HTTP_SESSION);

		// Flag indicating if redirect is required
		boolean isRedirectRequired = false;

		// Determine if requires a secure connection
		if (this.isRequireSecure) {

			/*
			 * Request may have come in on another URL continuation which did
			 * not require a secure connection and is to now to render this HTTP
			 * template. Therefore trigger redirect for a secure connection.
			 * 
			 * Note that do not down grade to non-secure connection as already
			 * have the request and no need to close the existing secure
			 * connection and establish a new non-secure connection.
			 */
			boolean isConnectionSecure = connection.isSecure();
			if (!isConnectionSecure) {
				// Flag redirect for secure connection
				isRedirectRequired = true;
			}
		}

		// Determine if POST/redirect/GET pattern to be applied
		if (!isRedirectRequired) {
			// Request likely overridden to POST, so use client HTTP method
			String method = connection.getHttpMethod();
			if (this.renderRedirectHttpMethods.contains(method.toUpperCase())) {
				// Flag redirect for POST/redirect/GET pattern
				isRedirectRequired = true;
			}
		}

		// Undertake the redirect
		if (isRedirectRequired) {
			HttpRouteTask.doRedirect(this.templateUriPath,
					this.isRequireSecure, connection, location, requestState,
					session);
			return null; // redirected, do not render template
		}

		// Configure the response
		if (this.contentType != null) {
			connection.getHttpResponse().setContentType(this.contentType);
		}
		if (this.charset != null) {
			connection.getHttpResponse().setContentCharset(this.charset,
					this.charsetName);
		}

		// Render the template
		context.doFlow(Flows.RENDER, null);
		return null;
	}

}