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
package net.officefloor.model.woof;

import net.officefloor.model.change.Change;

/**
 * Tests changing the {@link WoofTemplateModel} URI when has a
 * {@link WoofTemplateExtension}.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeTemplateUriWithExtensionTest extends
		AbstractWoofChangesTestCase {

	/**
	 * {@link WoofTemplateModel}.
	 */
	private WoofTemplateModel template;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.template = this.model.getWoofTemplates().get(0);

		// Ensure have extensions
		assertEquals("Invalid test as no extensions", 2, this.template
				.getExtensions().size());
	}

	/**
	 * Ensure able make no change to URI.
	 */
	public void testNotChangeUri() {

		final String TEMPLATE_URI = "path";

		// Register the extension test details
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange,
				TEMPLATE_URI, new String[] { "ONE", "A", "TWO", "B" },
				TEMPLATE_URI, new String[] { "ONE", "A", "TWO", "B" },
				this.getWoofTemplateChangeContext());

		// Record changing
		MockChangeWoofTemplateExtensionSource.recordAssertChange(
				extensionChange, this);

		// Test
		this.replayMockObjects();

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeTemplateApplicationPath(
				this.template, TEMPLATE_URI,
				this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.template, "Change Template URI", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can change to unique URI.
	 */
	public void testUniqueUri() {

		final String TEMPLATE_URI = "unique/uri";

		// Register the extension test details
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange, "path",
				new String[] { "ONE", "A", "TWO", "B" }, TEMPLATE_URI,
				new String[] { "ONE", "A", "TWO", "B" },
				this.getWoofTemplateChangeContext());

		// Record changing
		MockChangeWoofTemplateExtensionSource.recordAssertChange(
				extensionChange, this);

		// Test
		this.replayMockObjects();

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeTemplateApplicationPath(
				this.template, TEMPLATE_URI,
				this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.template, "Change Template URI", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can change to non-unique URI.
	 */
	public void testNonUniqueUri() {
		
		final String TEMPLATE_URI = "same/uri";

		// Register the extension test details
		Change<?> extensionChange = this.createMock(Change.class);
		MockChangeWoofTemplateExtensionSource.reset(extensionChange, "path",
				new String[] { "ONE", "A", "TWO", "B" }, TEMPLATE_URI,
				new String[] { "ONE", "A", "TWO", "B" },
				this.getWoofTemplateChangeContext());

		// Record changing
		MockChangeWoofTemplateExtensionSource.recordAssertChange(
				extensionChange, this);

		// Test
		this.replayMockObjects();

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeTemplateApplicationPath(
				this.template, TEMPLATE_URI,
				this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.template, "Change Template URI", true);

		// Verify
		this.verifyMockObjects();
	}

}