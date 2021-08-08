/*-
 * #%L
 * Web configuration
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

package net.officefloor.woof.template;

import java.util.Collections;

import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.build.WebArchitect;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.woof.template.impl.AbstractWoofTemplateExtensionSource;

/**
 * Tests the {@link WoofTemplateExtensionLoader} extending the
 * {@link WebTemplate}.
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
	private final PropertyList properties = OfficeFloorCompiler.newPropertyList();

	/**
	 * {@link WebTemplate}.
	 */
	private final WebTemplate template = this.createMock(WebTemplate.class);

	/**
	 * {@link OfficeArchitect}.
	 */
	private final OfficeArchitect officeArchitet = this.createMock(OfficeArchitect.class);

	/**
	 * {@link WebArchitect}.
	 */
	private final WebArchitect webArchitect = this.createMock(WebArchitect.class);

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext = this.createMock(SourceContext.class);

	@Override
	protected void setUp() throws Exception {
		// Reset for each test
		MockWoofTemplateExtensionSource.reset(null);
	}

	/**
	 * Ensure escalate appropriately if failure in extending the
	 * {@link WebTemplate}.
	 */
	public void testExtensionFailure() throws Exception {

		final Exception failure = new Exception("TEST");

		// Record initiate
		this.recordReturn(this.sourceContext, this.sourceContext.getName(), "template");
		this.recordReturn(this.sourceContext, this.sourceContext.isLoadingType(), false);
		this.recordReturn(this.sourceContext, this.sourceContext.getProfiles(), Collections.emptyList());
		this.recordReturn(this.sourceContext,
				this.sourceContext.loadClass(MockWoofTemplateExtensionSource.class.getName()),
				MockWoofTemplateExtensionSource.class);

		// Flag to throw exception
		MockWoofTemplateExtensionSource.reset(failure);

		// Test
		try {
			this.extendTemplate(MockWoofTemplateExtensionSource.class);
		} catch (WoofTemplateExtensionException ex) {
			assertEquals("Incorrect exception",
					"Failed loading Template Extension " + MockWoofTemplateExtensionSource.class.getName() + ". TEST",
					ex.getMessage());
			assertSame("Incorrect cause", failure, ex.getCause());
		}
	}

	/**
	 * Ensure can extend the {@link WebTemplate}.
	 */
	public void testExtendTemplate() throws Exception {

		// Record initiate
		this.recordReturn(this.sourceContext, this.sourceContext.getName(), "template");
		this.recordReturn(this.sourceContext, this.sourceContext.isLoadingType(), false);
		this.recordReturn(this.sourceContext, this.sourceContext.getProfiles(), Collections.emptyList());

		// Add the property
		this.properties.addProperty("NAME").setValue("VALUE");

		// Test
		this.extendTemplate(MockWoofTemplateExtensionSource.class);
	}

	/**
	 * Extends the {@link WebTemplate}.
	 * 
	 * @param extensionSourceClass {@link WoofTemplateExtensionSource}
	 *                             {@link Class}.
	 */
	private void extendTemplate(Class<? extends WoofTemplateExtensionSource> extensionSourceClass) throws Exception {
		this.replayMockObjects();
		this.loader.extendTemplate(extensionSourceClass.getDeclaredConstructor().newInstance(), this.properties, "URI",
				this.template, this.officeArchitet, this.webArchitect, this.sourceContext);
		this.verifyMockObjects();
	}

	/**
	 * Mock {@link WoofTemplateExtensionSource}.
	 */
	public static class MockWoofTemplateExtensionSource extends AbstractWoofTemplateExtensionSource {

		/**
		 * Failure.
		 */
		private static Exception failure = null;

		/**
		 * Resets for the next test.
		 * 
		 * @param failure {@link Exception}.
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
		public void extendTemplate(WoofTemplateExtensionSourceContext context) throws Exception {

			// Provide failure if specified
			if (failure != null) {
				throw failure;
			}

			// Ensure property is defined
			assertEquals("Incorrect property value", "VALUE", context.getProperty("NAME"));
			assertNull("Should not have non-defined property", context.getProperty("NOT DEFINED", null));

			// Ensure correct details
			assertEquals("Incorrect template URI", "URI", context.getApplicationPath());
		}
	}

}
