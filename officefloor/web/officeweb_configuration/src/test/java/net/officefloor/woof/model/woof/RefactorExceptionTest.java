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

import java.io.IOException;

import net.officefloor.model.change.Change;

/**
 * Tests refactoring the {@link WoofExceptionModel}.
 * 
 * @author Daniel Sagenschneider
 */
public class RefactorExceptionTest extends AbstractWoofChangesTestCase {

	/**
	 * {@link WoofExceptionModel}.
	 */
	private WoofExceptionModel exception;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.exception = this.model.getWoofExceptions().get(0);
	}

	/**
	 * Ensure no issue if refactored to same exception.
	 */
	public void testNoChange() {

		// Change to a unique exception
		Change<WoofExceptionModel> change = this.operations.refactorException(this.exception,
				RuntimeException.class.getName());

		// Validate the change
		this.assertChange(change, this.exception, "Refactor Exception", true);
	}

	/**
	 * Ensure can change the exception.
	 */
	public void testChangeException() {

		// Change to a unique exception
		Change<WoofExceptionModel> change = this.operations.refactorException(this.exception,
				NullPointerException.class.getName());

		// Validate the change
		this.assertChange(change, this.exception, "Refactor Exception", true);
	}

	/**
	 * Ensure not able to refactor to an existing {@link Exception}.
	 */
	public void testExceptionAlreadyExists() {

		// Change to a unique exception
		Change<WoofExceptionModel> change = this.operations.refactorException(this.exception,
				IOException.class.getName());

		// Validate no change (as exception is already handled)
		this.assertChange(change, this.exception, "Refactor Exception", false,
				"Exception already exists for '" + IOException.class.getName() + "'");
	}

}
