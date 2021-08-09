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
 * Tests changing the {@link WoofHttpInputModel} application path.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeHttpInputApplicationPathTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofHttpInputModel}.
	 */
	private WoofHttpInputModel httpInput;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.httpInput = this.model.getWoofHttpInputs().get(0);
	}

	/**
	 * Ensure able make no change to application path.
	 */
	public void testNotChangeApplicationPath() {

		// Change path to be same
		Change<WoofHttpInputModel> change = this.operations.changeApplicationPath(this.httpInput, "/input");

		// Validate the change
		this.assertChange(change, this.httpInput, "Change Application Path", true);
	}

	/**
	 * Ensure can change to unique resource path.
	 */
	public void testChangeApplicationPath() {

		// Change template to unique path
		Change<WoofHttpInputModel> change = this.operations.changeApplicationPath(this.httpInput, "/change");

		// Validate the change
		this.assertChange(change, this.httpInput, "Change Application Path", true);
	}

	/**
	 * Ensure can change to unique resource path.
	 */
	public void testChangeApplicationPathUniqueByMethod() {

		// Change template to unique path
		Change<WoofHttpInputModel> change = this.operations.changeApplicationPath(this.httpInput, "/inputPut");

		// Validate the change
		this.assertChange(change, this.httpInput, "Change Application Path", true);
	}

	/**
	 * Ensure can not change to non-unique resource path.
	 */
	public void testNonUniqueApplicationPath() {

		// Attempt to change to non-unique path
		Change<WoofHttpInputModel> change = this.operations.changeApplicationPath(this.httpInput, "/inputPost");

		// Validate the change
		this.assertChange(change, this.httpInput, "Change Application Path", false,
				"Application path '/inputPost' already configured for HTTP Input");
	}

	/**
	 * Ensure no change if attempt to clear application path.
	 */
	public void testClearApplicationPath() {

		// Change to attempting to clear application path
		Change<WoofHttpInputModel> change = this.operations.changeApplicationPath(this.httpInput, null);

		// Validate the change
		this.assertChange(change, this.httpInput, "Change Application Path", false, "Must provide an application path");
	}

}
