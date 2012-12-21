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
package net.officefloor.plugin.web.http.route;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.stream.impl.NotAllDataAvailableException;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.route.HttpRouteTask.HttpRouteTaskDependencies;
import net.officefloor.plugin.web.http.route.HttpRouteTask.HttpRouteTaskFlows;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniseException;

/**
 * Tests the {@link HttpRouteWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRouteWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Ensure correct specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpRouteWorkSource.class);
	}

	/**
	 * Ensure correct type.
	 */
	public void testType() {

		// Create the expected type
		HttpRouteTask factory = new HttpRouteTask();
		WorkTypeBuilder<HttpRouteTask> type = WorkLoaderUtil
				.createWorkTypeBuilder(factory);
		TaskTypeBuilder<HttpRouteTaskDependencies, HttpRouteTaskFlows> task = type
				.addTaskType(HttpRouteWorkSource.TASK_NAME, factory,
						HttpRouteTaskDependencies.class,
						HttpRouteTaskFlows.class);
		task.addObject(ServerHttpConnection.class).setKey(
				HttpRouteTaskDependencies.SERVER_HTTP_CONNECTION);
		task.addObject(HttpApplicationLocation.class).setKey(
				HttpRouteTaskDependencies.HTTP_APPLICATION_LOCATION);
		task.addObject(HttpSession.class).setKey(
				HttpRouteTaskDependencies.HTTP_SESSION);
		task.addFlow().setKey(HttpRouteTaskFlows.NOT_HANDLED);
		task.addEscalation(InvalidHttpRequestUriException.class);
		task.addEscalation(HttpRequestTokeniseException.class);
		task.addEscalation(NotAllDataAvailableException.class);
		task.addEscalation(UnknownWorkException.class);
		task.addEscalation(UnknownTaskException.class);
		task.addEscalation(InvalidParameterTypeException.class);

		// Validate the expected type
		WorkLoaderUtil.validateWorkType(type, HttpRouteWorkSource.class);
	}

}