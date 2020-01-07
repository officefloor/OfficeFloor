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

import net.officefloor.frame.api.function.ManagedFunctionContext;
import reactor.core.publisher.Mono;

/**
 * {@link Mono} subscription.
 * 
 * @author Daniel Sagenschneider
 */
public class MonoSubscription<T> extends AbstractSubscription<T> {

	/**
	 * Subscribes to the {@link Mono}.
	 * 
	 * @param <T>     Success type.
	 * @param mono    {@link Mono}.
	 * @param context {@link ManagedFunctionContext}.
	 */
	public static <T> void subscribe(Mono<T> mono, ManagedFunctionContext<?, ?> context) {
		MonoSubscription<T> subscription = new MonoSubscription<>(context);
		mono.subscribe(subscription.getSuccess(), subscription.getError(), subscription.getCompletion());
	}

	/**
	 * Success.
	 */
	private T success = null;

	/**
	 * Instantiate.
	 * 
	 * @param context {@link ManagedFunctionContext}.
	 */
	public MonoSubscription(ManagedFunctionContext<?, ?> context) {
		super(context);
	}

	/*
	 * =============== AbstractSubscription ================
	 */

	@Override
	protected void addSuccess(T success) {
		this.success = success;
	}

	@Override
	protected Object getNextFunctionArgument() {
		return this.success;
	}

}
