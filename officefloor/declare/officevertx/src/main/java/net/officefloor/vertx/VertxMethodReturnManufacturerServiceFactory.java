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

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.method.MethodReturnManufacturer;
import net.officefloor.plugin.clazz.method.MethodReturnManufacturerContext;
import net.officefloor.plugin.clazz.method.MethodReturnManufacturerServiceFactory;
import net.officefloor.plugin.clazz.method.MethodReturnTranslator;

/**
 * {@link Vertx} {@link MethodReturnManufacturerServiceFactory}.
 * 
 * @author Daniel Sagenschneider
 */
public class VertxMethodReturnManufacturerServiceFactory<T>
		implements MethodReturnManufacturerServiceFactory, MethodReturnManufacturer<Future<T>, T> {

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
	public MethodReturnTranslator<Future<T>, T> createReturnTranslator(MethodReturnManufacturerContext<T> context)
			throws Exception {

		// Obtain the return class and method
		Class<?> returnClass = context.getReturnClass();
		Method method = context.getMethod();

		// Determine if return type a Future
		if (Future.class.equals(returnClass)) {
			context.setTranslatedReturnClass(this.getSuccessClass(method));
			context.addEscalation(Throwable.class);
			return new VertxMethodReturnTranslator<>();
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
		Type futureType = method.getGenericReturnType();
		if (futureType instanceof ParameterizedType) {
			ParameterizedType paramType = (ParameterizedType) futureType;
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
