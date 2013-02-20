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
package net.officefloor.plugin.web.http.resource.file;

import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.resource.HttpFile;
import net.officefloor.plugin.web.http.resource.file.HttpFileWriterTask.HttpFileWriterTaskDependencies;

/**
 * {@link WorkSource} that sends a {@link HttpFile}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpFileWriterWorkSource extends
		AbstractWorkSource<HttpFileWriterTask> {

	/*
	 * ======================== AbstractWorkSource =====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceWork(WorkTypeBuilder<HttpFileWriterTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Create the factory
		HttpFileWriterTask factory = new HttpFileWriterTask();

		// Specify the work factory
		workTypeBuilder.setWorkFactory(factory);

		// Add the task
		TaskTypeBuilder<HttpFileWriterTaskDependencies, None> task = workTypeBuilder
				.addTaskType("WriteFileToResponse", factory,
						HttpFileWriterTaskDependencies.class, None.class);
		task.addObject(HttpFile.class).setKey(
				HttpFileWriterTaskDependencies.HTTP_FILE);
		task.addObject(ServerHttpConnection.class).setKey(
				HttpFileWriterTaskDependencies.SERVER_HTTP_CONNECTION);
		task.addEscalation(IOException.class);
	}

}