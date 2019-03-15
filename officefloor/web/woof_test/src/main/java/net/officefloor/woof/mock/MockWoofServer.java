/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.woof.mock;

import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.mock.MockHttpServer;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.woof.WoofLoaderExtensionService;
import net.officefloor.woof.WoofLoaderExtensionService.WoofLoaderRunnableContext;

/**
 * <p>
 * {@link MockHttpServer} loading the WoOF application.
 * <p>
 * This provides convenient means to test WoOF applications.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofServer extends MockHttpServer implements AutoCloseable {

	/**
	 * Enables configuring the {@link MockWoofServer}.
	 */
	@FunctionalInterface
	public static interface MockWoofServerConfigurer {

		/**
		 * Allows overriding the configuration of the {@link MockWoofServer}.
		 * 
		 * @param context  {@link WoofLoaderRunnableContext}.
		 * @param compiler {@link CompileOfficeFloor}.
		 * @throws Exception If fails to configure.
		 */
		void configure(WoofLoaderRunnableContext context, CompileOfficeFloor compiler) throws Exception;
	}

	/**
	 * Opens the {@link MockWoofServer}.
	 * 
	 * @param configurers {@link MockWoofServerConfigurer} instances.
	 * @return {@link MockWoofServer}.
	 * @throws Exception If fails to start the {@link MockWoofServer}.
	 */
	public static MockWoofServer open(MockWoofServerConfigurer... configurers) throws Exception {

		// Create the server
		MockWoofServer server = new MockWoofServer();

		// Return the open server
		return open(server, configurers);
	}

	/**
	 * Opens the {@link MockWoofServer}.
	 * 
	 * @param server      {@link MockWoofServer}.
	 * @param configurers {@link MockWoofServerConfigurer} instances.
	 * @return Input {@link MockWoofServer}.
	 * @throws Exception If fails to open the {@link MockWoofServer}.
	 */
	protected static MockWoofServer open(MockWoofServer server, MockWoofServerConfigurer... configurers)
			throws Exception {

		// Undertake compiling (without HTTP Server loading)
		return WoofLoaderExtensionService.contextualLoad((loadContext) -> {

			// Mock the HTTP Server, so do not load
			loadContext.notLoadHttpServer();

			// Compile the OfficeFloor to run the server
			CompileOfficeFloor compiler = new CompileOfficeFloor();
			compiler.officeFloor((context) -> {

				// Configure server to service requests
				DeployedOfficeInput input = context.getDeployedOffice()
						.getDeployedOfficeInput(WebArchitect.HANDLER_SECTION_NAME, WebArchitect.HANDLER_INPUT_NAME);
				MockHttpServer.configureMockHttpServer(server, input);
			});
			compiler.office((context) -> {
				// Configured by WoOF extension
			});
			for (MockWoofServerConfigurer configurer : configurers) {
				configurer.configure(loadContext, compiler);
			}
			server.officeFloor = compiler.compileAndOpenOfficeFloor();

			// Return the server
			return server;
		});
	}

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * Obtains the {@link OfficeFloor}.
	 * 
	 * @return {@link OfficeFloor}.
	 */
	public OfficeFloor getOfficeFloor() {
		return this.officeFloor;
	}

	/*
	 * =================== AutoCloseable =======================
	 */

	@Override
	public void close() throws Exception {
		this.officeFloor.closeOfficeFloor();
	}

}