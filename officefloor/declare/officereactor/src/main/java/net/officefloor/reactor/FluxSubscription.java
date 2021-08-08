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
