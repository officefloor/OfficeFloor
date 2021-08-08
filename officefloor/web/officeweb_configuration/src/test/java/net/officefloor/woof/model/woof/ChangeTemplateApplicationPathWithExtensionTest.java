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

package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;

/**
 * Tests changing the {@link WoofTemplateModel} application path when has a
 * {@link WoofTemplateExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeTemplateApplicationPathWithExtensionTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofTemplateModel}.
	 */
	private WoofTemplateModel template;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.template = this.model.getWoofTemplates().get(0);

		// Ensure have extensions
		assertEquals("Invalid test as no extensions", 2, this.template.getExtensions().size());
	}

	/**
	 * Ensure able make no change to application path.
	 */
	public void testNotChangeApplicationPath() {

		final String TEMPLATE_APPLICATION_PATH = "/path";

		// Register the extension test details
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange, TEMPLATE_APPLICATION_PATH,
				new String[] { "ONE", "A", "TWO", "B" }, TEMPLATE_APPLICATION_PATH,
				new String[] { "ONE", "A", "TWO", "B" }, this.getWoofTemplateChangeContext());

		// Record changing
		MockChangeWoofTemplateExtensionSource.recordAssertChange(extensionChange, this);

		// Test
		this.replayMockObjects();

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeApplicationPath(this.template,
				TEMPLATE_APPLICATION_PATH, this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.template, "Change Template Application Path", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can change to application path.
	 */
	public void testChangeApplicationPath() {

		final String TEMPLATE_APPLICATION_PATH = "/change/path";

		// Register the extension test details
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange, "/path", new String[] { "ONE", "A", "TWO", "B" },
				TEMPLATE_APPLICATION_PATH, new String[] { "ONE", "A", "TWO", "B" },
				this.getWoofTemplateChangeContext());

		// Record changing
		MockChangeWoofTemplateExtensionSource.recordAssertChange(extensionChange, this);

		// Test
		this.replayMockObjects();

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeApplicationPath(this.template,
				TEMPLATE_APPLICATION_PATH, this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.template, "Change Template Application Path", true);

		// Verify
		this.verifyMockObjects();
	}

}
