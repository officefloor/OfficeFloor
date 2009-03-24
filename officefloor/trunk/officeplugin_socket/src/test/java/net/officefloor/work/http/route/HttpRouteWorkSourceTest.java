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
package net.officefloor.work.http.route;

import java.util.regex.Pattern;

import net.officefloor.compile.spi.work.WorkType;
import net.officefloor.compile.spi.work.source.TaskTypeBuilder;
import net.officefloor.compile.spi.work.source.WorkTypeBuilder;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.work.http.route.HttpRouteTask.HttpRouteTaskDependencies;

/**
 * Tests the {@link HttpRouteWorkSource}.
 * 
 * @author Daniel
 */
public class HttpRouteWorkSourceTest extends OfficeFrameTestCase {

	/**
	 * Verifies the specification.
	 */
	public void testSpecification() {
		// No properties
		WorkLoaderUtil.validateSpecification(HttpRouteWorkSource.class);
	}

	/**
	 * Ensures not load without at least one path.
	 */
	public void testNoRoutes() throws Exception {
		try {
			WorkLoaderUtil.loadWork(HttpRouteWorkSource.class);
			fail("Should not be able to load without routings");
		} catch (Throwable ex) {
		}
	}

	/**
	 * Ensures able to load with single route.
	 */
	public void testWithSingleRoute() throws Exception {
		this.doTest("path", "path");
	}

	/**
	 * Ensures able to load with multiple routes.
	 */
	public void testWithMultipleRoutes() throws Exception {
		this.doTest("one", "one", "two", "two", "three", "three");
	}

	/**
	 * Tests the {@link HttpRouteWorkSource}.
	 * 
	 * @param properties
	 *            Name/value property pairs.
	 */
	private void doTest(String... properties) throws Exception {

		// Create the listing of flow names and patterns
		String[] flowNames = new String[properties.length / 2];
		String[] expectedPatterns = new String[properties.length / 2];
		for (int i = 0; i < properties.length; i += 2) {
			String flowName = properties[i];
			flowNames[i / 2] = flowName;

			String pattern = properties[i + 1];
			expectedPatterns[i / 2] = pattern;
		}

		// Create the expected work
		WorkTypeBuilder<HttpRouteTask> expectedWork = this
				.createHttpRouteWorkModel(flowNames);

		// Verify work is correct
		WorkLoaderUtil.validateWorkType(expectedWork,
				HttpRouteWorkSource.class, route(properties));
	}

	/**
	 * Creates the HTTP route {@link WorkType} for comparison testing.
	 * 
	 * @param flowNames
	 *            Names of the flows.
	 * @return {@link WorkType}.
	 */
	private WorkTypeBuilder<HttpRouteTask> createHttpRouteWorkModel(
			String... flowNames) {

		HttpRouteTask workTaskFactory = new HttpRouteTask(new Pattern[0]);
		WorkTypeBuilder<HttpRouteTask> work = WorkLoaderUtil
				.createWorkTypeBuilder(workTaskFactory);
		TaskTypeBuilder<HttpRouteTaskDependencies, Indexed> task = work
				.addTaskType("route", workTaskFactory,
						HttpRouteTaskDependencies.class, Indexed.class);
		task.addObject(ServerHttpConnection.class).setKey(
				HttpRouteTaskDependencies.SERVER_HTTP_CONNECTION);

		// Create the flows for each flow name
		for (String flowName : flowNames) {
			task.addFlow().setLabel(flowName);
		}

		// Add the default flow
		task.addFlow().setLabel("default");

		// Return the work
		return work;
	}

	/**
	 * Transforms the property names to be prefixed with
	 * {@link HttpRouteWorkSource#ROUTE_PROPERTY_PREFIX}.
	 * 
	 * @param properties
	 *            Name value property pairings.
	 * @return Transformed properties.
	 */
	private String[] route(String... properties) {

		// Prefix the property names if necessary
		String[] transformedProperties = new String[properties.length];
		for (int i = 0; i < properties.length; i += 2) {
			transformedProperties[i] = HttpRouteWorkSource.ROUTE_PROPERTY_PREFIX
					+ properties[i];
			transformedProperties[i + 1] = properties[i + 1];
		}

		// Return the transformed properties
		return transformedProperties;
	}

}