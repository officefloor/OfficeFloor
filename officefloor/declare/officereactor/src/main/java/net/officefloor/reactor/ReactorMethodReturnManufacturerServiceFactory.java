package net.officefloor.reactor;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.reactivestreams.Publisher;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.managedfunction.method.MethodReturnManufacturer;
import net.officefloor.plugin.managedfunction.method.MethodReturnManufacturerContext;
import net.officefloor.plugin.managedfunction.method.MethodReturnManufacturerServiceFactory;
import net.officefloor.plugin.managedfunction.method.MethodReturnTranslator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactor {@link MethodReturnManufacturerServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ReactorMethodReturnManufacturerServiceFactory<R extends Publisher<? super T>, T>
		implements MethodReturnManufacturerServiceFactory, MethodReturnManufacturer<R, T> {

	/*
	 * ================== MethodReturnManufacturerServiceFactory ==================
	 */

	@Override
	public MethodReturnManufacturer<?, ?> createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ========================= MethodReturnManufacturer =========================
	 */

	@Override
	@SuppressWarnings("unchecked")
	public MethodReturnTranslator<R, T> createReturnTranslator(MethodReturnManufacturerContext<T> context)
			throws Exception {

		// Obtain the return class and method
		Class<?> returnClass = context.getReturnClass();
		Method method = context.getMethod();

		// Determine if return type a Mono
		if (Mono.class.equals(returnClass)) {
			context.setTranslatedReturnClass(this.getSuccessClass(method));
			context.addEscalation(Throwable.class);
			return new ReactorMethodReturnTranslator<>((mono, managedFunctionContext) -> MonoSubscription
					.subscribe((Mono<T>) mono, managedFunctionContext));
		}

		// Determine if return type a Flux
		if (Flux.class.equals(returnClass)) {
			Class<?> successClass = this.getSuccessClass(method);
			Class<? super T> translatedClass = successClass == null ? null
					: (Class<? super T>) Array.newInstance(successClass, 0).getClass();
			context.setTranslatedReturnClass(translatedClass);
			context.addEscalation(Throwable.class);
			return new ReactorMethodReturnTranslator<>((flux, managedFunctionContext) -> FluxSubscription
					.subscribe(successClass, (Flux<T>) flux, managedFunctionContext));
		}

		// As here, not Reactor type
		return null;
	}

	/**
	 * Obtains the translated {@link Class}.
	 * 
	 * @param method {@link Method} returning the Reactor type.
	 * @return Translated {@link Class}.
	 */
	@SuppressWarnings("unchecked")
	private Class<? super T> getSuccessClass(Method method) {

		// Determine the translated return type
		Class<? super T> translatedClass = Object.class;
		Type monoType = method.getGenericReturnType();
		if (monoType instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) monoType;
			for (Type type : paramType.getActualTypeArguments()) {
				if (type instanceof Class) {
					Class<? super T> typeClass = (Class<? super T>) type;
					translatedClass = Void.class.equals(typeClass) ? null : typeClass;
				}
			}
		}

		// Return the translated class
		return translatedClass;
	}

}