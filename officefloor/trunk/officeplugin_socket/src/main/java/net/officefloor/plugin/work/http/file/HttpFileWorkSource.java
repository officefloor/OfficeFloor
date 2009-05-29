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
package net.officefloor.plugin.work.http.file;

import java.io.File;
import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.work.http.HttpException;
import net.officefloor.plugin.work.http.file.HttpFileTask.HttpFileTaskDependencies;

/**
 * {@link WorkSource} to provide {@link File} content HTTP responses.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileWorkSource extends AbstractWorkSource<HttpFileTask> {

	/*
	 * ===================== AbstractWorkSource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// TODO consider providing directory to restrict file accesses
	}

	@Override
	public void sourceWork(WorkTypeBuilder<HttpFileTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Create the task to return a file
		HttpFileTask task = new HttpFileTask();

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