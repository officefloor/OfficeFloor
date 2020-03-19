package net.officefloor.servlet.supply;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import javax.servlet.Servlet;

import org.apache.jasper.tagplugins.jstl.core.If;

import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

/**
 * Injector of dependencies for the {@link Servlet}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServletInjector {

	/**
	 * {@link FieldInjector} instances.
	 */
	private final FieldInjector[] injectors;

	/**
	 * Initiate.
	 * 
	 * @param clazz         {@link Class} of object to supply dependencies.
	 * @param sourceContext {@link SupplierSourceContext}.
	 */
	public ServletInjector(Class<?> clazz, SupplierSourceContext sourceContext) {

		// Obtain the class loader
		ClassLoader classLoader = sourceContext.getClassLoader();

		// Load the dependencies
		List<FieldInjector> injectors = new ArrayList<>();
		while (clazz != null) {

			// Load injectors for declared fields
			NEXT_FIELD: for (Field field : clazz.getDeclaredFields()) {

				// Determine if dependency
				if (!field.isAnnotationPresent(Dependency.class)) {
					continue NEXT_FIELD; // not dependency
				}

				// Obtain the supplier dependency
				Class<?> fieldType = field.getType();
				SupplierThreadLocal<?> threadLocal = sourceContext.addSupplierThreadLocal(null, fieldType);

				// Create the dependency
				Object dependency;
				if (fieldType.isInterface()) {

					// Create proxy for dependency
					dependency = Proxy.newProxyInstance(classLoader, new Class<?>[] { fieldType },
							(proxy, method, args) -> {

								// Obtain the dependency
								Object service = threadLocal.get();

								// Invoke functionality of service
								Method serviceMethod = service.getClass().getMethod(method.getName(),
										method.getParameterTypes());
								return serviceMethod.invoke(service, args);
							});

				} else {
					// Create class proxy for dependency
					Enhancer enhancer = new Enhancer();
					enhancer.setSuperclass(fieldType);
					enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {

						// Obtain the dependency
						Object service = threadLocal.get();

						// Invoke functionliaty of service
						Method serviceMethod = service.getClass().getMethod(method.getName(),
								method.getParameterTypes());
						return serviceMethod.invoke(service, args);
					});
					dependency = enhancer.create();
				}

				// Include the dependency
				injectors.add(new FieldInjector(field, dependency));
			}

			// Continue with super
			clazz = clazz.getSuperclass();
		}

		// Specify the injectors
		this.injectors = injectors.toArray(new FieldInjector[injectors.size()]);
	}

	/**
	 * Injects dependencies onto the object.
	 * 
	 * @param object Object to receive the dependencies.
	 * @throws Exception If fails to load the dependencies.
	 */
	public void inject(Object object) throws Exception {
		for (FieldInjector injector : this.injectors) {
			injector.inject(object);
		}
	}

	/**
	 * Visits all the injections.
	 * 
	 * @param visitor Visitor to receive qualifier and type of injection.
	 */
	public void visit(BiConsumer<String, Class<?>> visitor) {
		for (FieldInjector injector : this.injectors) {
			visitor.accept(null, injector.field.getType());
		}
	}

	/**
	 * Injects the dependency for the {@link Field}.
	 */
	private static class FieldInjector {

		/**
		 * {@link Field} to inject the dependency.
		 */
		private final Field field;

		/**
		 * Dependency for the {@link Field}.
		 */
		private final Object dependency;

		/**
		 * Instantiate.
		 * 
		 * @param field      {@link Field} to inject the dependency.
		 * @param dependency Dependency for the {@link Field}.
		 */
		private FieldInjector(Field field, Object dependency) {
			this.field = field;
			this.field.setAccessible(true);
			this.dependency = dependency;
		}

		/**
		 * Injects the dependency onto the object.
		 * 
		 * @param object Object to receive the dependency.
		 * @throws If fails to inject the dependency.
		 */
		private void inject(Object object) throws Exception {
			this.field.set(object, this.dependency);
		}
	}

}