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
package net.officefloor.plugin.web.http.continuation;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.compile.work.TaskType;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.location.HttpApplicationLocationMangedObject;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;

/**
 * {@link WorkSource} for a HTTP URL continuation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpUrlContinuationWorkSource extends
		AbstractWorkSource<HttpUrlContinuationTask> {

	/**
	 * Name of {@link Property} specifying the URI path for the HTTP URL
	 * continuation.
	 */
	public static final String PROPERTY_URI_PATH = "http.continuation.uri.path";

	/**
	 * Name of {@link Property} specifying whether the HTTP URL continuation
	 * requires a secure {@link ServerHttpConnection}.
	 */
	public static final String PROPERTY_SECURE = "http.continuation.secure";

	/**
	 * Name of the {@link TaskType}.
	 */
	public static final String TASK_NAME = "CONTINUATION";

	/**
	 * Obtains the application URI path from the configured URI path.
	 * 
	 * @param configuredUriPath
	 *            Configured URI path.
	 * @return Application URI path.
	 * @throws InvalidHttpRequestUriException
	 *             If configured URI path is invalid.
	 */
	public static String getApplicationUriPath(String configuredUriPath)
			throws InvalidHttpRequestUriException {

		// Ensure the configure URI path is absolute
		String applicationUriPath = (configuredUriPath.startsWith("/") ? configuredUriPath
				: "/" + configuredUriPath);

		// Ensure is canonical
		applicationUriPath = HttpApplicationLocationMangedObject
				.transformToCanonicalPath(applicationUriPath);

		// Return the Application URI path
		return applicationUriPath;
	}

	/*
	 * ====================== WorkSource ===========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PROPERTY_URI_PATH, "URI Path");
	}

	@Override
	public void sourceWork(
			WorkTypeBuilder<HttpUrlContinuationTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the application URI path (for use)
		String applicationUriPath = context.getProperty(PROPERTY_URI_PATH);
		applicationUriPath = getApplicationUriPath(applicationUriPath);

		// Determine if secure
		String isSecureText = context.getProperty(PROPERTY_SECURE, null);
		Boolean isSecure = (isSecureText == null ? null : Boolean
				.valueOf(isSecureText));

		// Create the differentiator
		HttpUrlContinuationDifferentiator differentiator = new HttpUrlContinuationDifferentiatorImpl(
				applicationUriPath, isSecure);

		// Create the factory
		HttpUrlContinuationTask factory = new HttpUrlContinuationTask();

		// Configure the work and task
		workTypeBuilder.setWorkFactory(factory);
		workTypeBuilder.addTaskType(TASK_NAME, factory, None.class, None.class)
				.setDifferentiator(differentiator);
	}
}