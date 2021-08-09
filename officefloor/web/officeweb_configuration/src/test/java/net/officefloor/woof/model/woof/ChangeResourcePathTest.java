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
 * Tests changing the {@link WoofResourceModel} resource path.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeResourcePathTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofResourceModel}.
	 */
	private WoofResourceModel resource;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.resource = this.model.getWoofResources().get(0);
	}

	/**
	 * Ensure able make no change to resource path.
	 */
	public void testNotChangeResourcePath() {

		// Change resource path to be same
		Change<WoofResourceModel> change = this.operations.changeResourcePath(this.resource, "/resource.html");

		// Validate the change
		this.assertChange(change, this.resource, "Change Resource Path", true);
	}

	/**
	 * Ensure can change to unique resource path.
	 */
	public void testChangeResourcePath() {

		// Change template to unique path
		Change<WoofResourceModel> change = this.operations.changeResourcePath(this.resource, "/resource.gif");

		// Validate the change
		this.assertChange(change, this.resource, "Change Resource Path", true);
	}

	/**
	 * Ensure can change to non-unique resource path.
	 */
	public void testNonUniqueResourcePath() {

		// Change template to non-unique path
		Change<WoofResourceModel> change = this.operations.changeResourcePath(this.resource, "/resource.png");

		// Validate the change
		this.assertChange(change, this.resource, "Change Resource Path", false,
				"Resource already exists for '/resource.png'");
	}

	/**
	 * Ensure no change if attempt to clear resource path.
	 */
	public void testClearResourcePath() {

		// Change to attempting to clear resource path
		Change<WoofResourceModel> change = this.operations.changeResourcePath(this.resource, null);

		// Validate the change
		this.assertChange(change, this.resource, "Change Resource Path", false, "Must provide resource path");
	}

}
