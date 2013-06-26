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
package net.officefloor.plugin.woof.template;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.spi.source.SourceContext;
import net.officefloor.frame.spi.source.UnknownClassError;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.web.http.application.HttpTemplateAutoWireSection;
import net.officefloor.plugin.web.http.application.WebAutoWireApplication;
import net.officefloor.plugin.woof.template.impl.AbstractWoofTemplateExtensionSource;

/**
 * Tests the {@link WoofTemplateExtensionLoader} extending the
 * {@link HttpTemplateAutoWireSection}.
 * 
 * @author Daniel Sagenschneider
 */
public class ExtendWoofTemplateExtensionLoaderTest extends OfficeFrameTestCase {

	/**
	 * {@link WoofTemplateExtensionLoader} to test.
	 */
	private final WoofTemplateExtensionLoader loader = new WoofTemplateExtensionLoaderImpl();

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList properties = OfficeFloorCompiler
			.newPropertyList();

	/**
	 * {@link HttpTemplateAutoWireSection}.
	 */
	private final HttpTemplateAutoWireSection template = this
			.createMock(HttpTemplateAutoWireSection.class);

	/**
	 * {@link WebAutoWireApplication}.
	 */
	private final WebAutoWireApplication application = this
			.createMock(WebAutoWireApplication.class);

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext = this
			.createMock(SourceContext.class);

	@Override
	protected void setUp() throws Exception {
		// Reset for each test
		MockWoofTemplateExtensionSource.reset(null);
	}

	/**
	 * Ensure escalate appropriately if fail to instantiate the
	 * {@link MockWoofTemplateExtensionSource}.
	 */
	public void testFailToInstantiateExtension() throws Exception {

		UnknownClassError error = new UnknownClassError("Unknown", "UNKNOWN");

		// Record fail to instantiate
		this.recordReturn(this.sourceContext,
				this.sourceContext.isLoadingType(), false);
		this.sourceContext.loadClass("UNKNOWN");
		this.control(this.sourceContext).setThrowable(error);

		// Test
		try {
			this.extendTemplate("UNKNOWN");
			fail("Should not be successful");
		} catch (WoofTemplateExtensionException ex) {
			assertEquals("Incorrect exception",
					"Failed loading Template Extension UNKNOWN. Unknown",
					ex.getMessage());
			assertSame("Incorrect cause", error, ex.getCause());
		}
	}

	/**
	 * Ensure escalate appropriately if failure in extending the
	 * {@link HttpTemplateAutoWireSection}.
	 */
	public void testExtensionFailure() throws Exception {

		final Exception failure = new Exception("TEST");

		// Record initiate
		this.recordReturn(this.sourceContext,
				this.sourceContext.isLoadingType(), false);
		this.recordReturn(this.sourceContext, this.sourceContext
				.loadClass(MockWoofTemplateExtensionSource.class.getName()),
				MockWoofTemplateExtensionSource.class);

		// Flag to throw exception
		MockWoofTemplateExtensionSource.reset(failure);

		// Test
		try {
			this.extendTemplate(MockWoofTemplateExtensionSource.class.getName());
		} catch (WoofTemplateExtensionException ex) {
			assertEquals("Incorrect exception",
					"Failed loading Template Extension "
							+ MockWoofTemplateExtensionSource.class.getName()
							+ ". TEST", ex.getMessage());
			assertSame("Incorrect cause", failure, ex.getCause());
		}
	}

	/**
	 * Ensure can extend the {@link HttpTemplateAutoWireSection}.
	 */
	public void testExtendTemplate() throws Exception {

		// Record initiate
		this.recordReturn(this.sourceContext,
				this.sourceContext.isLoadingType(), false);
		this.recordReturn(this.sourceContext, this.sourceContext
				.loadClass(MockWoofTemplateExtensionSource.class.getName()),
				MockWoofTemplateExtensionSource.class);

		// Add the property
		this.properties.addProperty("NAME").setValue("VALUE");

		// Record actions on mock objects to ensure correctly available
		this.recordReturn(this.template, this.template.getTemplateUri(), "URI");
		this.recordReturn(this.application, this.application.getURIs(),
				new String[] { "URI" });

		// Test
		this.extendTemplate(MockWoofTemplateExtensionSource.class.getName());
	}

	/**
	 * Extends the {@link HttpTemplateAutoWireSection}.
	 * 
	 * @param extensionSourceClassName
	 *            {@link WoofTemplateExtensionSource} class name.
	 */
	private void extendTemplate(String extensionSourceClassName)
			throws Exception {
		this.replayMockObjects();
		this.loader.extendTemplate(extensionSourceClassName, this.properties,
				this.template, this.application, this.sourceContext);
		this.verifyMockObjects();
	}

	/**
	 * Mock {@link WoofTemplateExtensionSource}.
	 */
	public static class MockWoofTemplateExtensionSource extends
			AbstractWoofTemplateExtensionSource {

		/**
		 * Failure.
		 */
		private static Exception failure = null;

		/**
		 * Resets for the next test.
		 * 
		 * @param failure
		 *            {@link Exception}.
		 */
		public static void reset(Exception failure) {
			MockWoofTemplateExtensionSource.failure = failure;
		}

		/*
		 * ============== WoofTemplateExtensionSource ===================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			fail("Should not require specification for extending the template");
		}

		@Override
		public void extendTemplate(WoofTemplateExtensionSourceContext context)
				throws Exception {

			// Provide failure if specified
			if (failure != null) {
				throw failure;
			}

			// Ensure property is defined
			assertEquals("Incorrect property value", "VALUE",
					context.getProperty("NAME"));
			assertNull("Should not have non-defined property",
					context.getProperty("NOT DEFINED", null));

			// Ensure correct details
			assertEquals("Incorrect template URI", "URI", context.getTemplate()
					.getTemplateUri());
			String[] uris = context.getWebApplication().getURIs();
			assertEquals("Incorrect number of application URIs", 1, uris.length);
			assertEquals("Incorrect application URI", "URI", uris[0]);
		}
	}

}