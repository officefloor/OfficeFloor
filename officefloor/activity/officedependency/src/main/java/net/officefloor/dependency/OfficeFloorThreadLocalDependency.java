/*-
 * #%L
 * Dependency
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

package net.officefloor.dependency;

import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Supplier;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.test.module.ModuleAccessible;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.NoOp;

/**
 * {@link OfficeFloor} {@link ThreadLocal} dependency.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorThreadLocalDependency {

	/**
	 * Creates a proxy dependency that delegates to the {@link SupplierThreadLocal}
	 * of the current {@link Thread}.
	 * 
	 * @param <T>                 Dependency type.
	 * @param type                Type.
	 * @param classLoader         {@link ClassLoader}.
	 * @param supplierThreadLocal {@link Supplier} to provide the
	 *                            {@link SupplierThreadLocal} dependency.
	 * @return Static dependency.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T newStaticProxy(Class<? extends T> type, ClassLoader classLoader,
			Supplier<? extends T> supplierThreadLocal) {

		// Create the static dependency
		Object dependency;
		if (type.isInterface()) {

			// Create proxy for dependency
			dependency = Proxy.newProxyInstance(classLoader, new Class<?>[] { type }, (proxy, method, args) -> {

				// Obtain the dependency
				Object service = supplierThreadLocal.get();

				// Invoke functionality of service
				Method serviceMethod = service.getClass().getMethod(method.getName(), method.getParameterTypes());
				serviceMethod.setAccessible(true);
				try {
					return serviceMethod.invoke(service, args);
				} catch (InvocationTargetException ex) {
					throw ex.getCause();
				}
			});

		} else {
			try {
				// Create class proxy for dependency
				Enhancer enhancer = new Enhancer() {
					@Override
					@SuppressWarnings("rawtypes")
					protected void filterConstructors(Class sc, List constructors) {
						// No filtering to allow proxy of any class
					}
				};
				enhancer.setUseFactory(true);
				enhancer.setSuperclass(type);
				enhancer.setCallbackType(InvocationHandler.class);
				Class<?> dependencyClass = enhancer.createClass();

				// Instantiate the proxy dependency
				Objenesis objensis = new ObjenesisStd(false);
				Factory dependencyFactory = (Factory) objensis.getInstantiatorOf(dependencyClass).newInstance();

				// Register call back handling
				InvocationHandler handler = (obj, method, args) -> {

					// Obtain the dependency
					Object service = supplierThreadLocal.get();

					// Invoke functionality of service
					Method serviceMethod = service.getClass().getMethod(method.getName(), method.getParameterTypes());
					serviceMethod.setAccessible(true);
					try {
						return serviceMethod.invoke(service, args);
					} catch (InvocationTargetException ex) {
						throw ex.getCause();
					}
				};
				dependencyFactory.setCallbacks(new Callback[] { handler, NoOp.INSTANCE });

				// Specify the dependency
				dependency = dependencyFactory;

			} catch (ExceptionInInitializerError ex) {
				// Handle module inaccessible error
				Throwable cause = ex.getCause();
				while (cause != null) {
					if (cause instanceof InaccessibleObjectException) {
						throw new InaccessibleObjectException("Unable to create thread local proxy for "
								+ type.getName() + ", due to:\n" + cause.getMessage()
								+ "\n\nDue to module restrictions, change dependency to be an interface.\n\nHowever, if not possible then will need to open module with JVM argument:\n\n"
								+ ModuleAccessible.getOpenModuleJvmArgument("<module.name>", "<package.name>")
								+ "\n\nHowever, be careful using this in production as opens up security concerns.");
					}
					cause = cause.getCause();
				}

				// Not module access issue, so propagate
				throw ex;
			}
		}

		// Return the dependency
		return (T) dependency;
	}

}
