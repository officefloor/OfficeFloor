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

package net.officefloor.plugin.web.http.template.route;

import net.officefloor.compile.WorkSourceService;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.spi.work.source.WorkSourceContext;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.spi.work.source.impl.AbstractWorkSource;
import net.officefloor.frame.api.execute.Task;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownTaskException;
import net.officefloor.frame.api.manage.UnknownWorkException;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.resource.InvalidHttpRequestUriException;
import net.officefloor.plugin.web.http.template.parse.LinkHttpTemplateSectionContent;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteTask.HttpTemplateRouteDependencies;
import net.officefloor.plugin.web.http.template.route.HttpTemplateRouteTask.HttpTemplateRouteTaskFlows;

/**
 * {@link WorkSource} to providing routing of the
 * {@link LinkHttpTemplateSectionContent} request to the handling {@link Task}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpTemplateRouteWorkSource extends
		AbstractWorkSource<HttpTemplateRouteTask> implements
		WorkSourceService<HttpTemplateRouteTask, HttpTemplateRouteWorkSource> {

	/**
	 * Name of property providing the {@link Task} name prefix.
	 */
	public static final String PROPERTY_TASK_NAME_PREFIX = "task.name.prefix";

	/*
	 * ===================== WorkSourceService =========================
	 */

	@Override
	public String getWorkSourceAlias() {
		return "HTTP_TEMPLATE_ROUTER";
	}

	@Override
	public Class<HttpTemplateRouteWorkSource> getWorkSourceClass() {
		return HttpTemplateRouteWorkSource.class;
	}

	/*
	 * ======================== WorkSource ============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	public void sourceWork(
			WorkTypeBuilder<HttpTemplateRouteTask> workTypeBuilder,
			WorkSourceContext context) throws Exception {

		// Obtain the task name prefix
		String taskNamePrefix = context.getProperty(PROPERTY_TASK_NAME_PREFIX,
				null);

		// Create the task
		HttpTemplateRouteTask taskFactory = new HttpTemplateRouteTask(
				taskNamePrefix);

		// Construct the work
		workTypeBuilder.setWorkFactory(taskFactory);

		// Construct the task
		TaskTypeBuilder<HttpTemplateRouteDependencies, HttpTemplateRouteTaskFlows> task = workTypeBuilder
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
	}

}