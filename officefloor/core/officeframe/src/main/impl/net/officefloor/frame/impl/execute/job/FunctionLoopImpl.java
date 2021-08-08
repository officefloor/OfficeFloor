/*-
 * #%L
 * OfficeFrame
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

package net.officefloor.frame.impl.execute.job;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.executive.ProcessIdentifier;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.execute.function.AbstractFunctionState;
import net.officefloor.frame.impl.execute.officefloor.OfficeFloorImpl;
import net.officefloor.frame.impl.execute.team.TeamManagementImpl;
import net.officefloor.frame.impl.execute.thread.ThreadStateImpl;
import net.officefloor.frame.impl.spi.team.PassiveTeamSource;
import net.officefloor.frame.internal.structure.AvoidTeam;
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
	 * @param function       {@link FunctionState} attempting to be assigned to
	 *                       {@link Team}.
	 * @param overloadedTeam Overloaded {@link TeamManagement}.
	 * @param escalation     {@link Escalation} from {@link Team} indicated unable
	 *                       to accept {@link Job}.
	 * @return {@link FunctionState} to handle the overloaded {@link Team}.
	 */
	private FunctionState handleOverloadedTeam(FunctionState function, TeamManagement overloadedTeam,
			Throwable escalation) {

		// Avoid the overloaded team
		ThreadState threadState = function.getThreadState();
		AvoidTeam avoidTeam = threadState.avoidTeam(function, overloadedTeam);

		// Handle escalation (avoiding overloaded team)
		HandleOverloadedTeamEscalationCompletion escalationCompletion = new HandleOverloadedTeamEscalationCompletion(
				avoidTeam, threadState);
		return avoidTeam.getFunctionState().handleEscalation(escalation, escalationCompletion);
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
			FunctionState handlerFunction = this.handleOverloadedTeam(function, responsibleTeam, ex);
			if (handlerFunction != null) {
				this.delegateFunction(handlerFunction);
			}
		}
	}

	/**
	 * Undertakes the {@link FunctionState} loop for a particular
	 * {@link ThreadState}.
	 * 
	 * @param threadState              Particular {@link ThreadState}.
	 * @param headFunction             Head {@link FunctionState}.
	 * @param isRequireThreadStateSafe <code>true</code> to provide {@link Thread}
	 *                                 safety on executing the {@link FunctionState}
	 *                                 instances.
	 * @param currentTeam              Identifier of the current {@link Team}.
	 * @return Optional next {@link FunctionState} that requires execution by
	 *         another {@link ThreadState} (or {@link TeamManagement}).
	 */
	private FunctionState executeThreadStateFunctionLoop(FunctionState headFunction, boolean isRequireThreadStateSafe,
			Object currentTeam) {

		// Obtain the thread state for loop
		ThreadState threadState = headFunction.getThreadState();

		// Attach thread state to thread
		ThreadStateContext context = ThreadStateImpl.attachThreadStateToThread(threadState, isRequireThreadStateSafe);

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
					if ((!context.isThreadStateSafe())
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
		protected final Object currentTeam;

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
			return FunctionLoopImpl.this.executeThreadStateFunctionLoop(headFunction, isRequireThreadStateSafety,
					this.currentTeam);
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
			function.getThreadState().runThreadSafeOperation(null);

			// Obtain the responsible team
			SafeLoop loop = new SafeLoop(function, responsibleTeam.getIdentifier());
			Team team = responsibleTeam.getTeam();
			try {

				// Assign the function to the responsible team
				team.assignJob(loop);
				return null; // job successfully assigned

			} catch (Throwable ex) {
				// Handle (likely overloaded) team
				return FunctionLoopImpl.this.handleOverloadedTeam(function, responsibleTeam, ex);
			}
		}

		/*
		 * ==================== Job ====================
		 */

		@Override
		public ProcessIdentifier getProcessIdentifier() {
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
			return FunctionLoopImpl.this.executeThreadStateFunctionLoop(headFunction, true, this.currentTeam);
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
				return FunctionLoopImpl.this.handleOverloadedTeam(function, responsibleTeam, ex);
			}
		}
	}

	/**
	 * {@link EscalationCompletion} to be notified when {@link Escalation} handling
	 * of overloaded {@link Team} is handled.
	 */
	private static class HandleOverloadedTeamEscalationCompletion implements EscalationCompletion {

		/**
		 * {@link AvoidTeam}.
		 */
		private final AvoidTeam avoidTeam;

		/**
		 * {@link ThreadState}.
		 */
		private final ThreadState threadState;

		/**
		 * Instantiate.
		 * 
		 * @param avoidTeam   {@link AvoidTeam}.
		 * @param threadState {@link ThreadState} to notify of
		 *                    {@link EscalationCompletion}.
		 */
		private HandleOverloadedTeamEscalationCompletion(AvoidTeam avoidTeam, ThreadState threadState) {
			this.avoidTeam = avoidTeam;
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

					// Escalation handled, so stop avoiding team
					HandleOverloadedTeamEscalationCompletion.this.avoidTeam.stopAvoidingTeam();
					return null;
				}
			};
		}
	}

}
