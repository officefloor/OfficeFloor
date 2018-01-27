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
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.plugin.managedfunction.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.WebArchitect;

/**
 * Mock {@link WoofLoaderExtensionService} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofExtensionService implements WoofExtensionService {

	/*
	 * =================== WoofExtensionService ================
	 */

	@Override
	public void extend(WoofExtensionServiceContext context) throws Exception {

		// Obtain the extension context
		OfficeExtensionContext extensionContext = context.getOfficeExtensionContext();

		// Validate property
		TestCase.assertEquals("Incorrect property", "VALUE", extensionContext.getProperty("CHAIN.TEST"));

		// Validate class loader
		TestCase.assertNotNull("Must have class loader", extensionContext.getClassLoader());

		// Validate resource
		TestCase.assertEquals("Incorrect resource text", "EXTENSION",
				this.getResourceContent(context, "ApplicationExtension.woof.config"));

		// Validate the webapp directory
		TestCase.assertEquals("Incorrect webapp access", "<web-app />",
				this.getResourceContent(context, "WEB-INF/web.xml"));

		// Configure the servicer
		OfficeArchitect office = context.getOfficeArchitect();
		OfficeSection servicer = office.addOfficeSection("CHAIN", ClassSectionSource.class.getName(),
				ChainServicer.class.getName());

		// Chain in the servicer
		WebArchitect web = context.getWebArchitect();
		web.chainServicer(servicer.getOfficeSectionInput("service"), servicer.getOfficeSectionOutput("notHandled"));
	}

	/**
	 * Obtains the content of the resource.
	 * 
	 * @param context
	 *            {@link WoofExtensionServiceContext}.
	 * @param resourcePath
	 *            Path to the resource.
	 * @return Content of the resource.
	 */
	private String getResourceContent(WoofExtensionServiceContext context, String resourcePath) throws IOException {

		// Obtain the resource content
		InputStream resource = context.getOfficeExtensionContext().getResource(resourcePath);
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

		public void service(ServerHttpConnection connection, Flows flows) throws IOException {
			if ("/chain".equals(connection.getRequest().getUri())) {
				// Provide chained response
				Writer writer = connection.getResponse().getEntityWriter();
				writer.write("CHAINED");
				writer.flush();

			} else {
				// Not handled
				flows.notHandled();
			}
		}
	}

}