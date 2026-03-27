/*-
 * #%L
 * Activity
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

package net.officefloor.activity.model;

import net.officefloor.model.change.Change;

/**
 * Tests refactoring the {@link ActivityInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorInputTest extends AbstractActivityChangesTestCase {

	/**
	 * {@link ActivityInputModel}.
	 */
	private ActivityInputModel input;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.input = this.model.getActivityInputs().get(1);
	}

	/**
	 * Ensure handle no change.
	 */
	public void testNoChange() {

		// Refactor with same details
		Change<ActivityInputModel> change = this.operations.refactorInput(this.input, "INPUT", String.class.getName());

		// Validate change
		this.assertChange(change, null, "Refactor Input", true);
	}

	/**
	 * Ensure handle change to all details.
	 */
	public void testChange() {

		// Refactor with changes
		Change<ActivityInputModel> change = this.operations.refactorInput(this.input, "CHANGE", null);

		// Validate change
		this.assertChange(change, null, "Refactor Input", true);
	}

	/**
	 * Ensure keeps unique {@link ActivityInputModel} name.
	 */
	public void testInputAlreadyExists() {

		// Refactor to existing name
		Change<ActivityInputModel> change = this.operations.refactorInput(this.input, "EXISTS", null);

		// Validate change
		this.assertChange(change, null, "Refactor Input", true);
	}

}
