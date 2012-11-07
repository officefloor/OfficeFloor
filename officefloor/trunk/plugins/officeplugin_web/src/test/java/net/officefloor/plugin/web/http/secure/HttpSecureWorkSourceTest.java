/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.secure;

import org.junit.Ignore;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.secure.HttpSecureTask.HttpSecureTaskDependencies;
import net.officefloor.plugin.web.http.secure.HttpSecureTask.HttpSecureTaskFlows;
import net.officefloor.plugin.web.http.session.HttpSession;

/**
 * Tests the {@link HttpSecureWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO implement functionality")
public class HttpSecureWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpSecureWorkSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		HttpSecureTask factory = new HttpSecureTask();
		WorkTypeBuilder<HttpSecureTask> type = WorkLoaderUtil
				.createWorkTypeBuilder(factory);
		TaskTypeBuilder<HttpSecureTaskDependencies, HttpSecureTaskFlows> task = type
				.addTaskType("secure", factory,
						HttpSecureTaskDependencies.class,
						HttpSecureTaskFlows.class);
		task.addObject(ServerHttpConnection.class).setKey(
				HttpSecureTaskDependencies.SERVER_HTTP_CONNECTION);
		task.addObject(HttpApplicationLocation.class).setKey(
				HttpSecureTaskDependencies.HTTP_APPLICATION_LOCATION);
		task.addObject(HttpSession.class).setKey(
				HttpSecureTaskDependencies.HTTP_SESSION);
		task.addFlow().setKey(HttpSecureTaskFlows.SERVICE);

		// Validate the expected type
		WorkLoaderUtil.validateWorkType(type, HttpSecureWorkSource.class);
	}

}