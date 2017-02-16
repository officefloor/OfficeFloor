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

import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.execute.function.AbstractDelegateFunctionState;
import net.officefloor.frame.impl.execute.officefloor.OfficeFloorImpl;
import net.officefloor.frame.impl.execute.team.TeamManagementImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.impl.spi.team.WorkerPerJobTeam;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;

/**
 * {@link FunctionLoop} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class FunctionLoopImpl implements FunctionLoop {

	/**
	 * Enable debug logging of execution.
	 */
	private static Logger LOGGER = OfficeFloorImpl.getFrameworkLogger();

	/**
	 * Identifier for the {@link FunctionLoop}.
	 */
	private final Object LOOP_TEAM = new Object();

	/**
	 * Default {@link TeamManagement} to assign {@link FunctionState} instances.
	 */
	private final TeamManagement defaultTeam;

	/**
	 * Maximum depth of {@link ThreadState} recursive attaching to
	 * {@link Thread} before it is broken.
	 */
	private final int maxThreadStateRecursiveDepth;

	/**
	 * {@link Team} containing an active {@link Thread} implementation, to
	 * enable breaking the {@link ThreadState} recursive attach to
	 * {@link Thread} depth.
	 */
	private final TeamManagement activeTeamToBreakRecursion;

	/**
	 * Instantiates.
	 * 
	 * @param defaultTeam
	 *            Default {@link TeamManagement}. May be <code>null</code>.
	 * @param maxThreadStateRecursiveDepth
	 *            Maximum recursive {@link ThreadState} depth in attaching to
	 *            {@link Thread} before broken.
	 */
	public FunctionLoopImpl(TeamManagement defaultTeam, int maxThreadStateRecursiveDepth) {

		// Ensure have default team
		this.defaultTeam = (defaultTeam != null) ? defaultTeam : new TeamManagementImpl(new PassiveTeam());

		// Create active team to break thread state attach to thread recursion
		this.maxThreadStateRecursiveDepth = maxThreadStateRecursiveDepth;
		this.activeTeamToBreakRecursion = new TeamManagementImpl(new WorkerPerJobTeam("BREAK_THREAD_STATE_RECURSION"));
	}

	/*
	 * =================== FunctionLoop ===========================
	 */

	@Override
	public void executeFunction(FunctionState function) {
		// Run on current thread (will swap to appropriate as necessary)
		UnsafeLoop loop = new UnsafeLoop(function, LOOP_TEAM);
		loop.run();
	}

	@Override
	public void delegateFunction(FunctionState function) {

		// Obtain the responsible team
		TeamManagement responsibleTeam = function.getResponsibleTeam();
		if (responsibleTeam == null) {
			responsibleTeam = this.defaultTeam;
		}

		// Delegate function to the responsible team
		SafeLoop loop = new SafeLoop(function, responsibleTeam.getIdentifier());
		Team team = responsibleTeam.getTeam();
		team.assignJob(loop);
	}

	/**
	 * Undertakes the {@link FunctionState} loop for a particular
	 * {@link ThreadState}.
	 * 
	 * @param threadState
	 *            Particular {@link ThreadState}.
	 * @param headFunction
	 *            Head {@link FunctionState}.
	 * @param isThreadStateSafe
	 *            Flag indicating if changes to the {@link ThreadState} are safe
	 *            on the current {@link Thread}.
	 * @param currentTeam
	 *            Identifier of the current {@link Team}.
	 * @return Optional next {@link FunctionState} that requires execution by
	 *         another {@link ThreadState} (or {@link TeamManagement}).
	 */
	private FunctionState executeThreadStateFunctionLoop(FunctionState headFunction, boolean isThreadStateSafe,
			Object currentTeam) {

		// Obtain the thread state for loop
		ThreadState threadState = headFunction.getThreadState();

		// Attach thread state to thread
		if (!ThreadStateImpl.attachThreadStateToThread(threadState, isThreadStateSafe,
				this.maxThreadStateRecursiveDepth)) {
			// Max thread state recursive depth reached
			this.delegateFunction(new BreakThreadStateDepthFunction(headFunction));
			return null; // break recursive depth
		}

		// Ensure detach thread state on exit of loop
		try {

			// Run function loop for the current thread state and team
			FunctionState nextFunction = headFunction;
			do {
				try {
					// Ensure appropriate thread state
					if (nextFunction.getThreadState() != threadState) {
						// Other thread state to undertake function loop
						return nextFunction;
					}

					// Ensure appropriate team undertakes the function
					TeamManagement responsible = nextFunction.getResponsibleTeam();
					if ((responsible != null) && (currentTeam != responsible.getIdentifier())) {
						// Different responsible team
						return nextFunction;
					}

					// Ensure providing appropriate thread state safety
					if ((!isThreadStateSafe) && (nextFunction.isRequireThreadStateSafety())) {
						// Exit loop to obtain thread state safety
						return nextFunction;
					}

					// Provide debug level logging of execution
					if (LOGGER.isLoggable(Level.FINEST)) {
						LOGGER.log(Level.FINEST,
								nextFunction.toString() + " (TS:" + Integer.toHexString(threadState.hashCode()) + ", T:"
										+ Thread.currentThread().getName() + ")");
					}

					// Required team, thread state and safety, so execute
					nextFunction = nextFunction.execute();

				} catch (Throwable ex) {
					// Handle failure
					nextFunction = nextFunction.handleEscalation(ex);
				}

			} while (nextFunction != null);

		} finally {
			// Detach thread state from the thread
			ThreadStateImpl.detachThreadStateFromThread();
		}

		// No further functions to execute
		return null;
	}

	/**
	 * <p>
	 * {@link FunctionState} loop {@link Job} implementation that is not
	 * {@link Thread} safe.
	 * <p>
	 * This is available for the initial {@link FunctionState} of a
	 * {@link ProcessState} to avoid synchronising overheads if no other
	 * responsible {@link TeamManagement} nor {@link ThreadState} is involved.
	 */
	private class UnsafeLoop implements Job {

		/**
		 * Initial {@link FunctionState}.
		 */
		private final FunctionState initialFunction;

		/**
		 * Identifier of the current {@link Team} executing this {@link Job}.
		 */
		protected Object currentTeam;

		/**
		 * Instantiate.
		 * 
		 * @param initialFunction
		 *            Initial {@link FunctionState}.
		 * @param currentTeam
		 *            Identifier of the current {@link Team} executing this
		 *            {@link Job}.
		 */
		public UnsafeLoop(FunctionState initialFunction, Object currentTeam) {
			this.initialFunction = initialFunction;
			this.currentTeam = currentTeam;
		}

		/**
		 * Undertakes the {@link FunctionState} for the {@link ThreadState}.
		 * 
		 * @param headFunction
		 *            Head {@link FunctionState} for loop.
		 * @param isRequireThreadStateSafe
		 *            <code>true</code> to provide {@link Thread} safety on
		 *            executing the {@link FunctionState} instances.
		 * @return Optional next {@link FunctionState} that requires execution
		 *         by another {@link ThreadState} (or {@link TeamManagement} or
		 *         requires {@link Thread} safety).
		 */
		protected FunctionState doThreadStateFunctionLoop(FunctionState headFunction,
				boolean isRequireThreadStateSafety) {
			if (isRequireThreadStateSafety) {
				// Execute loop with thread state safety
				synchronized (headFunction.getThreadState()) {
					return FunctionLoopImpl.this.executeThreadStateFunctionLoop(headFunction, true, this.currentTeam);
				}
			} else {
				// Execute loop unsafely (only one thread, avoid overheads)
				return FunctionLoopImpl.this.executeThreadStateFunctionLoop(headFunction, false, this.currentTeam);
			}
		}

		/**
		 * Assigns the {@link FunctionState} to its responsible
		 * {@link TeamManagement}.
		 * 
		 * @param function
		 *            {@link FunctionState}.
		 * @param responsibleTeam
		 *            Responsible {@link TeamManagement} for the
		 *            {@link FunctionState}.
		 */
		protected void assignFunction(FunctionState function, TeamManagement responsibleTeam) {

			// First assigning, so must synchronise thread state
			synchronized (function.getThreadState()) {
			}

			// Assign the function to the responsible team
			responsibleTeam.getTeam().assignJob(new SafeLoop(function, responsibleTeam.getIdentifier()));
		}

		/*
		 * ==================== Job ====================
		 */

		@Override
		public Object getProcessIdentifier() {
			return this.initialFunction.getThreadState().getProcessState().getProcessIdentifier();
		}

		@Override
		public void run() {

			// Execute the functions for the thread state
			FunctionState nextFunction = this.initialFunction;
			do {

				// Ensure appropriate team undertakes the functions
				TeamManagement responsible = nextFunction.getResponsibleTeam();
				if ((responsible != null) && (this.currentTeam != responsible.getIdentifier())) {
					// Different responsible team
					this.assignFunction(nextFunction, responsible);
					return;
				}

				// Undertake loop for thread state
				boolean isRequireThreadStateSafety = nextFunction.isRequireThreadStateSafety();
				nextFunction = this.doThreadStateFunctionLoop(nextFunction, isRequireThreadStateSafety);

			} while (nextFunction != null);
		}

		@Override
		public void cancel(Throwable cause) {
			// Handle cancellation of the job
			SafeLoop loop = new SafeLoop(this.initialFunction.handleEscalation(cause), this.currentTeam);
			loop.run();
		}
	}

	/**
	 * {@link Thread} safe {@link FunctionState} loop {@link Job}
	 * implementation.
	 */
	private class SafeLoop extends UnsafeLoop {

		/**
		 * Instantiate.
		 * 
		 * @param initialFunction
		 *            Initial {@link FunctionState}.
		 * @param currentTeam
		 *            Current {@link Team} identifier.
		 */
		public SafeLoop(FunctionState initialFunction, Object currentTeam) {
			super(initialFunction, currentTeam);
		}

		/*
		 * ====================== UnsafeJob ======================
		 */

		@Override
		protected FunctionState doThreadStateFunctionLoop(FunctionState headFunction, boolean isRequireThreadSafety) {
			// Always require thread state safety
			return super.doThreadStateFunctionLoop(headFunction, true);
		}

		@Override
		protected void assignFunction(FunctionState function, TeamManagement responsibleTeam) {
			// No need to synchronise assigning function, as loop is thread safe
			responsibleTeam.getTeam().assignJob(new SafeLoop(function, responsibleTeam.getIdentifier()));
		}
	}

	/**
	 * {@link FunctionState} to break the {@link ThreadState} attach to
	 * {@link Thread} recursive depth.
	 */
	private class BreakThreadStateDepthFunction extends AbstractDelegateFunctionState {

		/**
		 * Instantiate.
		 * 
		 * @param delegate
		 *            Delegate {@link FunctionState}.
		 */
		public BreakThreadStateDepthFunction(FunctionState delegate) {
			super(delegate);
		}

		/*
		 * ======================== FunctionState ========================
		 */

		@Override
		public TeamManagement getResponsibleTeam() {
			return FunctionLoopImpl.this.activeTeamToBreakRecursion;
		}

		@Override
		public FunctionState execute() throws Throwable {

			// Ensure delegate allowed to be executed by appropriate team
			return this.delegate;
		}
	}

}