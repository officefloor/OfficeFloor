/*-
 * #%L
 * Web configuration
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
