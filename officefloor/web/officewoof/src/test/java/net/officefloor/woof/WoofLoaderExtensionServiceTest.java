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
package net.officefloor.woof;

import java.io.IOException;
import java.util.logging.LogRecord;

import net.officefloor.frame.test.LoggerAssertion;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.section.clazz.NextFunction;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.server.http.mock.MockHttpResponse;
import net.officefloor.woof.mock.MockWoofServer;

/**
 * Tests the {@link WoofLoaderExtensionService}.
 * 
 * @author Daniel Sagenschneider
 */
public class WoofLoaderExtensionServiceTest extends OfficeFrameTestCase {

	/**
	 * {@link MockWoofServer}.
	 */
	private MockWoofServer server;

	/**
	 * {@link LoggerAssertion} for the {@link WoofLoader}.
	 */
	private LoggerAssertion loaderLoggerAssertion;

	/**
	 * {@link LoggerAssertion} for the {@link WoofLoaderExtensionService}.
	 */
	private LoggerAssertion sourceLoggerAssertion;

	@Override
	protected void setUp() throws Exception {

		// Provide chain servicer extension property
		System.setProperty("CHAIN.TEST", "VALUE");

		// Create the logger assertions
		this.loaderLoggerAssertion = LoggerAssertion.setupLoggerAssertion(WoofLoaderImpl.class.getName());
		this.sourceLoggerAssertion = LoggerAssertion.setupLoggerAssertion(WoofLoaderExtensionService.class.getName());

		// Clear implicit template extension
		MockImplicitWoofTemplateExtensionSourceService.reset();
	}

	@Override
	protected void tearDown() throws Exception {

		// Clear property
		System.clearProperty("CHAIN.TEST");

		// Shutdown
		try {
			if (this.server != null) {
				this.server.close();
			}
		} finally {
			// Disconnect from loggers
			this.sourceLoggerAssertion.disconnectFromLogger();
			this.loaderLoggerAssertion.disconnectFromLogger();
		}
	}

	/**
	 * Ensure can run the application from default configuration.
	 */
	public void testWoOF() throws Exception {

		// Start WoOF application for testing
		this.server = MockWoofServer.open();

		// Validate log not loading unknown extension
		LogRecord[] records = this.sourceLoggerAssertion.getLogRecords();
		StringBuilder messages = new StringBuilder();
		for (LogRecord record : records) {
			messages.append("\t" + record.getMessage() + "\n");
		}
		assertEquals("Incorrect number of records:\n" + messages.toString(), 1, records.length);
		LogRecord record = records[0];
		assertEquals("Incorrect unknown extension log record", WoofLoaderExtensionService.class.getName()
				+ ": Provider woof.application.extension.not.available.Service not found", record.getMessage());

		// Ensure WoOF configuration loaded
		MockHttpResponse response = this.server.send(MockWoofServer.mockRequest("/path"));
		response.assertResponse(200, "TEST");
		
		// Ensure Objects loaded
		
		
		// Ensure Resources loaded
		
		
		// Ensure Teams loaded

	}

	/**
	 * Class for {@link ClassSectionSource} in testing.
	 */
	public static class LinkToResource {
		@NextFunction("resource")
		public void service() {
		}
	}

	/**
	 * Class for {@link ClassSectionSource} in testing.
	 */
	public static class Section {
		public void service(ServerHttpConnection connection, MockDependency dependency) throws IOException {

			// Obtain content to validate objects and teams
			Thread thread = Thread.currentThread();
			String content = "WOOF " + dependency.getMessage() + " " + thread.getName();

			// Write response
			net.officefloor.server.http.HttpResponse response = connection.getResponse();
			response.getEntity().write(content.getBytes());
		}
	}

}