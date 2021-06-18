/*-
 * #%L
 * Vertx
 * %%
 * Copyright (C) 2005 - 2021 Daniel Sagenschneider
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
