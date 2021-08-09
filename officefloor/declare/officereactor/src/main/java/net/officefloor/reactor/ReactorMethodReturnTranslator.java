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

import java.util.function.BiConsumer;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.plugin.clazz.method.MethodReturnTranslator;
import net.officefloor.plugin.clazz.method.MethodReturnTranslatorContext;

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
