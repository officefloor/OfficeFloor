/*-
 * #%L
 * Dependency
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
