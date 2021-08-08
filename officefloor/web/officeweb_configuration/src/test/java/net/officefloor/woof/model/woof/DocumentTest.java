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
 * Tests adding documentation to a {@link WoofModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class DocumentTest extends AbstractWoofChangesTestCase {

	/**
	 * Ensure able to add documentation to {@link WoofHttpContinuationModel}.
	 */
	public void testAddHttpContinuationDocumentation() {

		// Add the HTTP continuation documentation
		WoofHttpContinuationModel continuation = this.model.getWoofHttpContinuations().get(0);
		Change<WoofHttpContinuationModel> change = this.operations.addDocumentation(continuation,
				"Added documentation");

		// Validate the change
		this.assertChange(change, null, "Add HTTP Continuation Documentation", true);
	}

	/**
	 * Ensure able to change documentation to {@link WoofHttpContinuationModel}.
	 */
	public void testChangeHttpContinuationDocumentation() {

		// Add the HTTP continuation documentation
		WoofHttpContinuationModel continuation = this.model.getWoofHttpContinuations().get(1);
		Change<WoofHttpContinuationModel> change = this.operations.addDocumentation(continuation,
				"Changed documentation");

		// Validate the change
		this.assertChange(change, null, "Change HTTP Continuation Documentation", true);
	}

	/**
	 * Ensure able to add documentation to {@link WoofHttpInputModel}.
	 */
	public void testAddHttpInputDocumentation() {

		// Add the HTTP input documentation
		WoofHttpInputModel input = this.model.getWoofHttpInputs().get(0);
		Change<WoofHttpInputModel> change = this.operations.addDocumentation(input, "Added documentation");

		// Validate the change
		this.assertChange(change, null, "Add HTTP Input Documentation", true);
	}

	/**
	 * Ensure able to change documentation to {@link WoofHttpInputModel}.
	 */
	public void testChangeHttpInputDocumentation() {

		// Add the HTTP input documentation
		WoofHttpInputModel input = this.model.getWoofHttpInputs().get(1);
		Change<WoofHttpInputModel> change = this.operations.addDocumentation(input, "Changed documentation");

		// Validate the change
		this.assertChange(change, null, "Change HTTP Input Documentation", true);
	}

}
