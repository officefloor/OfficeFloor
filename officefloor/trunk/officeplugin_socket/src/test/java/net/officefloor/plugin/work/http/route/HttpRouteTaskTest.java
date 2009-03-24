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
package net.officefloor.plugin.work.http.route;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.execute.FlowFuture;
import net.officefloor.frame.api.execute.TaskContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.socket.server.http.api.HttpRequest;
import net.officefloor.plugin.socket.server.http.api.ServerHttpConnection;
import net.officefloor.plugin.work.http.route.HttpRouteTask;
import net.officefloor.plugin.work.http.route.HttpRouteTask.HttpRouteTaskDependencies;

/**
 * Tests the {@link HttpRouteTask}.
 * 
 * @author Daniel
 */
public class HttpRouteTaskTest extends OfficeFrameTestCase {

	/**
	 * Mock {@link TaskContext}.
	 */
	@SuppressWarnings("unchecked")
	private TaskContext<HttpRouteTask, HttpRouteTaskDependencies, Indexed> taskContext = this
			.createMock(TaskContext.class);

	/**
	 * Mock {@link ServerHttpConnection}.
	 */
	private ServerHttpConnection serverHttpConnection = this
			.createMock(ServerHttpConnection.class);

	/**
	 * Mock {@link FlowFuture}.
	 */
	private FlowFuture flowFuture = this.createMock(FlowFuture.class);

	/**
	 * Mock {@link HttpRequest}.
	 */
	private HttpRequest httpRequest = this.createMock(HttpRequest.class);

	/**
	 * Ensures able to route {@link HttpRequest}.
	 */
	public void testRoute() throws Throwable {
		this.doRouteTest("/path", 0, "/path");
	}

	/**
	 * Ensures able to route {@link HttpRequest} to second flow.
	 */
	public void testRouteToSecond() throws Throwable {
		this.doRouteTest("/second", 1, "/first", "/second", "/third");
	}

	/**
	 * Ensures falls through to default route.
	 */
	public void testDefaultRoute() throws Throwable {
		this.doRouteTest("/default", 1, "/notmatch");
	}

	/**
	 * Ensures to able route {@link HttpRequest} with parameters on the path.
	 */
	public void testRouteWithParameters() throws Throwable {
		this.doRouteTest("/path?param=value", 0, "/path");
	}

	/**
	 * Ensures to able route {@link HttpRequest} with no path.
	 */
	public void testRouteNoPath() throws Throwable {
		this.doRouteTest("", 0, "");
	}

	/**
	 * Ensures able to route {@link HttpRequest} with root path.
	 */
	public void testRouteRoot() throws Throwable {
		this.doRouteTest("/", 0, "/");
	}

	/**
	 * Ensures able to route {@link HttpRequest} with <code>null</code> path.
	 */
	public void testRouteNullPath() throws Throwable {
		this.doRouteTest(null, 0, "");
	}

	/**
	 * Does the routing test.
	 * 
	 * @param path
	 *            Path on the {@link HttpRequest}.
	 * @param flowIndex
	 *            Index of expected flow to invoke.
	 * @param routingPatterns
	 *            Listing of routing patterns.
	 */
	private void doRouteTest(String path, int flowIndex,
			String... routingPatterns) throws Throwable {

		// Create the pattern listings
		List<Pattern> patterns = new LinkedList<Pattern>();
		for (String routingPattern : routingPatterns) {
			patterns.add(Pattern.compile(routingPattern));
		}

		// Create the route task
		HttpRouteTask routeTask = new HttpRouteTask(patterns
				.toArray(new Pattern[0]));

		// Record actions on mocks
		this.recordReturn(this.taskContext, this.taskContext.getObject(0),
				this.serverHttpConnection);
		this.recordReturn(this.serverHttpConnection, this.serverHttpConnection
				.getHttpRequest(), this.httpRequest);
		this.recordReturn(this.httpRequest, this.httpRequest.getPath(), path);
		this.recordReturn(this.taskContext, this.taskContext.doFlow(flowIndex,
				null), this.flowFuture);

		// Replay mocks
		this.replayMockObjects();

		// Execute the task to route the request
		routeTask.doTask(this.taskContext);

		// Verify mocks
		this.verifyMockObjects();
	}
}
