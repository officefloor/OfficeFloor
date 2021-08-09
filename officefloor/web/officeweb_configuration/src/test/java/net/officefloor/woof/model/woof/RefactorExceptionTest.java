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
