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
package net.officefloor.plugin.web.http.session;

import java.io.IOException;
import java.io.OutputStream;

import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.internal.structure.ManagedObjectScope;
import net.officefloor.frame.test.MockTeamSource;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.frame.test.ReflectiveWorkBuilder.ReflectiveTaskBuilder;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpTestUtil;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.server.HttpServicerTask;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Tests the {@link HttpSessionManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSessionManagedObjectSourceTest extends MockHttpServer {

	/*
	 * =================== HttpServicerBuilder ==========================
	 */

	@Override
	public HttpServicerTask buildServicer(String managedObjectName,
			MockHttpServer server) throws Exception {

		// Register the Http Session
		ManagedObjectBuilder<Indexed> session = server.constructManagedObject(
				"HTTP_SESSION", HttpSessionManagedObjectSource.class,
				this.getOfficeName());
		session.setTimeout(1000);

		// Register the servicer
		ReflectiveWorkBuilder work = server.constructWork(new Servicer(),
				"Servicer", null);
		ReflectiveTaskBuilder task = work.buildTask("service", "SERVICER");
		task.buildObject(managedObjectName);
		DependencyMappingBuilder sessionDependencies = task.buildObject(
				"HTTP_SESSION", ManagedObjectScope.PROCESS);
		sessionDependencies.mapDependency(0, managedObjectName);

		// Register team for servicer
		server.constructTeam("SERVICER",
				MockTeamSource.createOnePersonTeam("SERVICER"));

		// Return the servicer task
		return new HttpServicerTask("Servicer", "service");
	}

	/*
	 * ======================== Tests ==================================
	 */

	/**
	 * Ensure state is maintained by the {@link HttpSession} across multiple
	 * {@link HttpRequest} calls made by a user.
	 */
	public void testHttpSessionStateAcrossCalls() throws Exception {

		// Loop calling server (HttpClient should send back Session Id)
		try (CloseableHttpClient client = HttpTestUtil.createHttpClient()) {
			for (int i = 0; i < 10; i++) {

				// Call the server
				HttpGet request = new HttpGet(this.getServerUrl());
				HttpResponse response = client.execute(request);
				int status = response.getStatusLine().getStatusCode();
				String callIndex = HttpTestUtil.getEntityBody(response);

				// Ensure results match and call index remembered by Session
				assertEquals("Call should be successful", 200, status);
				assertEquals("Incorrect call index", String.valueOf(i),
						callIndex);
			}
		}
	}

	/**
	 * Services the {@link HttpRequest}.
	 */
	public static class Servicer {

		/**
		 * Services the {@link HttpRequest}.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 * @param session
		 *            {@link HttpSession}.
		 */
		public void service(ServerHttpConnection connection, HttpSession session)
				throws IOException {

			// Obtain the current call index
			Integer callIndex = (Integer) session.getAttribute("CALL_INDEX");

			// Increment the call index and store for next call
			callIndex = new Integer(callIndex == null ? 0
					: (callIndex.intValue() + 1));
			session.setAttribute("CALL_INDEX", callIndex);

			// Return the call index
			OutputStream response = connection.getHttpResponse().getEntity();
			response.write(String.valueOf(callIndex.intValue()).getBytes());
		}
	}

}