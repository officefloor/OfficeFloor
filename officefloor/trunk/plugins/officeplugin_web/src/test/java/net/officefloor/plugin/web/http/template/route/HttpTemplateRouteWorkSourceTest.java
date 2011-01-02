/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2010 Daniel Sagenschneider
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
package net.officefloor.plugin.web.http.template.route;

import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.resource.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteTask;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteWorkSource;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteTask.HttpTemplateRouteDependencies;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteTask.HttpTemplateRouteTaskFlows;

/**
 * Tests the {@link HttpTemplateRouteWorkSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateRouteWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Verifies the specification.
	 */
	public void testSpecification() {
		WorkLoaderUtil.validateSpecification(HttpTemplateRouteWorkSource.class);
	}

	/**
	 * Ensures correct type.
	 */
	public void testType() {

		// Create the type
		HttpTemplateRouteTask taskFactory = new HttpTemplateRouteTask();
		WorkTypeBuilder<HttpTemplateRouteTask> work = WorkLoaderUtil
				.createWorkTypeBuilder(taskFactory);
		TaskTypeBuilder<HttpTemplateRouteDependencies, HttpTemplateRouteTaskFlows> task = work
				.addTaskType("route", taskFactory,
						HttpTemplateRouteDependencies.class,
						HttpTemplateRouteTaskFlows.class);
		task.addObject(ServerHttpConnection.class).setKey(
				HttpTemplateRouteDependencies.SERVER_HTTP_CONNECTION);
		task.addFlow().setKey(HttpTemplateRouteTaskFlows.NON_MATCHED_REQUEST);
		task.addEscalation(InvalidHttpRequestUriException.class);
		task.addEscalation(UnknownWorkException.class);
		task.addEscalation(UnknownTaskException.class);
		task.addEscalation(InvalidParameterTypeException.class);

		// Verify type
		WorkLoaderUtil
				.validateWorkType(work, HttpTemplateRouteWorkSource.class);
	}

}