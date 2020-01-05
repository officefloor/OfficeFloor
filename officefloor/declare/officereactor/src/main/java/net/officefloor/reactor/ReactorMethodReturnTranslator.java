package net.officefloor.reactor;

import java.util.function.Consumer;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.managedfunction.method.MethodReturnTranslator;
import net.officefloor.plugin.managedfunction.method.MethodReturnTranslatorContext;
import reactor.core.publisher.Mono;

/**
 * Reactor {@link MethodReturnTranslator}.
 * 
 * @author Daniel Sagenschneider
 */
public class ReactorMethodReturnTranslator<T> implements MethodReturnTranslator<Mono<T>, T> {

	/*
	 * ======================= MethodReturnTranslator ========================
	 */

	@Override
	public void translate(MethodReturnTranslatorContext<Mono<T>, T> context) throws Exception {

		// Obtain the return
		Mono<T> returnValue = context.getReturnValue();
		if (returnValue == null) {
			return; // must have Mono instance
		}

		// Create subscription
		Subscription<T> subscription = new Subscription<>(context.getManagedFunctionContext());

		// Subscribe to the Mono
		returnValue.subscribe(subscription.getSuccess(), subscription.getError(), subscription.getCompletion());
	}

	/**
	 * Subscription to result.
	 */
	private static class Subscription<T> {

		/**
		 * {@link ManagedFunctionContext}.
		 */
		private final ManagedFunctionContext<?, ?> context;

		/**
		 * {@link AsynchronousFlow}.
		 */
		private final AsynchronousFlow flow;

		/**
		 * Success.
		 */
		private T success = null;

		/**
		 * Instantiate.
		 * 
		 * @param context {@link ManagedFunctionContext}.
		 */
		private Subscription(ManagedFunctionContext<?, ?> context) {
			this.context = context;
			this.flow = context.createAsynchronousFlow();
		}

		/**
		 * Obtains the success {@link Consumer}.
		 * 
		 * @return Success {@link Consumer}.
		 */
		private Consumer<? super T> getSuccess() {
			return (success) -> this.success = success;
		}

		/**
		 * Obtain the error {@link Consumer}.
		 * 
		 * @return Error {@link Consumer}.
		 */
		private Consumer<? super Throwable> getError() {
			return (exception) -> this.flow.complete(() -> {
				throw exception;
			});
		}

		/**
		 * Obtains the completion.
		 * 
		 * @return Completion.
		 */
		private Runnable getCompletion() {
			return () -> this.flow.complete(() -> this.context.setNextFunctionArgument(this.success));
		}
	}

}