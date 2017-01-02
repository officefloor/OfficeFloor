/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
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
package net.officefloor.frame.impl.execute.job;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.impl.execute.function.ManagedFunctionContainerImpl;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedFunctionLogic;

/**
 * Tests the {@link ManagedFunctionContainerImpl} invoking sequential
 * {@link Flow} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class SequentialContainerTest extends AbstractManagedFunctionContainerTest {

	/**
	 * Ensures execution of a {@link ManagedFunction} with a sequential
	 * {@link Flow} invoked.
	 */
	public void testExecuteFunctionWithSequentialFlow() {

		// Create a function invoking a sequential flow
		final Object sequentialFlowParameter = "Sequential Flow Parameter";
		ManagedFunctionLogic function = this.createFunction(new FunctionFunctionality() {
			@Override
			public Object executeFunctionality(FunctionFunctionalityContext context) throws Throwable {
				context.doFlow(0, sequentialFlowParameter, null, false);
				return null;
			}
		});

		// Record actions
		this.record_Container_initialSteps(function);
		this.record_doSequentialFlow(function, sequentialFlowParameter);
		this.record_completeFunction(function);

		// Replay mocks
		this.replayMockObjects();

		// Execute the function
		this.doFunction(function);

		// Verify mocks
		this.verifyMockObjects();

		// Ensure function run
		assertFunctionExecuted(function);
	}

}