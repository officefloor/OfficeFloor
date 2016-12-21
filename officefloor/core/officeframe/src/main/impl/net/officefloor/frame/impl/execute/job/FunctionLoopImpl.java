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

import net.officefloor.frame.impl.execute.team.TeamManagementImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl.ActiveThreadState;
import net.officefloor.frame.impl.spi.team.PassiveTeam;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.spi.team.Job;
import net.officefloor.frame.spi.team.Team;
import net.officefloor.frame.spi.team.TeamIdentifier;

/**
 * {@link FunctionLoop} implementation.
 *
 * @author Daniel Sagenschneider
 */
public class FunctionLoopImpl implements FunctionLoop {

	/**
	 * {@link TeamIdentifier} for the {@link FunctionLoopImpl}.
	 */
	private final TeamIdentifier LOOP_TEAM = new TeamIdentifier() {
	};

	/**
	 * Default {@link TeamManagement} to assign {@link FunctionState} instances.
	 */
	private final TeamManagement defaultTeam;

	/**
	 * Instantiates.
	 * 
	 * @param defaultTeam
	 *            Default {@link TeamManagement}.
	 */
	public FunctionLoopImpl(TeamManagement defaultTeam) {
		this.defaultTeam = (defaultTeam != null) ? defaultTeam : new TeamManagementImpl(LOOP_TEAM, new PassiveTeam());
	}

	/*
	 * =================== FunctionLoop ===========================
	 */

	@Override
	public void executeFunction(FunctionState function) {
		// Run on current thread (will swap to appropriate as necessary)
		new UnsafeLoop(function, LOOP_TEAM).run();
	}

	@Override
	public void delegateFunction(FunctionState function) {

		// Obtain the responsible team
		TeamManagement responsibleTeam = function.getResponsibleTeam();
		if (responsibleTeam == null) {
			responsibleTeam = this.defaultTeam;
		}

		// Delegate function to the responsible team
		responsibleTeam.getTeam().assignJob(new SafeLoop(function, responsibleTeam.getIdentifier()));
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
	 *            {@link TeamIdentifier} of the current {@link Team}.
	 * @return Optional next {@link FunctionState} that requires execution by
	 *         another {@link ThreadState} (or {@link TeamManagement}).
	 */
	private FunctionState executeThreadStateJobNodeLoop(FunctionState headFunction, boolean isThreadStateSafe,
			TeamIdentifier currentTeam) {

		// Obtain existing active thread state
		ActiveThreadState existingThreadState = ThreadStateImpl.getActiveThreadState();

		// Obtain the thread state for loop
		ThreadState threadState = headFunction.getThreadState();
		try {
			// Attach thread state to thread
			threadState.attachThreadStateToThread(isThreadStateSafe);

			// Run function loop for the current thread state and team
			FunctionState nextFunction = headFunction;
			do {

				// Ensure appropriate thread state
				if (nextFunction.getThreadState() != this) {
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

				// Required team, thread state and safety, so execute
				nextFunction = nextFunction.execute();

			} while (nextFunction != null);

		} finally {
			// Detach thread state from the thread
			threadState.detachThreadStateFromThread();

			// Re-attach possible previous thread state
			if (existingThreadState != null) {
				existingThreadState.threadState.attachThreadStateToThread(existingThreadState.isThreadStateSafe);
			}
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
		 * {@link TeamIdentifier} of the current {@link Team} executing this
		 * {@link Job}.
		 */
		protected TeamIdentifier currentTeam;

		/**
		 * Instantiate.
		 * 
		 * @param initialFunction
		 *            Initial {@link FunctionState}.
		 * @param currentTeam
		 *            {@link TeamIdentifier} of the current {@link Team}
		 *            executing this {@link Job}.
		 */
		public UnsafeLoop(FunctionState initialFunction, TeamIdentifier currentTeam) {
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
					return FunctionLoopImpl.this.executeThreadStateJobNodeLoop(headFunction, true, this.currentTeam);
				}
			} else {
				// Execute loop unsafely (only one thread, avoid overheads)
				return FunctionLoopImpl.this.executeThreadStateJobNodeLoop(headFunction, false, this.currentTeam);
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
				nextFunction = this.doThreadStateFunctionLoop(nextFunction, nextFunction.isRequireThreadStateSafety());

			} while (nextFunction != null);
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
		 *            Current {@link TeamIdentifier}.
		 */
		public SafeLoop(FunctionState initialFunction, TeamIdentifier currentTeam) {
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

}