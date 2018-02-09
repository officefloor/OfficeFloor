/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
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
package net.officefloor.web.resource.build;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;

import net.officefloor.compile.impl.structure.OfficeNodeImpl;
import net.officefloor.compile.issues.CompilerIssue;
import net.officefloor.compile.spi.office.OfficeEscalation;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.test.issues.MockCompilerIssues;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.compile.CompileWebContext;
import net.officefloor.web.compile.WebCompileOfficeFloor;
import net.officefloor.web.resource.HttpFile;
import net.officefloor.web.resource.HttpResource;
import net.officefloor.web.resource.HttpResourceCache;
import net.officefloor.web.resource.HttpResourceStore;
import net.officefloor.web.resource.classpath.ClasspathResourceSystemService;
import net.officefloor.web.resource.spi.ResourceSystemService;
import net.officefloor.web.resource.spi.ResourceTransformerService;
import net.officefloor.web.security.build.HttpSecurableBuilder;
import net.officefloor.web.security.build.HttpSecurityArchitect;
import net.officefloor.web.security.build.HttpSecurityArchitectEmployer;
import net.officefloor.web.security.scheme.MockChallengeHttpSecuritySource;
import net.officefloor.web.security.scheme.MockCredentials;

/**
 * Tests the {@link HttpResourceArchitect}.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpResourceArchitectTest extends OfficeFrameTestCase {

	/**
	 * {@link WebCompileOfficeFloor}.
	 */
	private final WebCompileOfficeFloor compile = new WebCompileOfficeFloor();

	/**
	 * {@link HttpSecurityArchitect}.
	 */
	private HttpSecurityArchitect securityArchitect;

	/**
	 * {@link MockHttpServer}.
	 */
	private MockHttpServer server;

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	@Override
	protected void setUp() throws Exception {
		this.compile.officeFloor((context) -> {
			this.server = MockHttpServer.configureMockHttpServer(context.getDeployedOffice()
					.getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME, WebArchitect.HANDLER_INPUT_NAME));
		});
	}

	@Override
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can send resource.
	 */
	public void testSectionOutputToResource() throws Exception {
		this.compile((context, resource) -> {
			OfficeSection section = context.addSection("section", OutputToResourceServicer.class);
			resource.link(section.getOfficeSectionOutput("resource"), "resource.html");
			context.getWebArchitect().link(false, "/path", section.getOfficeSectionInput("service"));
		});

		// Send the request
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(200, "TEST RESOURCE");

		// Send again to service from cache
		response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(200, "TEST RESOURCE");
	}

	public static class OutputToResourceServicer {
		@NextFunction("resource")
		public void service() {
		}
	}

	/**
	 * Ensure issue if missing resource.
	 */
	public void testSectionOutputToMissingResource() throws Exception {
		this.issue((issues) -> issues.recordIssue("OFFICE", OfficeNodeImpl.class,
				"Can not find HTTP resource '/missing.html'"), (context, resource) -> {
					OfficeSection section = context.addSection("section", OutputToResourceServicer.class);
					resource.link(section.getOfficeSectionOutput("resource"), "missing.html");
					context.getWebArchitect().link(false, "/path", section.getOfficeSectionInput("service"));
				});
	}

	/**
	 * Ensure send resource on {@link Escalation}.
	 */
	public void testEscalationToResource() throws Exception {
		this.compile((context, resource) -> {
			context.link(false, "/path", EscalationToResourceServicer.class);
			OfficeEscalation escalation = context.getOfficeArchitect().addOfficeEscalation(Exception.class.getName());
			resource.link(escalation, "resource.html");
		});

		// Ensure handle escalation
		MockHttpResponse response = this.server.send(MockHttpServer.mockRequest("/path"));
		response.assertResponse(200, "TEST RESOURCE");
	}

	public static class EscalationToResourceServicer {
		public void service() throws Exception {
			throw new Exception("TEST");
		}
	}

	/**
	 * Ensure issue in missing resource.
	 */
	public void testEscalationToMissingResource() throws Exception {
		this.issue((issues) -> issues.recordIssue("OFFICE", OfficeNodeImpl.class,
				"Can not find HTTP resource '/missing.html'"), (context, resource) -> {
					context.link(false, "/path", EscalationToResourceServicer.class);
					OfficeEscalation escalation = context.getOfficeArchitect()
							.addOfficeEscalation(Exception.class.getName());
					resource.link(escalation, "missing.html");
				});
	}

	/**
	 * Ensure can service a resource.
	 */
	public void testServiceResource() throws Exception {
		this.compile((context, resource) -> {
			// Use default configuration
		});

		// Ensure can obtain resource
		this.server.send(MockHttpServer.mockRequest("/resource.html")).assertResponse(200, "TEST RESOURCE");

		// Ensure passes through if not found resource
		this.server.send(MockHttpServer.mockRequest("/missing.html")).assertResponse(404,
				"No resource found for /missing.html");
	}

	/**
	 * Ensure can obtain external {@link HttpResource}.
	 */
	public void testDefaultFileResources() throws Exception {
		File resourcesDirectory = this.findFile(this.getClass(), "resources");
		this.compile((context, resource) -> {
			resource.addHttpResources(resourcesDirectory.getAbsolutePath());
		});

		// Ensure can obtain resource
		this.server.send(MockHttpServer.mockRequest("/external.html")).assertResponse(200, "TEST EXTERNAL RESOURCE");

		// Ensure passes through if not found resource
		this.server.send(MockHttpServer.mockRequest("/missing.html")).assertResponse(404,
				"No resource found for /missing.html");
	}

	/**
	 * Ensure issue if protocol implementation not available.
	 */
	public void testProtocolNotAvailable() throws Exception {
		this.issue((issues) -> issues.recordIssue(
				"Resource 'missing protocol' not available.  Please ensure its " + ResourceSystemService.class.getName()
						+ " implementation is on the class path and configured as a service."),
				(context, resource) -> {
					resource.addHttpResources("missing protocol:location");
				});
	}

	public void testConfiguredProtocolResource() throws Exception {
		File resourcesDirectory = this.findFile(this.getClass(), "resources");
		this.compile((context, resource) -> {
			resource.addHttpResources("file:" + resourcesDirectory.getAbsolutePath());
		});

		// Ensure can obtain resource
		this.server.send(MockHttpServer.mockRequest("/external.html")).assertResponse(200, "TEST EXTERNAL RESOURCE");
	}

	/**
	 * Ensure can secure external {@link HttpResource}.
	 */
	public void testSecureConfiguredResources() throws Exception {
		File resourcesDirectory = this.findFile(this.getClass(), "resources");
		File securedDirectory = this.findFile(this.getClass(), "secured");
		this.compile((context, resource) -> {

			// Configure secured resources
			this.securityArchitect.addHttpSecurity("secure", new MockChallengeHttpSecuritySource("secure"));
			HttpResourcesBuilder external = resource.addHttpResources(securedDirectory.getAbsolutePath());
			HttpSecurableBuilder securable = external.getHttpSecurer();
			securable.setHttpSecurityName("secure");
			securable.addRole("test");

			// Configure non-secured resources
			resource.addHttpResources(resourcesDirectory.getAbsolutePath());
		});

		// Ensure can obtain non-secured resource
		this.server.send(MockHttpServer.mockRequest("/external.html")).assertResponse(200, "TEST EXTERNAL RESOURCE");

		// Ensure secured resources (not available to be found)
		this.server.send(MockHttpServer.mockRequest("/secured.html")).assertResponse(404,
				"No resource found for /secured.html");

		// Ensure no access if not in roles
		this.server.send(new MockCredentials("not", "not").loadHttpRequest(MockHttpServer.mockRequest("/secured.html")))
				.assertResponse(404, "No resource found for /secured.html");

		// Ensure can obtain once authenticated with appropriate role
		this.server
				.send(new MockCredentials("test", "test").loadHttpRequest(MockHttpServer.mockRequest("/secured.html")))
				.assertResponse(200, "TEST SECURED RESORUCE");
	}

	/**
	 * Ensure can provide context path to resources in servicing.
	 */
	public void testContextPath() throws Exception {
		this.compile((context, resource) -> {
			HttpResourcesBuilder resources = resource.addHttpResources(new ClasspathResourceSystemService(), "PUBLIC");
			resources.setContextPath("context");
		});

		// Ensure requires context path
		this.server.send(MockHttpServer.mockRequest("/resource.html")).assertResponse(404,
				"No resource found for /resource.html");

		// Ensure available with context path
		this.server.send(MockHttpServer.mockRequest("/context/resource.html")).assertResponse(200, "TEST RESOURCE");

		// Ensure default resource with context path
		this.server.send(MockHttpServer.mockRequest("/context")).assertResponse(200, "<html><body>test</body></html>");
	}

	/**
	 * Ensure can auto-wire specific resources via type qualification.
	 */
	public void testTypeQualifier() throws Exception {
		this.compile((context, resource) -> {
			HttpResourcesBuilder resources = resource.addHttpResources(new ClasspathResourceSystemService(), "PUBLIC");
			resources.addTypeQualifier("qualifier");

			// Configure linking servicer
			OfficeSection servicer = context.addSection("servicer", TypeQualifierServicer.class);
			context.getWebArchitect().link(false, "/store", servicer.getOfficeSectionInput("store"));
			context.getWebArchitect().link(false, "/cache", servicer.getOfficeSectionInput("cache"));
		});

		// Ensure not cached
		this.server.send(MockHttpServer.mockRequest("/cache")).assertResponse(200, "Not cached");

		// Ensure can obtain from store
		this.server.send(MockHttpServer.mockRequest("/store")).assertResponse(200, "TEST RESOURCE");

		// Ensure can obtain from cache
		this.server.send(MockHttpServer.mockRequest("/cache")).assertResponse(200, "TEST RESOURCE");
	}

	public static class TypeQualifierServicer {
		public void store(@Parameter HttpResourceStore store, ServerHttpConnection connection) throws IOException {
			HttpFile file = (HttpFile) store.getHttpResource("/resource.html");
			file.writeTo(connection.getResponse());
		}

		public void cache(@Parameter HttpResourceCache cache, ServerHttpConnection connection) throws IOException {
			HttpFile file = (HttpFile) cache.getHttpResource("/resource.html");
			if (file == null) {
				connection.getResponse().getEntityWriter().append("Not cached");
			} else {
				file.writeTo(connection.getResponse());
			}
		}
	}

	/**
	 * Ensure can transform the resources.
	 */
	public void testResourceTransformer() {
		fail("TODO implement");
	}

	/**
	 * Ensure issue if {@link ResourceTransformerService} is not available.
	 */
	public void testResourceTransformerServiceNotAvailable() {
		fail("TODO implement");
	}

	/**
	 * Ensure can configure {@link ResourceSystemService}.
	 */
	public void testResourceTransformerService() {
		fail("TODO implement");
	}

	/**
	 * Ensure can change the default directory resource name.
	 */
	public void testChangeDirectoryDefaultResource() {
		fail("TODO impelement");
	}

	/**
	 * Initialises the {@link HttpResourceArchitect}.
	 */
	private static interface Initialiser {

		/**
		 * Initialises the {@link HttpResourceArchitect}.
		 * 
		 * @param context
		 *            {@link CompileWebContext}.
		 * @param resource
		 *            {@link HttpResourceArchitect}.
		 */
		void initialise(CompileWebContext context, HttpResourceArchitect resource);
	}

	/**
	 * Compiles with the {@link Initialiser}.
	 * 
	 * @param initialiser
	 *            {@link Initialiser}.
	 */
	private void compile(Initialiser initialiser) throws Exception {
		this.compile.web((context) -> {
			this.securityArchitect = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(
					context.getWebArchitect(), context.getOfficeArchitect(), context.getOfficeSourceContext());
			HttpResourceArchitect resource = HttpResourceArchitectEmployer.employHttpResourceArchitect(
					context.getWebArchitect(), this.securityArchitect, context.getOfficeArchitect(),
					context.getOfficeSourceContext());
			initialiser.initialise(context, resource);
			resource.informWebArchitect();
			this.securityArchitect.informWebArchitect();
		});
		this.officeFloor = this.compile.compileAndOpenOfficeFloor();
	}

	/**
	 * Ensure issue with compiling.
	 * 
	 * @param issueRecorder
	 *            Records the {@link CompilerIssue}.
	 * @param initialiser
	 *            {@link Initialiser}.
	 */
	private void issue(Consumer<MockCompilerIssues> issueRecorder, Initialiser initialiser) throws Exception {

		// Record issue
		MockCompilerIssues issues = new MockCompilerIssues(this);
		issueRecorder.accept(issues);

		// Test
		this.replayMockObjects();
		this.compile.getOfficeFloorCompiler().setCompilerIssues(issues);
		this.compile.web((context) -> {
			this.securityArchitect = HttpSecurityArchitectEmployer.employHttpSecurityArchitect(
					context.getWebArchitect(), context.getOfficeArchitect(), context.getOfficeSourceContext());
			HttpResourceArchitect resource = HttpResourceArchitectEmployer.employHttpResourceArchitect(
					context.getWebArchitect(), this.securityArchitect, context.getOfficeArchitect(),
					context.getOfficeSourceContext());
			initialiser.initialise(context, resource);
			resource.informWebArchitect();
			this.securityArchitect.informWebArchitect();
		});
		this.officeFloor = this.compile.compileOfficeFloor();
		assertNull("Should not compile", this.officeFloor);

		// Ensure issue
		this.verifyMockObjects();
	}

}