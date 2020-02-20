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
