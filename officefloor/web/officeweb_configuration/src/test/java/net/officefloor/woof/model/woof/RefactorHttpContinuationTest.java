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