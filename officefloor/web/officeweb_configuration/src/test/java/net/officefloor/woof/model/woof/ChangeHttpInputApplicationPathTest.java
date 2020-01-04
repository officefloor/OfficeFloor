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
