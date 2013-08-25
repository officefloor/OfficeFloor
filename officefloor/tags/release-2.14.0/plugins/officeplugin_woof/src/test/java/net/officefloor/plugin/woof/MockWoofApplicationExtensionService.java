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
package net.officefloor.plugin.woof;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import junit.framework.TestCase;
import net.officefloor.autowire.AutoWireSection;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.plugin.socket.server.http.ServerHttpConnection;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
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

		// Validate property
		TestCase.assertEquals("Incorrect property", "VALUE",
				context.getProperty("CHAIN.TEST"));

		// Validate class loader
		TestCase.assertNotNull("Must have class loader",
				context.getClassLoader());

		// Validate resource
		TestCase.assertEquals("Incorrect resource text", "EXTENSION",
				this.getResourceContent(context,
						"ApplicationExtension.woof.config"));

		// Validate the webapp directory
		TestCase.assertEquals("Incorrect webapp access", "<web-app />",
				this.getResourceContent(context, "WEB-INF/web.xml"));

		// Configure the servicer
		WebAutoWireApplication app = context.getWebApplication();
		AutoWireSection servicer = app.addSection("CHAIN",
				ClassSectionSource.class.getName(),
				ChainServicer.class.getName());

		// Chain in the servicer
		app.chainServicer(servicer, "service", "notHandled");
	}

	/**
	 * Obtains the content of the resource.
	 * 
	 * @param context
	 *            {@link WoofApplicationExtensionServiceContext}.
	 * @param resourcePath
	 *            Path to the resource.
	 * @return Content of the resource.
	 */
	private String getResourceContent(
			WoofApplicationExtensionServiceContext context, String resourcePath)
			throws IOException {

		// Obtain the resource content
		InputStream resource = context.getResource(resourcePath);
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		for (int value = resource.read(); value != -1; value = resource.read()) {
			buffer.write(value);
		}
		String resourceText = new String(buffer.toByteArray());

		// Return the resource content
		return resourceText;
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
			if ("/chain".equals(connection.getHttpRequest().getRequestURI())) {
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