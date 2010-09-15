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
package net.officefloor.plugin.socket.server.http.security.integrate;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

import net.officefloor.compile.spi.work.source.WorkSource;
import net.officefloor.compile.test.work.WorkLoaderUtil;
import net.officefloor.compile.work.WorkType;
import net.officefloor.frame.api.build.DependencyMappingBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.ManagedObjectBuilder;
import net.officefloor.frame.api.build.ManagingOfficeBuilder;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.build.TaskBuilder;
import net.officefloor.frame.api.build.WorkBuilder;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.impl.spi.team.OnePersonTeamSource;
import net.officefloor.frame.impl.spi.team.WorkerPerTaskTeamSource;
import net.officefloor.frame.internal.structure.FlowInstigationStrategyEnum;
import net.officefloor.frame.spi.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.test.AbstractOfficeConstructTestCase;
import net.officefloor.frame.test.ReflectiveWorkBuilder;
import net.officefloor.plugin.socket.server.http.HttpRequest;
import net.officefloor.plugin.socket.server.http.HttpResponse;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.socket.server.http.security.HttpSecurity;
import net.officefloor.plugin.socket.server.http.security.HttpSecurityService;
import net.officefloor.plugin.socket.server.http.security.HttpSecurityServiceManagedObjectSource;
import net.officefloor.plugin.socket.server.http.security.HttpSecurityTask;
import net.officefloor.plugin.socket.server.http.security.HttpSecurityWorkSource;
import net.officefloor.plugin.socket.server.http.security.HttpSecurityTask.DependencyKeys;
import net.officefloor.plugin.socket.server.http.security.HttpSecurityTask.FlowKeys;
import net.officefloor.plugin.socket.server.http.security.scheme.BasicHttpSecuritySource;
import net.officefloor.plugin.socket.server.http.security.store.PasswordFileManagedObjectSource;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;
import net.officefloor.plugin.socket.server.http.session.source.HttpSessionManagedObjectSource;
import net.officefloor.plugin.socket.server.http.source.HttpServerSocketManagedObjectSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

/**
 * Integrate tests the {@link HttpSecurity} {@link WorkSource} and
 * {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecurityIntegrateTest extends AbstractOfficeConstructTestCase {

	/**
	 * Port to use for testing.
	 */
	private final int PORT = MockHttpServer.getAvailablePort();

	/**
	 * {@link HttpClient} to use for testing.
	 */
	private final HttpClient client = new HttpClient();

	@Override
	@SuppressWarnings("unchecked")
	protected void setUp() throws Exception {
		super.setUp();

		// Setup the integrated environment
		String officeName = this.getOfficeName();

		// HTTP Security Service
		ManagedObjectBuilder<None> httpSecurityService = this
				.constructManagedObject("HttpSecurityService",
						HttpSecurityServiceManagedObjectSource.class);
		httpSecurityService
				.addProperty(
						HttpSecurityServiceManagedObjectSource.PROPERTY_AUTHENTICATION_SCHEME,
						HttpSecurityServiceManagedObjectSource.BASIC_AUTHENTICATION_SCHEME);
		httpSecurityService.addProperty(BasicHttpSecuritySource.PROPERTY_REALM,
				"TestRealm");
		httpSecurityService.setManagingOffice(officeName);
		DependencyMappingBuilder httpSecurityServiceDependencies = this
				.getOfficeBuilder().addProcessManagedObject(
						"HttpSecurityService", "HttpSecurityService");
		httpSecurityServiceDependencies
				.mapDependency(0, "ServerHttpConnection");
		httpSecurityServiceDependencies.mapDependency(1, "HttpSession");
		httpSecurityServiceDependencies.mapDependency(2, "CredentialStore");

		// Password File Credential Store
		String passwordFilePath = this.findFile(this.getClass(),
				"password-file.txt").getAbsolutePath();
		ManagedObjectBuilder<None> passwordFile = this.constructManagedObject(
				"CredentialStore", PasswordFileManagedObjectSource.class);
		passwordFile.addProperty(
				PasswordFileManagedObjectSource.PROPERTY_PASSWORD_FILE_PATH,
				passwordFilePath);
		passwordFile.setManagingOffice(officeName);
		this.getOfficeBuilder().addProcessManagedObject("CredentialStore",
				"CredentialStore");

		// HTTP Session
		ManagedObjectBuilder<Indexed> httpSession = this
				.constructManagedObject("HttpSession",
						HttpSessionManagedObjectSource.class);
		httpSession.setTimeout(1000);
		httpSession.setManagingOffice(officeName);
		DependencyMappingBuilder httpSessionDependencies = this
				.getOfficeBuilder().addProcessManagedObject("HttpSession",
						"HttpSession");
		httpSessionDependencies.mapDependency(0, "ServerHttpConnection");

		// Server HTTP Connection
		ManagedObjectBuilder<Indexed> serverHttpConnection = this
				.constructManagedObject("ServerHttpConnection",
						HttpServerSocketManagedObjectSource.class);
		serverHttpConnection.addProperty(
				HttpServerSocketManagedObjectSource.PROPERTY_PORT, String
						.valueOf(PORT));
		ManagingOfficeBuilder<Indexed> connection = serverHttpConnection
				.setManagingOffice(officeName);
		connection.linkProcess(0, "Access", "access");
		connection.setInputManagedObjectName("ServerHttpConnection");

		// Work to secure access to servicer
		WorkType<HttpSecurityTask> accessWorkType = WorkLoaderUtil
				.loadWorkType(HttpSecurityWorkSource.class);
		WorkBuilder<HttpSecurityTask> accessWork = this.constructWork("Access",
				accessWorkType.getWorkFactory());
		TaskBuilder<HttpSecurityTask, DependencyKeys, FlowKeys> accessTask = (TaskBuilder<HttpSecurityTask, DependencyKeys, FlowKeys>) accessWork
				.addTask("access", accessWorkType.getTaskTypes()[0]
						.getTaskFactory());
		accessTask.linkManagedObject(DependencyKeys.HTTP_SECURITY_SERVICE,
				"HttpSecurityService", HttpSecurityService.class);
		accessTask.linkFlow(FlowKeys.AUTHENTICATED, "Servicer", "service",
				FlowInstigationStrategyEnum.SEQUENTIAL, HttpSecurity.class);
		accessTask.linkFlow(FlowKeys.UNAUTHENTICATED, "Servicer",
				"unauthorised", FlowInstigationStrategyEnum.SEQUENTIAL,
				HttpSecurity.class);
		accessTask.setTeam("Team");

		// Work to service request
		ReflectiveWorkBuilder servicer = this.constructWork(new Servicer(),
				"Servicer", null);
		servicer.buildTask("service", "Team").buildObject(
				"ServerHttpConnection");
		servicer.buildTask("unauthorised", "Team").buildObject(
				"ServerHttpConnection");

		// Team to service requests
		this.constructTeam("Team", OnePersonTeamSource.class);

		// Teams for Server HTTP Connection
		this.constructTeam("of-ServerHttpConnection.cleanup",
				OnePersonTeamSource.class);
		this.constructTeam("of-ServerHttpConnection.accepter",
				OnePersonTeamSource.class);
		this.constructTeam("of-ServerHttpConnection.listener",
				WorkerPerTaskTeamSource.class);

		// Start the office
		OfficeFloor officeFloor = this.constructOfficeFloor();
		officeFloor.openOfficeFloor();
	}

	/**
	 * Services the {@link HttpRequest}.
	 */
	public class Servicer {

		/**
		 * Services the {@link HttpRequest}.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 * @throws IOException
		 *             If fails.
		 */
		public void service(ServerHttpConnection connection) throws IOException {
			HttpResponse response = connection.getHttpResponse();
			Writer writer = new OutputStreamWriter(response.getBody()
					.getOutputStream());
			writer.write("Serviced");
			writer.flush();
			response.send();
		}

		/**
		 * Services an unauthorised {@link HttpRequest}.
		 * 
		 * @param connection
		 *            {@link ServerHttpConnection}.
		 * @throws IOException
		 *             If fails.
		 */
		public void unauthorised(ServerHttpConnection connection)
				throws IOException {
			HttpResponse response = connection.getHttpResponse();
			Writer writer = new OutputStreamWriter(response.getBody()
					.getOutputStream());
			writer.write("Please try again");
			writer.flush();
			response.send();
		}
	}

	/**
	 * Ensure can integrate.
	 */
	public void testIntegration() throws Exception {

		// Create the request
		GetMethod method = new GetMethod("http://localhost:" + PORT);

		// Should not authenticate (without credentials)
		this.doRequest(method, 401, "Please try again");

		// Should authenticate with credentials
		this.client.getState().setCredentials(
				new AuthScope(null, -1, "TestRealm"),
				new UsernamePasswordCredentials("daniel", "password"));
		method.setDoAuthentication(true);
		this.doRequest(method, 200, "Serviced");
	}

	/**
	 * Asserts the response from the {@link GetMethod}.
	 * 
	 * @param method
	 *            {@link GetMethod}.
	 */
	private void doRequest(GetMethod method, int expectedStatus,
			String expectedBodyContent) {
		try {
			// Execute the method
			int status = this.client.executeMethod(method);

			// Verify response
			assertEquals("Should be successful", expectedStatus, status);
			assertEquals("Incorrect response body", expectedBodyContent, method
					.getResponseBodyAsString());

		} catch (Exception ex) {
			throw fail(ex);
		} finally {
			// Ensure release connection
			method.releaseConnection();
		}
	}

}