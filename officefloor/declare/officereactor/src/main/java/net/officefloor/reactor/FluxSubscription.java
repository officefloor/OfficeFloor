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