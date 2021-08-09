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
 * Tests refactoring the {@link WoofResourceModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorResourceTest extends AbstractWoofChangesTestCase {

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
	 * Ensure can refactor.
	 */
	public void testRefactor() {

		/*
		 * Expecting the refactor method delegates to the change resource path
		 * method which will handle all refactoring. Therefore only providing
		 * simple test to ensure delegating.
		 */

		// Refactor template to change path
		Change<WoofResourceModel> change = this.operations.refactorResource(this.resource, "/resource.png");

		// Validate the change
		this.assertChange(change, this.resource, "Refactor Resource", true);
	}

}
