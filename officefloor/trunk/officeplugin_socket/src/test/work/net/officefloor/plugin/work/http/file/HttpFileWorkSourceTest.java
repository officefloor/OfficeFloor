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

import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.work.http.HttpException;
import net.officefloor.plugin.work.http.file.HttpFileTask.HttpFileTaskDependencies;

/**
 * Tests the {@link HttpFileWorkSource}.
 *
 * @author Daniel Sagenschneider
 */
public class HttpFileWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensures specification is correct.
	 */
	public void testSpecification() {
		// Should be no properties
		WorkLoaderUtil.validateSpecification(HttpFileWorkSource.class,
				HttpFileWorkSource.PACKAGE_PREFIX_PROPERTY_NAME,
				"Package prefix",
				HttpFileWorkSource.DEFAULT_INDEX_FILE_PROPETY_NAME,
				"Default index file");
	}

	/**
	 * Validates the {@link WorkType} for the {@link HttpFileWorkSource}.
	 */
	public void testLoad() throws Exception {

		// Build the expected work type
		HttpFileTask workTaskFactory = new HttpFileTask("html", "index.html");
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
		WorkLoaderUtil.validateWorkType(work, HttpFileWorkSource.class,
				HttpFileWorkSource.PACKAGE_PREFIX_PROPERTY_NAME, "html",
				HttpFileWorkSource.DEFAULT_INDEX_FILE_PROPETY_NAME,
				"index.html");
	}

}