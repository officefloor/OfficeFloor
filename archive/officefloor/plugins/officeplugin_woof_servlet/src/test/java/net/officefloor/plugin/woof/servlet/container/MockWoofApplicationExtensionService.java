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
package net.officefloor.plugin.woof.servlet.container;

import java.io.IOException;
import java.io.Writer;

import junit.framework.TestCase;

import net.officefloor.autowire.AutoWireSection;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.WebArchitect;
import net.officefloor.plugin.woof.WoofApplicationExtensionService;
import net.officefloor.plugin.woof.WoofApplicationExtensionServiceContext;
import net.officefloor.plugin.work.clazz.FlowInterface;

/**
 * Mock {@link WoofApplicationExtensionService} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofApplicationExtensionService implements
		WoofApplicationExtensionService {

	/*
	 * =================== WoofApplicationExtensionService ================
	 */

	@Override
	public void extendApplication(WoofApplicationExtensionServiceContext context)
			throws Exception {

		// Ensure property configured from init parameters
		TestCase.assertEquals("Incorrect property", "INIT_VALUE",
				context.getProperty("INIT_NAME"));

		// Configure the servicer
		WebArchitect app = context.getWebApplication();
		AutoWireSection servicer = app.addSection("CHAIN",
				ClassSectionSource.class.getName(),
				ChainServicer.class.getName());

		// Chain in the servicer
		app.chainServicer(servicer, "service", "notHandled");
	}

	/**
	 * Chained servicer.
	 */
	public static class ChainServicer {

		@FlowInterface
		public static interface Flows {
			void notHandled();
		}

		public void service(ServerHttpConnection connection, Flows flows)
				throws IOException {
			if ("/chain.html".equals(connection.getHttpRequest().getRequestURI())) {
				// Provide chained response
				Writer writer = connection.getHttpResponse().getEntityWriter();
				writer.write("CHAINED");
				writer.flush();

			} else {
				// Not handled
				flows.notHandled();
			}
		}
	}

}