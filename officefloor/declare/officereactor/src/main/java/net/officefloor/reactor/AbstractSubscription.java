/*-
 * #%L
 * Reactor
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

package net.officefloor.reactor;

import java.util.function.Consumer;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.clazz.method.MethodReturnTranslator;

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
