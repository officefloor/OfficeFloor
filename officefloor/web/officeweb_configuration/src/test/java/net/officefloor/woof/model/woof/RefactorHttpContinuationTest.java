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
 * Tests refactoring the {@link WoofHttpContinuationModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorHttpContinuationTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofHttpContinuationModel}.
	 */
	private WoofHttpContinuationModel httpContinuation;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.httpContinuation = this.model.getWoofHttpContinuations().get(0);
	}

	/**
	 * Ensure handle no change.
	 */
	public void testNoChange() {

		// Refactor with same details
		Change<WoofHttpContinuationModel> change = this.operations.refactorHttpContinuation(this.httpContinuation,
				"/path", false);

		// Validate change
		this.assertChange(change, null, "Refactor HTTP Continuation", true);
	}

	/**
	 * Ensure handle change to all details.
	 */
	public void testChange() {

		// Refactor the section with same details
		Change<WoofHttpContinuationModel> change = this.operations.refactorHttpContinuation(this.httpContinuation,
				"/change", true);

		// Validate change
		this.assertChange(change, null, "Refactor HTTP Continuation", true);
	}

}
