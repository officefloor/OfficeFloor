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