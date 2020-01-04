package net.officefloor.reactor;

import net.officefloor.frame.api.function.AsynchronousFlow;
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

		// Create asynchronous flow
		AsynchronousFlow flow = context.getManagedFunctionContext().createAsynchronousFlow();

		// Subscribe to the Mono
		returnValue.subscribe(
				(success) -> flow.complete(() -> context.getManagedFunctionContext().setNextFunctionArgument(success)),
				(failure) -> flow.complete(() -> {
					throw failure;
				}));
	}

}