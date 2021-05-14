package net.officefloor.vertx;

import io.vertx.core.Future;
import net.officefloor.plugin.clazz.method.MethodReturnTranslator;
import net.officefloor.plugin.clazz.method.MethodReturnTranslatorContext;

/**
 * Reactor {@link MethodReturnTranslator}.
 * 
 * @author Daniel Sagenschneider
 */
public class VertxMethodReturnTranslator<T> implements MethodReturnTranslator<Future<T>, T> {

	/*
	 * ======================= MethodReturnTranslator ========================
	 */

	@Override
	public void translate(MethodReturnTranslatorContext<Future<T>, T> context) throws Exception {

		// Obtain the return
		Future<T> returnFuture = context.getReturnValue();
		if (returnFuture == null) {
			return; // must have instance
		}

		// Obtain result
		T returnValue = OfficeFloorVertx.block(returnFuture);
		context.setTranslatedReturnValue(returnValue);
	}

}