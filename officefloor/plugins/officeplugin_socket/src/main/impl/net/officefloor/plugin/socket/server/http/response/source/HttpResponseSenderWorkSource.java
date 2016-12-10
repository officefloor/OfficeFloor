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
package net.officefloor.plugin.socket.server.http.response.source;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
		byte[] httpResponseContent = null;
		if (httpResponseFile != null) {
			// Obtain the HTTP response content
			InputStream httpResponseInputStream = context
					.getOptionalResource(httpResponseFile);
			if (httpResponseInputStream == null) {
				throw new FileNotFoundException(
						"Can not find HTTP response file '" + httpResponseFile
								+ "' on the class path");
			}
			ByteArrayOutputStream fileContent = new ByteArrayOutputStream();
			for (int data = httpResponseInputStream.read(); data != -1; data = httpResponseInputStream
					.read()) {
				fileContent.write(data);
			}
			httpResponseContent = fileContent.toByteArray();
		}

		// Create the send task
		HttpResponseSendTask task = new HttpResponseSendTask(httpStatus,
				httpResponseContent);

		// Load the work type information
		workTypeBuilder.setWorkFactory(task);

		// Load the send task type information
		HttpResponseSendTask.addTaskType("SEND", task, workTypeBuilder);
	}
}