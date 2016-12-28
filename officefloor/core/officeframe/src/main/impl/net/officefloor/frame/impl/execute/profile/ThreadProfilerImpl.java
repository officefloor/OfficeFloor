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
package net.officefloor.frame.impl.execute.profile;

import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.execute.ManagedFunction;
import net.officefloor.frame.api.profile.ProfiledManagedFunction;
import net.officefloor.frame.api.profile.ProfiledThreadState;
import net.officefloor.frame.internal.structure.ManagedFunctionLogicMetaData;
import net.officefloor.frame.internal.structure.ThreadProfiler;

/**
 * {@link ThreadProfiler} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ThreadProfilerImpl implements ThreadProfiler, ProfiledThreadState {

	/**
	 * Start time stamp.
	 */
	private final long startTimestamp;

	/**
	 * {@link ProfiledManagedFunction} instances.
	 */
	private final List<ProfiledManagedFunction> functions = new ArrayList<ProfiledManagedFunction>(32);

	/**
	 * Initiate.
	 * 
	 * @param startTimestamp
	 *            Start time stamp.
	 */
	public ThreadProfilerImpl(long startTimestamp) {
		this.startTimestamp = startTimestamp;
	}

	/*
	 * ====================== ThreadProfiler ============================
	 */

	@Override
	public void profileManagedFunction(ManagedFunctionLogicMetaData functionMetaData) {

		// Obtain the start time stamp
		long startTimestamp = System.nanoTime();

		// Obtain the function name
		String functionName = functionMetaData.getFunctionName();

		// Obtain the executing thread name
		String executingThreadName = Thread.currentThread().getName();

		// Create and add the profiled function
		this.functions.add(new ProfiledManagedFunctionImpl(functionName, startTimestamp, executingThreadName));
	}

	/*
	 * ======================= ProfiledThreadState =======================
	 */

	@Override
	public long getStartTimestamp() {
		return this.startTimestamp;
	}

	@Override
	public List<ProfiledManagedFunction> getProfiledManagedFunctions() {
		return this.functions;
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
		 * Start time stamp.
		 */
		private final long startTimestamp;

		/**
		 * Name of the executing {@link Thread}.
		 */
		private final String executingThreadName;

		/**
		 * Initiate.
		 * 
		 * @param functionName
		 *            {@link ManagedFunction} name.
		 * @param startTimestamp
		 *            Start time stamp.
		 */
		public ProfiledManagedFunctionImpl(String functionName, long startTimestamp, String executingThreadName) {
			this.functionName = functionName;
			this.startTimestamp = startTimestamp;
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
		public long getStartTimestamp() {
			return this.startTimestamp;
		}

		@Override
		public String getExecutingThreadName() {
			return this.executingThreadName;
		}
	}

}