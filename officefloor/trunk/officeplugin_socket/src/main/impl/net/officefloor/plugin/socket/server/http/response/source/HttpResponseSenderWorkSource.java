/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2009 Daniel Sagenschneider
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
package net.officefloor.plugin.socket.server.http.response.source;

import java.io.File;

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.execute.Work;
import net.officefloor.plugin.socket.server.http.HttpResponse;

/**
 * {@link WorkSource} to trigger sending the {@link HttpResponse}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResponseSenderWorkSource extends AbstractWorkSource<Work> {

	/**
	 * Property to obtain the HTTP status for the {@link HttpResponse}.
	 */
	public static final String PROPERTY_HTTP_STATUS = "http.status";

	/**
	 * Property to obtain the class path location of the {@link File} containing
	 * the HTTP response.
	 */
	public static final String PROPERTY_HTTP_RESPONSE_CONTENT_FILE = "http.response.file";

	/*
	 * ======================= AbstractWorkSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceWork(WorkTypeBuilder<Work> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the HTTP status (default negative to not set)
		int httpStatus = Integer.parseInt(context.getProperty(
				PROPERTY_HTTP_STATUS, String.valueOf(-1)));

		// Obtain the HTTP response content (null to not set)
		String httpResponseFile = context.getProperty(
				PROPERTY_HTTP_RESPONSE_CONTENT_FILE, null);
		if (httpResponseFile != null) {
			// Obtain the HTTP response content

			// TODO provide content to HTTP response
			throw new UnsupportedOperationException(
					"TODO implement providing content for HTTP response");
		}

		// Create the send task
		HttpResponseSendTask task = new HttpResponseSendTask(httpStatus);

		// Load the work type information
		workTypeBuilder.setWorkFactory(task);

		// Load the send task type information
		HttpResponseSendTask.addTaskType("SEND", task, workTypeBuilder);
	}

}