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
	 * {@link Process} being profiled.
	 */
	private final ProcessState process;

	/**
	 * Start time stamp in milliseconds.
	 */
	private final long startTimestampMilliseconds;

	/**
	 * Start time stamp in nanoseconds.
	 */
	private final long startTimestampNanoseconds;

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
	 * @param process
	 *            {@link ProcessState} being profiled.
	 * @param startTimestampMilliseconds
	 *            Start time stamp in milliseconds.
	 * @param startTimestampNanoseconds
	 *            Start time stamp in nanoseconds.
	 */
	public ProcessProfilerImpl(Profiler profiler, ProcessState process, long startTimestampMilliseconds,
			long startTimestampNanoseconds) {
		this.profiler = profiler;
		this.process = process;
		this.startTimestampMilliseconds = startTimestampMilliseconds;
		this.startTimestampNanoseconds = startTimestampNanoseconds;
	}

	/**
	 * Obtains the main {@link ThreadState} for the {@link ProcessState} being
	 * profiled.
	 * 
	 * @return Main {@link ThreadState} for the {@link ProcessState} being
	 *         profiled.
	 */
	ThreadState getMainThreadState() {
		return this.process.getMainThreadState();
	}

	/**
	 * Registers the {@link ProfiledThreadState}.
	 * 
	 * @param profiledThreadState
	 *            {@link ProfiledThreadState}..
	 */
	void registerProfiledThreadState(ProfiledThreadState profiledThreadState) {
		this.threads.add(profiledThreadState);
	}

	/*
	 * ===================== ProcessProfiler ===========================
	 */

	@Override
	public ThreadProfiler addThreadState(ThreadState threadState) {
		long threadStartTimeMilliseconds = System.currentTimeMillis();
		long threadStartTimestampNanoseconds = System.nanoTime();
		return new ThreadProfilerImpl(threadState, this, threadStartTimeMilliseconds, threadStartTimestampNanoseconds);
	}

	@Override
	public void processStateCompleted() {
		this.profiler.profileProcessState(this);
	}

	/*
	 * ====================== ProfiledProcess ===========================
	 */

	@Override
	public List<ProfiledThreadState> getProfiledThreadStates() {
		synchronized (this.process.getMainThreadState()) {
			return this.threads;
		}
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