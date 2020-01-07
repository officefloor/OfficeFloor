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
