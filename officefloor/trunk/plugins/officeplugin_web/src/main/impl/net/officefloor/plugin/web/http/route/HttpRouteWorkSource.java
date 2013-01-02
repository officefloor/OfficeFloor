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
package net.officefloor.plugin.web.http.route;

import java.io.IOException;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.Office;
import net.officefloor.frame.api.manage.TaskManager;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.HttpRequestState;
import net.officefloor.plugin.web.http.continuation.HttpUrlContinuationDifferentiator;
import net.officefloor.plugin.web.http.location.HttpApplicationLocation;
import net.officefloor.plugin.web.http.location.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.route.HttpRouteTask.HttpRouteTaskDependencies;
import net.officefloor.plugin.web.http.route.HttpRouteTask.HttpRouteTaskFlows;
import net.officefloor.plugin.web.http.session.HttpSession;
import net.officefloor.plugin.web.http.tokenise.HttpRequestTokeniseException;

/**
 * <p>
 * {@link WorkSource} to provide appropriately secure
 * {@link ServerHttpConnection}.
 * <p>
 * Configuration of what to secure is determined by
 * {@link HttpUrlContinuationDifferentiator} on the {@link Office}
 * {@link TaskManager} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpRouteWorkSource extends AbstractWorkSource<HttpRouteTask> {

	/**
	 * Name of the {@link HttpRouteTask}.
	 */
	public static final String TASK_NAME = "route";

	/*
	 * ==================== WorkSource ==========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No required properties
	}

	@Override
	public void sourceWork(WorkTypeBuilder<HttpRouteTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Configure the work factory
		HttpRouteTask factory = new HttpRouteTask();
		workTypeBuilder.setWorkFactory(factory);

		// Configure the task
		TaskTypeBuilder<HttpRouteTaskDependencies, HttpRouteTaskFlows> task = workTypeBuilder
				.addTaskType(TASK_NAME, factory,
						HttpRouteTaskDependencies.class,
						HttpRouteTaskFlows.class);
		task.addObject(ServerHttpConnection.class).setKey(
				HttpRouteTaskDependencies.SERVER_HTTP_CONNECTION);
		task.addObject(HttpApplicationLocation.class).setKey(
				HttpRouteTaskDependencies.HTTP_APPLICATION_LOCATION);
		task.addObject(HttpRequestState.class).setKey(
				HttpRouteTaskDependencies.REQUEST_STATE);
		task.addObject(HttpSession.class).setKey(
				HttpRouteTaskDependencies.HTTP_SESSION);
		task.addFlow().setKey(HttpRouteTaskFlows.NOT_HANDLED);
		task.addEscalation(InvalidHttpRequestUriException.class);
		task.addEscalation(HttpRequestTokeniseException.class);
		task.addEscalation(IOException.class);
		task.addEscalation(UnknownWorkException.class);
		task.addEscalation(UnknownTaskException.class);
		task.addEscalation(InvalidParameterTypeException.class);
	}

}