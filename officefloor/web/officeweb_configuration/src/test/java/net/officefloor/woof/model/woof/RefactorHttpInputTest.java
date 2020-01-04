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
 * Tests refactoring the {@link WoofHttpInputModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorHttpInputTest extends AbstractWoofChangesTestCase {

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
	 * Ensure handle no change.
	 */
	public void testNoChange() {

		// Refactor with same details
		Change<WoofHttpInputModel> change = this.operations.refactorHttpInput(this.httpInput, "/input", "POST", false);

		// Validate change
		this.assertChange(change, null, "Refactor HTTP Input", true);
	}

	/**
	 * Ensure handle change to all details.
	 */
	public void testChange() {

		// Refactor with changes
		Change<WoofHttpInputModel> change = this.operations.refactorHttpInput(this.httpInput, "/change", "PUT", true);

		// Validate change
		this.assertChange(change, null, "Refactor HTTP Input", true);
	}

}
