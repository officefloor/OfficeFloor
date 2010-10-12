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
package net.officefloor.plugin.servlet.webxml;

import java.io.File;
import java.io.FileInputStream;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.Servlet;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.spi.section.SectionDesigner;
import net.officefloor.compile.spi.section.source.SectionSourceContext;
import net.officefloor.compile.test.issues.FailCompilerIssues;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.impl.repository.xml.XmlConfigurationContext;
import net.officefloor.model.repository.ConfigurationItem;
import net.officefloor.plugin.socket.server.http.server.MockHttpServer;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Abstract functionality for <code>web.xml</code> testing.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractWebXmlTestCase extends OfficeFrameTestCase {

	/**
	 * Created {@link HttpClient} instances.
	 */
	private final List<HttpClient> clients = new LinkedList<HttpClient>();

	/**
	 * {@link OfficeFloor} for the Servlet application.
	 */
	private OfficeFloor officeFloor = null;

	/**
	 * Port that {@link Servlet} application will be listening on for requests.
	 */
	private int port;

	@Override
	protected void tearDown() throws Exception {

		// Stop the HTTP clients
		for (HttpClient client : this.clients) {
			client.getConnectionManager().shutdown();
		}

		// Stop servlet application (if started)
		if (this.officeFloor != null) {
			this.stopServletApplication();
		}
	}

	/**
	 * Starts the {@link Servlet} application for the input <code>web.xml</code>
	 * file.
	 * 
	 * @param webXmlFileName
	 *            Name of the <code>web.xml</code> file to configure the
	 *            {@link Servlet} application.
	 */
	protected void startServletApplication(String webXmlFileName) {
		try {
			// Obtain the port for the application
			this.port = MockHttpServer.getAvailablePort();

			// Create the configuration context
			XmlConfigurationContext context = new XmlConfigurationContext(this,
					"OfficeFloor.xml");
			context.addTag("web.xml.file.name", webXmlFileName);
			context.addTag("port", String.valueOf(this.port));

			// Create and configure the compiler
			OfficeFloorCompiler compiler = OfficeFloorCompiler
					.newOfficeFloorCompiler();
			compiler.setConfigurationContext(context);
			compiler.setCompilerIssues(new FailCompilerIssues());

			// Compiler the Office Floor
			this.officeFloor = compiler.compile("office-floor");

			// Open (start) the Office Floor for the Servlet application
			this.officeFloor.openOfficeFloor();

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Creates a {@link HttpClient}.
	 * 
	 * @return {@link HttpClient}.
	 */
	protected HttpClient createHttpClient() {
		HttpClient client = new DefaultHttpClient();
		this.clients.add(client);
		return client;
	}

	/**
	 * Obtains the URL of the server ({@link Servlet} application).
	 * 
	 * @return URL of the server.
	 */
	protected String getServerUrl() {
		return "http://localhost:" + this.port;
	}

	/**
	 * Stops the {@link Servlet} application.
	 */
	protected void stopServletApplication() {
		this.officeFloor.closeOfficeFloor();
		this.officeFloor = null;
	}

	/**
	 * Ensure correct design.
	 */
	protected void doDesignTest(String webXmlFileName, DesignRecorder recorder) {
		try {

			// Mocks
			final SectionSourceContext context = this
					.createMock(SectionSourceContext.class);
			final ConfigurationItem item = this
					.createMock(ConfigurationItem.class);
			final SectionDesigner designer = this
					.createMock(SectionDesigner.class);

			// Obtain the web.xml configuration
			File webXmlConfiguration = this.findFile(this.getClass(),
					webXmlFileName);

			// Record obtaining the web.xml configuration
			this.recordReturn(context, context.getSectionLocation(),
					webXmlFileName);
			this.recordReturn(context,
					context.getConfiguration(webXmlFileName), item);
			this.recordReturn(item, item.getConfiguration(),
					new FileInputStream(webXmlConfiguration));

			// Do further recording
			if (recorder != null) {
				recorder.record(designer);
			}

			// Test
			this.replayMockObjects();
			new WebXmlSectionSource().sourceSection(designer, context);
			this.verifyMockObjects();

		} catch (Exception ex) {
			throw fail(ex);
		}
	}

	/**
	 * Records design of section.
	 */
	protected static interface DesignRecorder {

		/**
		 * Records design of the {@link Servlet} application section.
		 * 
		 * @param designer
		 *            Mock {@link SectionDesigner} to record design.
		 */
		void record(SectionDesigner designer);
	}

}