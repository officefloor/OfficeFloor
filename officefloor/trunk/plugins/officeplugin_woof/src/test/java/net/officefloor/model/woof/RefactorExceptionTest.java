/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2012 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.model.woof;

import org.junit.Ignore;

import net.officefloor.model.change.Change;

/**
 * Tests refactoring the {@link WoofExceptionModel}.
 * 
 * @author Daniel Sagenschneider
 */
@Ignore("TODO provide implementation")
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
	 * Ensure can change the exception.
	 */
	public void testChangeException() {

		// Change to a unique exception
		Change<WoofExceptionModel> change = this.operations.refactorException(
				this.exception, NullPointerException.class.getName());

		// Validate the change
		this.assertChange(change, this.exception, "Refactor Exception", true);
	}

	/**
	 * Ensure not able to refactor to an existing {@link Exception}.
	 */
	public void testExceptionAlreadyExists() {

		// TODO determine what to do for change if not able to change
		fail("TODO determine what to do for no operation change");

		// Change to a unique exception
		Change<WoofExceptionModel> change = this.operations.refactorException(
				this.exception, NullPointerException.class.getName());

		// Validate the change
		this.assertChange(change, this.exception, "Refactor Exception", true);
	}

}