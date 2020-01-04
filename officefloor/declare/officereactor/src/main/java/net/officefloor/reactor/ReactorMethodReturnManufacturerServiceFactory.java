package net.officefloor.reactor;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.managedfunction.method.MethodReturnManufacturer;
import net.officefloor.plugin.managedfunction.method.MethodReturnManufacturerContext;
import net.officefloor.plugin.managedfunction.method.MethodReturnManufacturerServiceFactory;
import net.officefloor.plugin.managedfunction.method.MethodReturnTranslator;
import reactor.core.publisher.Mono;

/**
 * Reactor {@link MethodReturnManufacturerServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class ReactorMethodReturnManufacturerServiceFactory<T>
		implements MethodReturnManufacturerServiceFactory, MethodReturnManufacturer<Mono<T>, T> {

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
	public MethodReturnTranslator<Mono<T>, T> createReturnTranslator(MethodReturnManufacturerContext<T> context)
			throws Exception {

		// Determine if return type a Mono
		Class<?> returnClass = context.getReturnClass();
		if (!Mono.class.equals(returnClass)) {
			return null; // not Mono
		}

		// Determine the translated return type
		Class<?> translatedClass = Object.class;
		Type monoType = context.getMethod().getGenericReturnType();
		if (monoType instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) monoType;
			for (Type type : paramType.getActualTypeArguments()) {
				if (type instanceof Class) {
					Class<?> typeClass = (Class<?>) type;
					translatedClass = Void.class.equals(typeClass) ? null : typeClass;
				}
			}
		}

		// Specify the translated return
		context.setTranslatedReturnClass((Class<T>) translatedClass);
		context.addEscalation(Throwable.class);

		// Return the translator
		return new ReactorMethodReturnTranslator<T>();
	}

}