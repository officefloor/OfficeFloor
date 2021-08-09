/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.compile.state;

import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;

/**
 * Tests the {@link CompileState}.
 * 
 * @author Daniel Sagenschneider
 */
public class CompileStateTest extends OfficeFrameTestCase {

	/**
	 * Ensure can obtain state.
	 */
	public void testState() {
		Closure<String> state = new Closure<>();
		state().compileInContext("TEST", () -> {
			state.value = state().getCompileState();
		});
		assertEquals("Incorrect state", "TEST", state.value);
		assertNull("Should clear state", state().getCompileState());
	}

	/**
	 * Ensure can return value.
	 */
	public void testSupply() {
		String value = state().compileInContext("TEST", () -> state().getCompileState());
		assertEquals("Incorrect state", "TEST", value);
		assertNull("Shoulc clear state", state().getCompileState());
	}

	/**
	 * Ensure correct default handling of merging state.
	 */
	public void testDefaultMergeState() {
		String value = state().compileInContext("OUTER", () -> {
			return state().compileInContext("INNER", (CompileSupplier<String, RuntimeException>) () -> {
				return state().getCompileState();
			});
		});
		assertEquals("Incorrect default merge state", "OUTER", value);
	}

	/**
	 * Ensure can merge state.
	 */
	public void testMergeState() {
		String value = mergeState().compileInContext("OUTER", () -> {
			return mergeState().compileInContext("INNER", (CompileSupplier<String, RuntimeException>) () -> {
				return mergeState().getCompileState();
			});
		});
		assertEquals("Incorrect default merge state", "OUTER-INNER", value);
	}

	public void testIsolatedStates() {
		Closure<String> state = new Closure<>();
		Closure<String> mergeState = new Closure<>();
		state().compileInContext("STATE", () -> {
			mergeState().compileInContext("MERGE", () -> {
				state.value = state().getCompileState();
				mergeState.value = mergeState().getCompileState();
			});
		});
		assertEquals("Incorrect state", "STATE", state.value);
		assertEquals("Incorrect merge state", "MERGE", mergeState.value);
	}

	public void testNoState() {
		String value = state().compileInContext("IGNORED", () -> mergeState().getCompileState());
		assertNull("Should be no state", value);
	}

	private static MockCompileState state() {
		return new MockCompileState();
	}

	public static class MockCompileState implements CompileState<String> {
	}

	private static MockMergeState mergeState() {
		return new MockMergeState();
	}

	public static class MockMergeState implements CompileState<String> {

		@Override
		public String mergeCompileContext(String outer, String inner) {
			return outer + "-" + inner;
		}
	}

}
