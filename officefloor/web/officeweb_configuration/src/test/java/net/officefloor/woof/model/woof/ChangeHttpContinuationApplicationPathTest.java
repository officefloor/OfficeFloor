/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.woof.model.woof;

import net.officefloor.model.change.Change;

/**
 * Tests changing the {@link WoofHttpContinuationModel} application path.
 * 
 * @author Daniel Sagenschneider
 */
public class ChangeHttpContinuationApplicationPathTest extends AbstractWoofChangesTestCase {

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
	 * Ensure able make no change to application path.
	 */
	public void testNotChangeApplicationPath() {

		// Change path to be same
		Change<WoofHttpContinuationModel> change = this.operations.changeApplicationPath(this.httpContinuation,
				"/path");

		// Validate the change
		this.assertChange(change, this.httpContinuation, "Change Application Path", true);
	}

	/**
	 * Ensure can change to unique resource path.
	 */
	public void testChangeApplicationPath() {

		// Change template to unique path
		Change<WoofHttpContinuationModel> change = this.operations.changeApplicationPath(this.httpContinuation,
				"/change");

		// Validate the change
		this.assertChange(change, this.httpContinuation, "Change Application Path", true);
	}

	/**
	 * Ensure can not change to non-unique resource path.
	 */
	public void testNonUniqueApplicationPath() {

		// Attempt to change to non-unique path
		Change<WoofHttpContinuationModel> change = this.operations.changeApplicationPath(this.httpContinuation,
				"/pathLink");

		// Validate the change
		this.assertChange(change, this.httpContinuation, "Change Application Path", false,
				"Application path '/pathLink' already configured for HTTP Continuation");
	}

	/**
	 * Ensure no change if attempt to clear application path.
	 */
	public void testClearApplicationPath() {

		// Change to attempting to clear application path
		Change<WoofHttpContinuationModel> change = this.operations.changeApplicationPath(this.httpContinuation, null);

		// Validate the change
		this.assertChange(change, this.httpContinuation, "Change Application Path", false,
				"Must provide an application path");
	}

}