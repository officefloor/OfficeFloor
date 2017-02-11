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
import net.officefloor.frame.internal.structure.FunctionState;

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
	 *         sequence and then continue {@link FunctionState} sequence.
	 */
	public static FunctionState then(FunctionState function, FunctionState thenFunction) {
		if (function == null) {
			// No initial function, so just continue
			return thenFunction;

		} else if (thenFunction != null) {
			// Create continue link
			return new ThenFunction(function, thenFunction);
		}

		// Only the initial function
		return function;
	}

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
		private final FunctionState thenFunction;

		/**
		 * Creation by static methods.
		 * 
		 * @param delegate
		 *            Delegate {@link FunctionState} to complete it and all
		 *            produced {@link FunctionState} instances before
		 *            continuing.
		 * @param thenFunction
		 *            Then {@link FunctionState}.
		 */
		private ThenFunction(FunctionState delegate, FunctionState thenFunction) {
			super(delegate);
			this.thenFunction = thenFunction;
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

}