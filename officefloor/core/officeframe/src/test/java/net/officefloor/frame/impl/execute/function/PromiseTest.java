/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link Promise}.
 * 
 * @author Daniel Sagenschneider
 */
public class PromiseTest extends OfficeFrameTestCase {

	/**
	 * Ensure return <code>null</code> on no {@link FunctionState} instances.
	 */
	public void testNothingFuther() {
		FunctionState promise = Promise.then(null, null);
		this.replayMockObjects();
		assertNull("Should be no promise", promise);
		this.verifyMockObjects();
	}

	/**
	 * Ensure return {@link FunctionState} if no then {@link FunctionState}.
	 */
	public void testNoThenFunction() {
		FunctionState function = this.createMock(FunctionState.class);
		this.replayMockObjects();
		FunctionState promise = Promise.then(function, null);
		assertSame("Should just be function", function, promise);
		this.verifyMockObjects();
	}

	/**
	 * Ensure return {@link FunctionState} if no delegate {@link FunctionState}.
	 */
	public void testNoDelegateFunction() {
		FunctionState function = this.createMock(FunctionState.class);
		this.replayMockObjects();
		FunctionState promise = Promise.then(null, function);
		assertSame("Should just be function", function, promise);
		this.verifyMockObjects();
	}

	/**
	 * Ensure can chain {@link FunctionState} instances with {@link Promise}.
	 */
	public void testChainTogether() throws Throwable {

		ThreadState threadState = this.createMock(ThreadState.class);
		FunctionState functionOne = this.createMock(FunctionState.class);
		FunctionState functionTwo = this.createMock(FunctionState.class);
		FunctionState functionPromise = this.createMock(FunctionState.class);

		// Record setting up promise
		this.recordReturn(functionOne, functionOne.getThreadState(), threadState);
		this.recordReturn(threadState, threadState.then(functionOne, functionTwo), functionPromise);

		this.replayMockObjects();

		// Create promise
		FunctionState promise = Promise.then(functionOne, functionTwo);
		assertSame("Should be provided promise", functionPromise, promise);

		this.verifyMockObjects();
	}

}
