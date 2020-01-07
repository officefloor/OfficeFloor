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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import net.officefloor.frame.api.function.ManagedFunctionContext;
import reactor.core.publisher.Flux;

/**
 * {@link Flux} subscription.
 * 
 * @author Daniel Sagenschneider
 */
public class FluxSubscription<T> extends AbstractSubscription<T> {

	/**
	 * Subscribes to the {@link Flux}.
	 * 
	 * @param <T>           Success type.
	 * @param componentType Component type for success array.
	 * @param flux          {@link Flux}.
	 * @param context       {@link ManagedFunctionContext}.
	 */
	public static <T> void subscribe(Class<?> componentType, Flux<T> flux, ManagedFunctionContext<?, ?> context) {
		FluxSubscription<T> subscription = new FluxSubscription<>(componentType, context);
		flux.subscribe(subscription.getSuccess(), subscription.getError(), subscription.getCompletion());
	}

	/**
	 * Component type for success array.
	 */
	private final Class<?> componentType;

	/**
	 * Successes.
	 */
	private final List<T> successes = new ArrayList<>();

	/**
	 * Instantiate.
	 * 
	 * @param context {@link ManagedFunctionContext}.
	 */
	public FluxSubscription(Class<?> comonentType, ManagedFunctionContext<?, ?> context) {
		super(context);
		this.componentType = comonentType;
	}

	/*
	 * =============== AbstractSubscription ================
	 */

	@Override
	protected void addSuccess(T success) {
		this.successes.add(success);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Object getNextFunctionArgument() {
		return this.componentType == null ? null
				: this.successes.toArray((T[]) Array.newInstance(this.componentType, this.successes.size()));
	}

}
