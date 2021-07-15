/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.profile;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.profile.ProfiledManagedFunction;
import net.officefloor.frame.api.profile.ProfiledThreadState;
import net.officefloor.frame.impl.execute.linkedlistset.AbstractLinkedListSetEntry;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicMetaData;
import net.officefloor.frame.internal.structure.ProcessProfiler;
import net.officefloor.frame.internal.structure.ThreadProfiler;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link ThreadProfiler} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadProfilerImpl extends AbstractLinkedListSetEntry<FunctionState, Flow>
		implements ThreadProfiler, ProfiledThreadState {

	/**
	 * {@link ThreadState} being profiled.
	 */
	private final ThreadState threadState;

	/**
	 * {@link ProcessProfiler}.
	 */
	private final ProcessProfilerImpl processProfiler;

	/**
	 * Start time stamp in milliseconds.
	 */
	private final long startTimestampMilliseconds;

	/**
	 * Start time stamp in nanoseconds.
	 */
	private final long startTimestampNanoseconds;

	/**
	 * {@link ProfiledManagedFunction} instances.
	 */
	private final List<ProfiledManagedFunction> functions = new ArrayList<ProfiledManagedFunction>(32);

	/**
	 * Initiate.
	 *
	 * @param threadState                {@link ThreadState} being profiled.
	 * @param processProfiler            {@link ProcessProfiler}.
	 * @param startTimestampMilliseconds Start time stamp in milliseconds.
	 * @param startTimestampNanoseconds  Start time stamp in nanoseconds.
	 */
	ThreadProfilerImpl(ThreadState threadState, ProcessProfilerImpl processProfiler, long startTimestampMilliseconds,
			long startTimestampNanoseconds) {
		this.threadState = threadState;
		this.processProfiler = processProfiler;
		this.startTimestampMilliseconds = startTimestampMilliseconds;
		this.startTimestampNanoseconds = startTimestampNanoseconds;
	}

	/*
	 * ====================== ThreadProfiler ============================
	 */

	@Override
	public void profileManagedFunction(ManagedFunctionLogicMetaData functionMetaData) {

		// Always invoked in ThreadState safety

		// Obtain the start time stamps
		long startTimestampMilliseconds = System.currentTimeMillis();
		long startTimestampNanoseconds = System.nanoTime();

		// Obtain the function name
		String functionName = functionMetaData.getFunctionName();

		// Obtain the executing thread name
		String executingThreadName = Thread.currentThread().getName();

		// Create and add the profiled function
		this.functions.add(new ProfiledManagedFunctionImpl(functionName, startTimestampMilliseconds,
				startTimestampNanoseconds, executingThreadName));
	}

	/*
	 * ======================= FunctionState ===========================
	 */

	@Override
	public ThreadState getThreadState() {

		// Always register thread profilers on main thread state of process
		return this.processProfiler.getMainThreadState();
	}

	@Override
	public FunctionState execute(FunctionStateContext context) throws Throwable {
		this.processProfiler.registerProfiledThreadState(this);
		return null;
	}

	/*
	 * ======================= ProfiledThreadState =======================
	 */

	@Override
	public long getStartTimestampMilliseconds() {
		return this.startTimestampMilliseconds;
	}

	@Override
	public long getStartTimestampNanoseconds() {
		return this.startTimestampNanoseconds;
	}

	@Override
	public List<ProfiledManagedFunction> getProfiledManagedFunctions() {
		return this.threadState.runThreadSafeOperation(() -> this.functions);
	}

	/**
	 * {@link ProfiledManagedFunction} implementation.
	 */
	private static class ProfiledManagedFunctionImpl implements ProfiledManagedFunction {

		/**
		 * {@link ManagedFunction} name.
		 */
		private final String functionName;

		/**
		 * Start time stamp in milliseconds.
		 */
		private final long startTimestampMilliseconds;

		/**
		 * Start time stamp in nanoseconds.
		 */
		private final long startTimestampNanoseconds;

		/**
		 * Name of the executing {@link Thread}.
		 */
		private final String executingThreadName;

		/**
		 * Initiate.
		 * 
		 * @param functionName               {@link ManagedFunction} name.
		 * @param startTimestampMilliseconds Start time stamp in milliseconds.
		 * @param startTimestampNanoseconds  Start time stamp in nanoseconds.
		 */
		public ProfiledManagedFunctionImpl(String functionName, long startTimestampMilliseconds,
				long startTimestampNanoseconds, String executingThreadName) {
			this.functionName = functionName;
			this.startTimestampMilliseconds = startTimestampMilliseconds;
			this.startTimestampNanoseconds = startTimestampNanoseconds;
			this.executingThreadName = executingThreadName;
		}

		/*
		 * ====================== ProfiledMangedFunction =====================
		 */

		@Override
		public String getFunctionName() {
			return this.functionName;
		}

		@Override
		public String getExecutingThreadName() {
			return this.executingThreadName;
		}

		@Override
		public long getStartTimestampMilliseconds() {
			return this.startTimestampMilliseconds;
		}

		@Override
		public long getStartTimestampNanoseconds() {
			return this.startTimestampNanoseconds;
		}
	}

}
