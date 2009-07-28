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
package net.officefloor.plugin.work.http.file;

import java.io.File;
import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.work.http.HttpException;
import net.officefloor.plugin.work.http.file.HttpFileTask.HttpFileTaskDependencies;

/**
 * {@link WorkSource} to provide {@link File} content HTTP responses.
 *
 * @author Daniel Sagenschneider
 */
public class HttpFileWorkSource extends AbstractWorkSource<HttpFileTask> {

	/**
	 * Property name for the package prefix.
	 */
	public static final String PACKAGE_PREFIX_PROPERTY_NAME = "package.prefix";

	/**
	 * Property name for the default index file name.
	 */
	public static final String DEFAULT_INDEX_FILE_PROPETY_NAME = "default.index.file.name";

	/*
	 * ===================== AbstractWorkSource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(PACKAGE_PREFIX_PROPERTY_NAME, "Package prefix");
		context.addProperty(DEFAULT_INDEX_FILE_PROPETY_NAME,
				"Default index file");
	}

	@Override
	public void sourceWork(WorkTypeBuilder<HttpFileTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the properties
		String packagePrefix = context
				.getProperty(PACKAGE_PREFIX_PROPERTY_NAME);
		String defaultIndexFileName = context
				.getProperty(DEFAULT_INDEX_FILE_PROPETY_NAME);

		// Create the task to return a file
		HttpFileTask task = new HttpFileTask(packagePrefix,
				defaultIndexFileName);

		// Define the work
		workTypeBuilder.setWorkFactory(task);
		TaskTypeBuilder<HttpFileTaskDependencies, None> fileTask = workTypeBuilder
				.addTaskType("file", task, HttpFileTaskDependencies.class,
						None.class);
		fileTask.addObject(ServerHttpConnection.class).setKey(
				HttpFileTaskDependencies.SERVER_HTTP_CONNECTION);
		fileTask.addEscalation(HttpException.class);
		fileTask.addEscalation(IOException.class);
	}

}