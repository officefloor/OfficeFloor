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

import net.officefloor.frame.api.profile.ProfiledProcessState;
import net.officefloor.frame.api.profile.ProfiledThreadState;
import net.officefloor.frame.api.profile.Profiler;
import net.officefloor.frame.internal.structure.ProcessProfiler;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.ThreadProfiler;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link ProcessProfiler} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class ProcessProfilerImpl implements ProcessProfiler, ProfiledProcessState {

	/**
	 * {@link Profiler}.
	 */
	private final Profiler profiler;

	/**
	 * Start time stamp.
	 */
	private final long startTimestamp;

	/**
	 * <p>
	 * {@link ProfiledThreadState} instances for this {@link ProcessProfiler}.
	 * <p>
	 * Typically only one {@link ThreadState} per {@link ProcessState}.
	 */
	private final List<ProfiledThreadState> threads = new ArrayList<ProfiledThreadState>(1);

	/**
	 * Initiate.
	 * 
	 * @param profiler
	 *            {@link Profiler}.
	 * @param startTimestamp
	 *            Start time stamp.
	 */
	public ProcessProfilerImpl(Profiler profiler, long startTimestamp) {
		this.profiler = profiler;
		this.startTimestamp = startTimestamp;
	}

	/*
	 * ===================== ProcessProfiler ===========================
	 */

	@Override
	public ThreadProfiler addThread(ThreadState thread) {
		long threadStartTimestamp = System.nanoTime();
		ThreadProfilerImpl profiler = new ThreadProfilerImpl(threadStartTimestamp);
		this.threads.add(profiler);
		return profiler;
	}

	@Override
	public void processCompleted() {
		this.profiler.profileProcessState(this);
	}

	/*
	 * ====================== ProfiledProcess ===========================
	 */

	@Override
	public long getStartTimestamp() {
		return this.startTimestamp;
	}

	@Override
	public List<ProfiledThreadState> getProfiledThreadStates() {
		return this.threads;
	}

}