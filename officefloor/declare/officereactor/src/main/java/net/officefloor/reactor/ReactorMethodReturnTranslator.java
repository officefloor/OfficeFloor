package net.officefloor.reactor;

import java.util.function.BiConsumer;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.managedfunction.method.MethodReturnTranslator;
import net.officefloor.plugin.managedfunction.method.MethodReturnTranslatorContext;

/**
 * Reactor {@link MethodReturnTranslator}.
 * 
 * @author Daniel Sagenschneider
 */
public class ReactorMethodReturnTranslator<R, T> implements MethodReturnTranslator<R, T> {

	/**
	 * Undertakes subscription.
	 */
	private final BiConsumer<R, ManagedFunctionContext<?, ?>> subscriber;

	/**
	 * Instantiate.
	 * 
	 * @param subscriber Undertakes subscription.
	 */
	public ReactorMethodReturnTranslator(BiConsumer<R, ManagedFunctionContext<?, ?>> subscriber) {
		this.subscriber = subscriber;
	}

	/*
	 * ======================= MethodReturnTranslator ========================
	 */

	@Override
	public void translate(MethodReturnTranslatorContext<R, T> context) throws Exception {

		// Obtain the return
		R returnValue = context.getReturnValue();
		if (returnValue == null) {
			return; // must have instance
		}

		// Subscribe
		this.subscriber.accept(returnValue, context.getManagedFunctionContext());
	}

}