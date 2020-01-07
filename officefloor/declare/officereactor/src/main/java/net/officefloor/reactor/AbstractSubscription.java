/*-
 * #%L
 * Reactor
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.reactor;

import java.util.function.Consumer;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.managedfunction.method.MethodReturnTranslator;

/**
 * Abstract {@link MethodReturnTranslator}.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSubscription<T> {

	/**
	 * {@link ManagedFunctionContext}.
	 */
	private final ManagedFunctionContext<?, ?> context;

	/**
	 * {@link AsynchronousFlow}.
	 */
	private final AsynchronousFlow flow;

	/**
	 * Failure.
	 */
	private Throwable failure = null;

	/**
	 * Instantiate.
	 * 
	 * @param context {@link ManagedFunctionContext}.
	 */
	public AbstractSubscription(ManagedFunctionContext<?, ?> context) {
		this.context = context;
		this.flow = context.createAsynchronousFlow();
	}

	/**
	 * Adds the success.
	 * 
	 * @param success Success.
	 */
	protected abstract void addSuccess(T success);

	/**
	 * Obtains the next {@link ManagedFunction} argument.
	 * 
	 * @return Next {@link ManagedFunction} argument.
	 */
	protected abstract Object getNextFunctionArgument();

	/**
	 * Obtains the success {@link Consumer}.
	 * 
	 * @return Success {@link Consumer}.
	 */
	public Consumer<? super T> getSuccess() {
		return (success) -> {
			if (this.failure == null) {
				this.addSuccess(success);
			}
		};
	}

	/**
	 * Obtain the error {@link Consumer}.
	 * 
	 * @return Error {@link Consumer}.
	 */
	public Consumer<? super Throwable> getError() {
		return (exception) -> {
			if (this.failure == null) {
				this.failure = exception;
				this.flow.complete(() -> {
					throw exception;
				});
			}
		};
	}

	/**
	 * Obtains the completion.
	 * 
	 * @return Completion.
	 */
	public Runnable getCompletion() {
		return () -> {
			if (this.failure == null) {
				Object nextFunctionArgument = this.getNextFunctionArgument();
				this.flow.complete(() -> this.context.setNextFunctionArgument(nextFunctionArgument));
			}
		};
	}

}
