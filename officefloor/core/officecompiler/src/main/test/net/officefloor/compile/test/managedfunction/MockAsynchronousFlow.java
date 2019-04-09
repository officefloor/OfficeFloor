/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
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
package net.officefloor.compile.test.managedfunction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;

/**
 * Mock {@link AsynchronousFlow}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockAsynchronousFlow implements AsynchronousFlow {

	/**
	 * Indicates if complete.
	 */
	private boolean isComplete = false;

	/**
	 * {@link AsynchronousFlowCompletion}. May be <code>null</code> if not supplied.
	 */
	private AsynchronousFlowCompletion completion;

	/**
	 * Indicates if {@link AsynchronousFlow} is complete.
	 * 
	 * @return <code>true</code> if {@link AsynchronousFlow} is complete.
	 */
	public boolean isComplete() {
		return this.isComplete;
	}

	/**
	 * <p>
	 * Obtains the provided {@link AsynchronousFlowCompletion}.
	 * <p>
	 * Note: will fail if this {@link AsynchronousFlow} is not complete.
	 * 
	 * @return {@link AsynchronousFlowCompletion} or <code>null</code> if not
	 *         provided.
	 */
	public AsynchronousFlowCompletion getCompletion() {
		assertTrue("Flow not complete", this.isComplete);
		return this.completion;
	}

	/*
	 * ================= AsynchronousFlow =======================
	 */

	@Override
	public void complete(AsynchronousFlowCompletion completion) {
		assertFalse("Already completed " + AsynchronousFlow.class.getSimpleName(), this.isComplete);
		this.isComplete = true;
		this.completion = completion;
	}

}