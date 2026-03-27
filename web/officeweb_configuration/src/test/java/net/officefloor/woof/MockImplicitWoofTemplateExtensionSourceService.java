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

package net.officefloor.woof;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;

import org.junit.Assert;

import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.web.template.build.WebTemplate;
import net.officefloor.woof.template.WoofTemplateExtensionSource;
import net.officefloor.woof.template.WoofTemplateExtensionSourceContext;
import net.officefloor.woof.template.WoofTemplateExtensionSourceService;
import net.officefloor.woof.template.impl.AbstractWoofTemplateExtensionSource;

/**
 * Mock implicit {@link WoofTemplateExtensionSourceService}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockImplicitWoofTemplateExtensionSourceService extends AbstractWoofTemplateExtensionSource
		implements WoofTemplateExtensionSourceService {

	/**
	 * Resets for testing loading implicit {@link WoofTemplateExtensionSource}.
	 * 
	 * @param templateApplicationPaths
	 *            Application paths of the {@link WebTemplate} instances being
	 *            extended.
	 */
	public static void reset(String... templateApplicationPaths) {
		MockImplicitWoofTemplateExtensionSourceService.templateApplicationPaths = new LinkedList<String>(
				Arrays.asList(templateApplicationPaths));
	}

	/**
	 * Records loading as implicit {@link WoofTemplateExtensionSource}.
	 * 
	 * @param context
	 *            {@link OfficeSourceContext}.
	 * @param testCase
	 *            {@link OfficeFrameTestCase}.
	 * @param templateApplicationPaths
	 *            Application paths of the {@link WebTemplate} instances being
	 *            extended.
	 */
	public static void recordLoadImplicit(OfficeSourceContext context, OfficeFrameTestCase testCase,
			String... templateApplicationPaths) {
		MockImplicitWoofTemplateExtensionSourceService.reset(templateApplicationPaths);
		testCase.recordReturn(context, context.loadOptionalServices(WoofTemplateExtensionSourceService.class),
				Arrays.asList(new MockImplicitWoofTemplateExtensionSourceService()));
	}

	/**
	 * Ensures the {@link WebTemplate} instances were extended.
	 */
	public static void assertTemplatesExtended() {
		Assert.assertEquals("Should have extended all template", 0, templateApplicationPaths.size());
	}

	/**
	 * Expected {@link WebTemplate} URIs.
	 */
	private static Deque<String> templateApplicationPaths = null;

	/*
	 * =================== WoofTemplateExtensionSourceService ==================
	 */

	@Override
	public WoofTemplateExtensionSource createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ================== WoofTemplateExtensionSourceService ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		Assert.fail("Should not require specification");
	}

	@Override
	public void extendTemplate(WoofTemplateExtensionSourceContext context) throws Exception {

		// Obtain the template application path
		String applicationPath = context.getApplicationPath();

		// Ensure expecting the template
		String expectedApplicationPath = templateApplicationPaths.pop();
		Assert.assertNotNull("Not expecting template " + applicationPath, expectedApplicationPath);
		Assert.assertEquals("Incorrect template application path for extension", expectedApplicationPath,
				applicationPath);
	}

}
