/*-
 * #%L
 * Web on OfficeFloor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.woof;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import junit.framework.TestCase;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeSection;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.section.clazz.ClassSectionSource;
import net.officefloor.server.http.ServerHttpConnection;
import net.officefloor.web.build.WebArchitect;

/**
 * Mock {@link WoofLoaderSettings} for testing.
 * 
 * @author Daniel Sagenschneider
 */
public class MockWoofExtensionService implements WoofExtensionService {

	/*
	 * =================== WoofExtensionService ================
	 */

	@Override
	public void extend(WoofContext context) throws Exception {

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
	 *            {@link WoofContext}.
	 * @param resourcePath
	 *            Path to the resource.
	 * @return Content of the resource.
	 */
	private String getResourceContent(WoofContext context, String resourcePath) throws IOException {

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
