/*-
 * #%L
 * Vertx
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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
