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
 * Tests changing the {@link WoofTemplateModel} URI.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeTemplateApplicationPathTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofTemplateModel}.
	 */
	private WoofTemplateModel template;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.template = this.model.getWoofTemplates().get(0);
	}

	/**
	 * Ensure able make no change to application path.
	 */
	public void testNotChangeApplicationPath() {

		// Test
		this.replayMockObjects();

		// Change with same details
		Change<WoofTemplateModel> change = this.operations.changeApplicationPath(this.template, "/template",
				this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.template, "Change Template Application Path", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can change to unique application path.
	 */
	public void testChangeApplicationPath() {

		// Test
		this.replayMockObjects();

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeApplicationPath(this.template, "/change",
				this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.template, "Change Template Application Path", true);

		// Verify
		this.verifyMockObjects();
	}

	/**
	 * Ensure can not change to non-unique application path.
	 */
	public void testNonUniqueApplicationPath() {

		// Test
		this.replayMockObjects();

		// Change template to unique URI
		Change<WoofTemplateModel> change = this.operations.changeApplicationPath(this.template, "/templateLink",
				this.getWoofTemplateChangeContext());

		// Validate the change
		this.assertChange(change, this.template, "Change Template Application Path", false,
				"Application path '/templateLink' already configured for Template");

		// Verify
		this.verifyMockObjects();
	}

}
