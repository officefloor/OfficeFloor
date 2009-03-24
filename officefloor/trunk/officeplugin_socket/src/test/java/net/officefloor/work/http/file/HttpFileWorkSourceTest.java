/*
 *  Office Floor, Application Server
 *  Copyright (C) 2006 Daniel Sagenschneider
 *
 *  This program is free software; you can redistribute it and/or modify it under the terms 
 *  of the GNU General Public License as published by the Free Software Foundation; either 
 *  version 2 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program; 
 *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *  MA 02111-1307 USA
 */
package net.officefloor.work.http.file;

import java.io.IOException;

import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.work.http.HttpException;
import net.officefloor.work.http.file.HttpFileTask.HttpFileTaskDependencies;

/**
 * Tests the {@link HttpFileWorkSource}.
 * 
 * @author Daniel
 */
public class HttpFileWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensures specification is correct.
	 */
	public void testSpecification() {
		// Should be no properties
		WorkLoaderUtil.validateSpecification(HttpFileWorkSource.class);
	}

	/**
	 * Validates the {@link WorkType} for the {@link HttpFileWorkSource}.
	 */
	public void testLoad() throws Exception {

		// Build the expected work type
		HttpFileTask workTaskFactory = new HttpFileTask();
		WorkTypeBuilder<HttpFileTask> work = WorkLoaderUtil
				.createWorkTypeBuilder(workTaskFactory);
		TaskTypeBuilder<HttpFileTaskDependencies, None> task = work
				.addTaskType("file", workTaskFactory,
						HttpFileTaskDependencies.class, None.class);
		task.addObject(ServerHttpConnection.class).setKey(
				HttpFileTaskDependencies.SERVER_HTTP_CONNECTION);
		task.addEscalation(HttpException.class);
		task.addEscalation(IOException.class);

		// Verify work type
		WorkLoaderUtil.validateWorkType(work, HttpFileWorkSource.class);
	}

}