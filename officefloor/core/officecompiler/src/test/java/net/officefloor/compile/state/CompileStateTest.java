/*-
 * #%L
 * OfficeCompiler
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
