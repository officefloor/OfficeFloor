/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2018 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.frame.impl.execute.job;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.execute.function.AbstractDelegateFunctionState;
import net.officefloor.frame.impl.execute.function.AbstractFunctionState;
import net.officefloor.frame.impl.execute.officefloor.OfficeFloorImpl;
import net.officefloor.frame.impl.execute.team.TeamManagementImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.internal.structure.EscalationCompletion;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.FunctionStateContext;
import net.officefloor.frame.internal.structure.ProcessState;
import net.officefloor.frame.internal.structure.TeamManagement;
import net.officefloor.frame.internal.structure.ThreadState;
import net.officefloor.frame.internal.structure.ThreadStateContext;

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
	 * Instantiates.
	 * 
	 * @param defaultTeam Default {@link TeamManagement}. May be <code>null</code>.
	 */
	public FunctionLoopImpl(TeamManagement defaultTeam) {

		// Ensure have default team
		this.defaultTeam = (defaultTeam != null) ? defaultTeam
				: new TeamManagementImpl(PassiveTeamSource.createPassiveTeam());
	}

	/**
	 * Handles overloaded {@link Team} {@link Throwable}.
	 * 
	 * @param function   {@link FunctionState} attempting to be assigned to
	 *                   {@link Team}.
	 * @param escalation {@link Escalation} from {@link Team} indicated unable to
	 *                   accept {@link Job}.
	 * @return {@link FunctionState} to handle the overloaded {@link Team}.
	 */
	private FunctionState handleOverloadedTeam(FunctionState function, Throwable escalation) {

		// Team for function is overloaded, so avoid
		Object avoidTeamIdentifier = function.getResponsibleTeam().getIdentifier();

		// Handle escalation (avoiding overloaded team)
		HandleOverloadedTeamEscalationCompletion escalationCompletion = new HandleOverloadedTeamEscalationCompletion(
				function.getThreadState());
		HandleOverloadedTeamFunctionState handleFunction = new HandleOverloadedTeamFunctionState(function,
				avoidTeamIdentifier, escalationCompletion);
		return handleFunction.handleEscalation(escalation, escalationCompletion);
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
		try {

			// Ensure handle overloaded team
			team.assignJob(loop);

		} catch (Throwable ex) {
			this.delegateFunction(this.handleOverloadedTeam(function, ex));
		}
	}

	/**
	 * Undertakes the {@link FunctionState} loop for a particular
	 * {@link ThreadState}.
	 * 
	 * @param threadState       Particular {@link ThreadState}.
	 * @param headFunction      Head {@link FunctionState}.
	 * @param isThreadStateSafe Flag indicating if changes to the
	 *                          {@link ThreadState} are safe on the current
	 *                          {@link Thread}.
	 * @param currentTeam       Identifier of the current {@link Team}.
	 * @return Optional next {@link FunctionState} that requires execution by
	 *         another {@link ThreadState} (or {@link TeamManagement}).
	 */
	private FunctionState executeThreadStateFunctionLoop(FunctionState headFunction, boolean isThreadStateSafe,
			Object currentTeam) {

		// Obtain the thread state for loop
		ThreadState threadState = headFunction.getThreadState();

		// Attach thread state to thread
		ThreadStateContext context = ThreadStateImpl.attachThreadStateToThread(threadState, isThreadStateSafe);

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
					if ((!isThreadStateSafe)
							&& (nextFunction.isRequireThreadStateSafety() || context.isRequireThreadStateSafety())) {
						// Exit loop to obtain thread state safety
						return nextFunction;
					}

					// Required team, thread state and safety, so execute
					nextFunction = context.executeFunction(nextFunction);

				} catch (Throwable ex) {

					// Handle escalation
					nextFunction = nextFunction.handleEscalation(ex, null);
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
	 * {@link ProcessState} to avoid synchronising overheads if no other responsible
	 * {@link TeamManagement} nor {@link ThreadState} is involved.
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
		 * @param initialFunction Initial {@link FunctionState}.
		 * @param currentTeam     Identifier of the current {@link Team} executing this
		 *                        {@link Job}.
		 */
		public UnsafeLoop(FunctionState initialFunction, Object currentTeam) {
			this.initialFunction = initialFunction;
			this.currentTeam = currentTeam;
		}

		/**
		 * Undertakes the {@link FunctionState} for the {@link ThreadState}.
		 * 
		 * @param headFunction             Head {@link FunctionState} for loop.
		 * @param isRequireThreadStateSafe <code>true</code> to provide {@link Thread}
		 *                                 safety on executing the {@link FunctionState}
		 *                                 instances.
		 * @return Optional next {@link FunctionState} that requires execution by
		 *         another {@link ThreadState} (or {@link TeamManagement} or requires
		 *         {@link Thread} safety).
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
		 * Assigns the {@link FunctionState} to its responsible {@link TeamManagement}.
		 * 
		 * @param function        {@link FunctionState}.
		 * @param responsibleTeam Responsible {@link TeamManagement} for the
		 *                        {@link FunctionState}.
		 * @return Possible {@link FunctionState} to handle not able to assign to
		 *         {@link Team} (as likely overloaded).
		 */
		protected FunctionState assignFunction(FunctionState function, TeamManagement responsibleTeam) {

			// First assigning, so must synchronise thread state
			synchronized (function.getThreadState()) {
			}

			// Obtain the responsible team
			SafeLoop loop = new SafeLoop(function, responsibleTeam.getIdentifier());
			Team team = responsibleTeam.getTeam();
			try {

				// Assign the function to the responsible team
				team.assignJob(loop);
				return null; // job successfully assigned

			} catch (Throwable ex) {
				// Handle (likely overloaded) team
				return FunctionLoopImpl.this.handleOverloadedTeam(function, ex);
			}
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
					nextFunction = this.assignFunction(nextFunction, responsible);
					if (nextFunction == null) {
						return; // assigned to team
					}
				}

				// Undertake loop for thread state
				boolean isRequireThreadStateSafety = nextFunction.isRequireThreadStateSafety();
				nextFunction = this.doThreadStateFunctionLoop(nextFunction, isRequireThreadStateSafety);

			} while (nextFunction != null);
		}

		@Override
		public void cancel(Throwable cause) {
			try {

				// Handle cancellation of the job
				FunctionState handler = this.initialFunction.handleEscalation(cause, null);
				SafeLoop loop = new SafeLoop(handler, this.currentTeam);
				loop.run();

			} catch (Throwable ex) {
				// Log failure to cancel job
				LOGGER.log(Level.WARNING, "Failed to handle cancellation of job", cause);
			}
		}
	}

	/**
	 * {@link Thread} safe {@link FunctionState} loop {@link Job} implementation.
	 */
	private class SafeLoop extends UnsafeLoop {

		/**
		 * Instantiate.
		 * 
		 * @param initialFunction Initial {@link FunctionState}.
		 * @param currentTeam     Current {@link Team} identifier.
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
		protected FunctionState assignFunction(FunctionState function, TeamManagement responsibleTeam) {

			// No need to synchronise assigning function, as loop is thread safe
			SafeLoop loop = new SafeLoop(function, responsibleTeam.getIdentifier());
			Team team = responsibleTeam.getTeam();
			try {

				// Assign the job
				team.assignJob(loop);
				return null; // job successfully assigned

			} catch (Throwable ex) {
				// Handle (likely overloaded) team
				return FunctionLoopImpl.this.handleOverloadedTeam(function, ex);
			}
		}
	}

	/**
	 * {@link EscalationCompletion} to be notified when {@link Escalation} handling
	 * of overloaded {@link Team} is handled.
	 */
	private static class HandleOverloadedTeamEscalationCompletion implements EscalationCompletion {

		/**
		 * Indicates if the {@link Escalation} has been handled.
		 */
		private boolean isEscalationHandled = false;

		/**
		 * {@link ThreadState}.
		 */
		private final ThreadState threadState;

		/**
		 * Instantiate.
		 * 
		 * @param threadState {@link ThreadState} to notify of
		 *                    {@link EscalationCompletion}.
		 */
		private HandleOverloadedTeamEscalationCompletion(ThreadState threadState) {
			this.threadState = threadState;
		}

		/*
		 * =================== EscalationCompletion ====================
		 */

		@Override
		public FunctionState escalationComplete() {
			return new AbstractFunctionState(this.threadState) {
				@Override
				public FunctionState execute(FunctionStateContext context) throws Throwable {

					// Flag escalation handled
					HandleOverloadedTeamEscalationCompletion.this.isEscalationHandled = true;
					return null;
				}
			};
		}
	}

	/**
	 * <p>
	 * Wrapping {@link FunctionState} to handle {@link Escalation} from {@link Team}
	 * about being overloaded.
	 * <p>
	 * This overrides the {@link Team} so that the handled {@link Escalation}
	 * {@link FunctionState} is not immediately reassigned to the overloaded
	 * {@link Team} (causing infinite loop on further back pressure exceptions).
	 */
	private static class HandleOverloadedTeamFunctionState extends AbstractDelegateFunctionState {

		/**
		 * {@link Team} to avoid sending due to overload.
		 */
		private final Object overloadedTeamIdentifier;

		/**
		 * {@link HandleOverloadedTeamEscalationCompletion}.
		 */
		private final HandleOverloadedTeamEscalationCompletion escalationCompletion;

		/**
		 * Instantiate.
		 * 
		 * @param delegate                 Delegate {@link FunctionState}.
		 * @param overloadedTeamIdentifier Identifier of {@link Team} to avoid.
		 * @param escalationCompletion     {@link HandleOverloadedTeamEscalationCompletion}.
		 */
		public HandleOverloadedTeamFunctionState(FunctionState delegate, Object overloadedTeamIdentifier,
				HandleOverloadedTeamEscalationCompletion escalationCompletion) {
			super(delegate);
			this.overloadedTeamIdentifier = overloadedTeamIdentifier;
			this.escalationCompletion = escalationCompletion;
		}

		/**
		 * Continues to avoid the overloaded {@link Team}.
		 * 
		 * @param functionState {@link FunctionState}.
		 * @return {@link FunctionState} to avoid the overloaded {@link Team}.
		 */
		private FunctionState avoidOverloadedTeam(FunctionState functionState) {

			// Determine if complete
			if (functionState == null) {
				return null;
			}

			// Determine if escalation handled
			if (this.escalationCompletion.isEscalationHandled) {
				// No further wrapping as team escalation handled.
				// Allows for attempting the team again.
				return functionState;
			}

			// Continue to avoid the overloaded team
			return new HandleOverloadedTeamFunctionState(functionState, this.overloadedTeamIdentifier,
					this.escalationCompletion);
		}

		/*
		 * ===================== FunctionState ========================
		 */

		@Override
		public FunctionState execute(FunctionStateContext context) throws Throwable {
			return this.avoidOverloadedTeam(this.delegate.execute(context));
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation, EscalationCompletion completion) {
			return this.avoidOverloadedTeam(this.delegate.handleEscalation(escalation, completion));
		}

		@Override
		public FunctionState cancel() {
			return this.avoidOverloadedTeam(this.delegate.cancel());
		}

		@Override
		public TeamManagement getResponsibleTeam() {

			// Obtain the required team
			TeamManagement requiredTeam = this.delegate.getResponsibleTeam();

			// Allow other threads to attempt team
			if (this.escalationCompletion.threadState != this.delegate.getThreadState()) {
				return requiredTeam;
			}

			// Determine if override team
			if (this.escalationCompletion.isEscalationHandled) {
				return requiredTeam; // no further overriding team
			}

			// If the team to avoid, then allow any team to process.
			// This causes back pressure on these teams to also slow.
			Object requiredTeamIdentifier = (requiredTeam != null) ? requiredTeam.getIdentifier() : null;
			if (requiredTeamIdentifier == this.overloadedTeamIdentifier) {
				return null; // continue with existing team
			}

			// Allow required team to be attempted
			return requiredTeam;
		}
	}

}