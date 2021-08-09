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
