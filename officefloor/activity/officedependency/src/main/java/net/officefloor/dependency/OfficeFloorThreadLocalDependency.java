package net.officefloor.dependency;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.function.Supplier;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.frame.api.manage.OfficeFloor;
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
		}

		// Return the dependency
		return (T) dependency;
	}

}