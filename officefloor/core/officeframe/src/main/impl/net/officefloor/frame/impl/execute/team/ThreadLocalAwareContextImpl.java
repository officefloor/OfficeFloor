package net.officefloor.frame.impl.execute.team;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.ThreadLocalAwareContext;
import net.officefloor.frame.internal.structure.ThreadLocalAwareExecutor;

/**
 * {@link ThreadLocalAwareContext} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class ThreadLocalAwareContextImpl implements ThreadLocalAwareContext {

	/**
	 * {@link ThreadLocalAwareExecutor}.
	 */
	private final ThreadLocalAwareExecutor executor;

	/**
	 * Instantiate.
	 * 
	 * @param executor
	 *            {@link ThreadLocalAwareExecutor}.
	 */
	public ThreadLocalAwareContextImpl(ThreadLocalAwareExecutor executor) {
		this.executor = executor;
	}

	/*
	 * ================= ThreadLocalAwareContext =======================
	 */

	@Override
	public void execute(Job job) {
		this.executor.execute(job);
	}

}