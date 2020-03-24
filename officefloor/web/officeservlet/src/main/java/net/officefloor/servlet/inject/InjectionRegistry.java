package net.officefloor.servlet.inject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.plugin.managedobject.clazz.Dependency;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.NoOp;

/**
 * Registry of injections.
 * 
 * @author Daniel Sagenschneider
 */
public class InjectionRegistry {

	/**
	 * {@link InjectDependency} instances.
	 */
	private final List<InjectDependency> dependencies = new LinkedList<>();

	/**
	 * {@link InjectDependency} instances for a {@link Class}.
	 */
	private final Map<Class<?>, FieldToDependency[]> classToDependencies = new HashMap<>();

	/**
	 * Registers the {@link Class} for injection.
	 * 
	 * @param clazz           {@link Class} to have dependencies injected.
	 * @param supplierContext {@link SupplierSourceContext} for the dependencies.
	 */
	public void registerForInjection(Class<?> clazz, SupplierSourceContext supplierContext) {

		// Keep track of original class
		final Class<?> originalClass = clazz;

		// Obtain the class loader
		ClassLoader classLoader = supplierContext.getClassLoader();

		// Load the dependencies
		List<FieldToDependency> fieldsToDependencies = new ArrayList<>();
		while (clazz != null) {

			// Load injectors for declared fields
			NEXT_FIELD: for (Field field : clazz.getDeclaredFields()) {

				// Determine if dependency
				if (!field.isAnnotationPresent(Dependency.class)) {
					continue NEXT_FIELD; // not dependency
				}

				// Obtain the supplier dependency
				String fieldQualifier = null;
				Class<?> fieldType = field.getType();
				InjectDependency injectDependency = null;
				SEARCH_DEPENDENCY: for (InjectDependency check : this.dependencies) {

					// Determine if match
					if (!fieldType.equals(check.type)) {
						continue SEARCH_DEPENDENCY; // not type, so go to next
					}

					// Determine if qualifier match
					if ((fieldQualifier != null) && (check.qualifier != null)
							&& (fieldQualifier.equals(check.qualifier))) {
						injectDependency = check;
						break SEARCH_DEPENDENCY; // found dependency
					}

					// Determine if no qualifier
					if ((fieldQualifier == null) && (check.qualifier == null)) {
						injectDependency = check;
						break SEARCH_DEPENDENCY; // found dependency
					}
				}
				if (injectDependency == null) {

					// No existing dependency, so create one

					// Obtain index for new dependency
					int dependencyIndex = this.dependencies.size();

					// Create the dependency
					Object dependency;
					if (fieldType.isInterface()) {

						// Create proxy for dependency
						dependency = Proxy.newProxyInstance(classLoader, new Class<?>[] { fieldType },
								(proxy, method, args) -> {

									// Obtain the dependency
									Object service = InjectContext.getActiveDependency(dependencyIndex);

									// Invoke functionality of service
									Method serviceMethod = service.getClass().getMethod(method.getName(),
											method.getParameterTypes());
									return serviceMethod.invoke(service, args);
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
						enhancer.setSuperclass(fieldType);
						enhancer.setCallbackType(InvocationHandler.class);
						Class<?> dependencyClass = enhancer.createClass();

						// Instantiate the proxy dependency
						Objenesis objensis = new ObjenesisStd();
						Factory dependencyFactory = (Factory) objensis.getInstantiatorOf(dependencyClass).newInstance();

						// Register call back handling
						InvocationHandler handler = (obj, method, args) -> {

							// Obtain the dependency
							Object service = InjectContext.getActiveDependency(dependencyIndex);

							// Invoke functionality of service
							Method serviceMethod = service.getClass().getMethod(method.getName(),
									method.getParameterTypes());
							return serviceMethod.invoke(service, args);
						};
						dependencyFactory.setCallbacks(new Callback[] { handler, NoOp.INSTANCE });

						// Specify the dependency
						dependency = dependencyFactory;
					}

					// Create the supplier thread local
					SupplierThreadLocal<?> threadLocal = supplierContext.addSupplierThreadLocal(fieldQualifier,
							fieldType);

					// Create and add the dependency
					injectDependency = new InjectDependency(fieldQualifier, fieldType, threadLocal, dependency);
					this.dependencies.add(injectDependency);
				}

				// Include the dependency
				fieldsToDependencies.add(new FieldToDependency(field, injectDependency));
			}

			// Continue with super
			clazz = clazz.getSuperclass();
		}

		// Register the class
		this.classToDependencies.put(originalClass,
				fieldsToDependencies.toArray(new FieldToDependency[fieldsToDependencies.size()]));
	}

	/**
	 * Creates the {@link InjectContextFactory}.
	 * 
	 * @return {@link InjectContextFactory}.
	 */
	public InjectContextFactory createInjectContextFactory() {

		// Create the list of supplier thread locals
		SupplierThreadLocal<?>[] supplierThreadLocals = new SupplierThreadLocal[this.dependencies.size()];
		for (int i = 0; i < supplierThreadLocals.length; i++) {
			supplierThreadLocals[i] = this.dependencies.get(i).supplierThreadLocal;
		}

		// Create the map of class to fields
		Map<Class<?>, InjectField[]> classToFields = new HashMap<>();
		this.classToDependencies.forEach((clazz, fieldsToDependencies) -> {

			// Create the injection fields
			InjectField[] fields = new InjectField[fieldsToDependencies.length];
			for (int i = 0; i < fields.length; i++) {
				FieldToDependency fieldToDependency = fieldsToDependencies[i];
				fields[i] = new InjectField(fieldToDependency.field,
						fieldToDependency.injectDependency.proxyDependency);
			}

			// Load injection fields for class
			classToFields.put(clazz, fields);
		});

		// Create and return inject context factory
		return new InjectContextFactory(supplierThreadLocals, classToFields);
	}

	/**
	 * {@link Field} to {@link InjectDependency} mapping.
	 */
	private static class FieldToDependency {

		/**
		 * {@link Field}.
		 */
		private final Field field;

		/**
		 * {@link InjectDependency}.
		 */
		private final InjectDependency injectDependency;

		/**
		 * Instantiate.
		 * 
		 * @param field            {@link Field}.
		 * @param injectDependency {@link InjectDependency}.
		 */
		private FieldToDependency(Field field, InjectDependency injectDependency) {
			this.field = field;
			this.injectDependency = injectDependency;
		}
	}

	/**
	 * Dependency for injection.
	 */
	private static class InjectDependency {

		/**
		 * Qualifier for dependency.
		 */
		private final String qualifier;

		/**
		 * Type of dependency.
		 */
		private final Class<?> type;

		/**
		 * {@link SupplierThreadLocal} to obtain the dependency.
		 */
		private final SupplierThreadLocal<?> supplierThreadLocal;

		/**
		 * Proxy dependency to use the active dependency.
		 */
		private final Object proxyDependency;

		/**
		 * Instantiate.
		 * 
		 * @param qualifier           Qualifier for dependency.
		 * @param type                Type of dependency.
		 * @param supplierThreadLocal {@link SupplierThreadLocal} to obtain the
		 *                            dependency.
		 * @param proxyDependency     Proxy dependency to use the active dependency.
		 */
		private InjectDependency(String qualifier, Class<?> type, SupplierThreadLocal<?> supplierThreadLocal,
				Object proxyDependency) {
			this.qualifier = qualifier;
			this.type = type;
			this.supplierThreadLocal = supplierThreadLocal;
			this.proxyDependency = proxyDependency;
		}
	}

}