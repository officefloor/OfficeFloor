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
package net.officefloor.frame.impl.execute.function;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.team.Job;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.impl.execute.team.TeamManagementImpl;
import net.officefloor.frame.impl.spi.team.WorkerPerJobTeam;
import net.officefloor.frame.internal.structure.FunctionLoop;
import net.officefloor.frame.internal.structure.FunctionState;
import net.officefloor.frame.internal.structure.TeamManagement;

/**
 * Provides promise like functionality for {@link FunctionState} instances.
 *
 * @author Daniel Sagenschneider
 */
public class Promise {

	/**
	 * <p>
	 * Execute the {@link FunctionState} then the {@link FunctionState}.
	 * <p>
	 * State is passed between {@link FunctionState} instances via
	 * {@link ManagedObject} instances, so no parameter is provided.
	 * 
	 * @param function
	 *            {@link FunctionState} to execute it and its sequence of
	 *            {@link FunctionState} instances. May be <code>null</code>.
	 * @param thenFunction
	 *            {@link FunctionState} to then continue after the first input
	 *            {@link FunctionState} sequence completes. May be
	 *            <code>null</code>.
	 * @return Next {@link FunctionState} to undertake the {@link FunctionState}
	 *         sequence and then continue {@link FunctionState} sequence. Will
	 *         return <code>null</code> if both inputs are <code>null</code>.
	 */
	public static FunctionState then(FunctionState function, FunctionState thenFunction) {
		if (function == null) {
			// No initial function, so just continue
			return thenFunction;

		} else if (thenFunction != null) {

			// Determine depth of the new then function
			int functionDepth = (function instanceof ThenFunction) ? ((ThenFunction) function).depth : 0;
			int thenFunctionDepth = (thenFunction instanceof ThenFunction) ? ((ThenFunction) thenFunction).depth : 0;
			int newDepth = Math.max(functionDepth, thenFunctionDepth) + 1;

			// Create continue link
			int maximumPromiseChainLength = function.getThreadState().getMaximumPromiseChainLength();
			if (newDepth > maximumPromiseChainLength) {
				// Chain too large, so break
				return new BreakThenFunction(function, thenFunction, 1);
			} else {
				// Continue existing chain
				return new ThenFunction(function, thenFunction, newDepth);
			}
		}

		// Only the initial function
		return function;
	}

	/**
	 * Active {@link Team} to enable breaking the {@link Promise} chain to avoid
	 * {@link StackOverflowError} in calling down the chain.
	 */
	private static final TeamManagement activeTeamToBreakChain = new TeamManagementImpl(
			new WorkerPerJobTeam("BREAK_THEN_CHAIN"));

	/**
	 * All access via static methods.
	 */
	private Promise() {
	}

	/**
	 * Then {@link FunctionState}.
	 */
	private static class ThenFunction extends AbstractDelegateFunctionState {

		/**
		 * Then {@link FunctionState}.
		 */
		protected final FunctionState thenFunction;

		/**
		 * Depth of this {@link ThenFunction};
		 */
		protected final int depth;

		/**
		 * Creation by static methods.
		 * 
		 * @param delegate
		 *            Delegate {@link FunctionState} to complete it and all
		 *            produced {@link FunctionState} instances before
		 *            continuing.
		 * @param thenFunction
		 *            Then {@link FunctionState}.
		 * @param depth
		 *            Depth of this {@link ThenFunction}.
		 */
		private ThenFunction(FunctionState delegate, FunctionState thenFunction, int depth) {
			super(delegate);
			this.thenFunction = thenFunction;
			this.depth = depth;
		}

		@Override
		public String toString() {
			return this.delegate.toString();
		}

		/*
		 * =================== FunctionState ==============================
		 */

		@Override
		public FunctionState execute() throws Throwable {
			FunctionState next = this.delegate.execute();
			return Promise.then(next, this.thenFunction);
		}

		@Override
		public FunctionState handleEscalation(Throwable escalation) {
			FunctionState handler = this.delegate.handleEscalation(escalation);
			return Promise.then(handler, this.thenFunction);
		}

		@Override
		public FunctionState cancel() {
			return Promise.then(this.delegate.cancel(), this.thenFunction.cancel());
		}
	}

	/**
	 * Break then chain {@link FunctionState}.
	 */
	private static class BreakThenFunction extends ThenFunction implements Job {

		/**
		 * Instantiate.
		 * 
		 * @param delegate
		 *            Delegate {@link FunctionState} to complete it and all
		 *            produced {@link FunctionState} instances before
		 *            continuing.
		 * @param thenFunction
		 *            Then {@link FunctionState}.
		 * @param depth
		 *            Depth of this {@link ThenFunction}.
		 */
		public BreakThenFunction(FunctionState delegate, FunctionState thenFunction, int depth) {
			super(delegate, thenFunction, depth);
		}

		/*
		 * =================== FunctionState ==============================
		 */

		@Override
		public FunctionState execute() throws Throwable {

			// Execute on another thread to break chain
			activeTeamToBreakChain.getTeam().assignJob(this);

			// Will continue chain on another thread
			return null;
		}

		/*
		 * ========================= Job ==================================
		 */

		@Override
		public void run() {

			// Continue executing the Promise chain
			FunctionState continueFunction = new ThenFunction(this.delegate, this.thenFunction, this.depth);
			FunctionLoop loop = this.delegate.getThreadState().getProcessState().getFunctionLoop();
			loop.delegateFunction(continueFunction);
		}

		@Override
		public Object getProcessIdentifier() {
			return this.delegate.getThreadState().getProcessState().getProcessIdentifier();
		}

		@Override
		public void cancel(Throwable cause) {
			throw new IllegalStateException("Should never cancel " + BreakThenFunction.class.getSimpleName(), cause);
		}
	}

}