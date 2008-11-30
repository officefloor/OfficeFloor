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

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.work.TaskFlowModel;
import net.officefloor.model.work.TaskModel;
import net.officefloor.model.work.TaskObjectModel;
import net.officefloor.model.work.WorkModel;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.work.WorkLoaderUtil;

/**
 * Tests the {@link HttpRouteWorkLoader}.
 * 
 * @author Daniel
 */
public class HttpRouteWorkLoaderTest extends OfficeFrameTestCase {

	/**
	 * Ensures not load without at least one path.
	 */
	public void testNoRoutes() throws Exception {
		try {
			this.loadHttpRouteWork();
			fail("Should not be able to load without routings");
		} catch (Exception ex) {
			assertEquals("Incorrect exception",
					"Must have at least one routing entry", ex.getMessage());
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
	 * Tests the {@link HttpRouteWorkLoader}.
	 * 
	 * @param properties
	 *            Name/value property pairs.
	 */
	private void doTest(String... properties) throws Exception {

		// Create the listing of flow names from properties
		String[] flowNames = new String[properties.length / 2];
		String[] expectedPatterns = new String[properties.length / 2];
		for (int i = 0; i < properties.length; i += 2) {
			String flowName = properties[i];
			flowNames[i / 2] = flowName;

			String pattern = properties[i + 1];
			expectedPatterns[i / 2] = pattern;
		}

		// Load the work
		WorkModel<?> actualWork = this.loadHttpRouteWork(properties);

		// Create the expected work
		WorkModel<?> expectedWork = this.createHttpRouteWorkModel(flowNames);

		// Verify correct
		WorkLoaderUtil.assertWorkModelMatch(expectedWork, actualWork);

		// Obtain the HTTP route task
		HttpRouteTask httpRouteTask = (HttpRouteTask) actualWork
				.getWorkFactory();
		for (int i = 0; i < expectedPatterns.length; i++) {
			String expectedPattern = expectedPatterns[i];
			String actualPattern = httpRouteTask.routings[i].pattern();
			assertEquals("Incorrect routing pattern, index " + i,
					expectedPattern, actualPattern);
		}
	}

	/**
	 * Creates the HTTP route {@link WorkModel} for comparison testing.
	 * 
	 * @param flowNames
	 *            Names of the flows.
	 * @return {@link WorkModel}.
	 */
	private WorkModel<?> createHttpRouteWorkModel(String... flowNames) {

		// Create the HTTP route task
		HttpRouteTask httpRouteTask = new HttpRouteTask(new Pattern[0]);

		// Create the expected work model
		WorkModel<HttpRouteTask> work = new WorkModel<HttpRouteTask>();
		work.setTypeOfWork(HttpRouteTask.class);
		work.setWorkFactory(httpRouteTask);

		// Create the route task
		TaskModel<Indexed, Indexed> task = new TaskModel<Indexed, Indexed>();
		task.setTaskName("route");
		task.setTaskFactoryManufacturer(httpRouteTask);
		work.addTask(task);

		// Create reference to HTTP managed object
		TaskObjectModel<Indexed> object = new TaskObjectModel<Indexed>();
		object.setObjectType(ServerHttpConnection.class.getName());
		task.addObject(object);

		// Create the flows for each flow name
		for (int i = 0; i < flowNames.length; i++) {
			String flowName = flowNames[i];
			TaskFlowModel<Indexed> flow = new TaskFlowModel<Indexed>();
			flow.setFlowIndex(i);
			flow.setLabel(flowName);
			task.addFlow(flow);
		}

		// Add the default flow
		TaskFlowModel<Indexed> defaultFlow = new TaskFlowModel<Indexed>();
		defaultFlow.setFlowIndex(flowNames.length); // last index
		defaultFlow.setLabel("default");
		task.addFlow(defaultFlow);

		// Return the work
		return work;
	}

	/**
	 * <p>
	 * Loads the {@link WorkModel} for the {@link HttpRouteWorkLoader} given the
	 * input properties.
	 * <p>
	 * Note that property names are prefixed with
	 * {@link HttpRouteWorkLoader#ROUTE_PROPERTY_PREFIX}.
	 * 
	 * @param properties
	 *            Name value property pairings.
	 * @return {@link WorkModel}.
	 * @throws Exception
	 *             If fails to load {@link WorkModel}.
	 */
	private WorkModel<?> loadHttpRouteWork(String... properties)
			throws Exception {

		// Prefix the property names if necessary
		for (int i = 0; i < properties.length; i += 2) {
			String propertyName = properties[i];
			properties[i] = HttpRouteWorkLoader.ROUTE_PROPERTY_PREFIX
					+ propertyName;
		}

		// Load and return the work
		return WorkLoaderUtil.loadWork(HttpRouteWorkLoader.class, properties);
	}
}
