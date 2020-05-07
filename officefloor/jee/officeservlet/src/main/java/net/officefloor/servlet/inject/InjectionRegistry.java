/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet.inject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
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
	 * {@link FieldDependencyExtractor} instances.
	 */
	private final FieldDependencyExtractor[] fieldDependencyExtractors;

	/**
	 * {@link InjectDependency} instances.
	 */
	private final List<InjectDependency> dependencies = new LinkedList<>();

	/**
	 * {@link InjectDependency} instances for a {@link Class}.
	 */
	private final Map<Class<?>, FieldToDependency[]> classToDependencies = new HashMap<>();

	/**
	 * Instantiate.
	 * 
	 * @param fieldDependencyExtractors {@link FieldDependencyExtractor} instances.
	 */
	public InjectionRegistry(FieldDependencyExtractor[] fieldDependencyExtractors) {
		this.fieldDependencyExtractors = fieldDependencyExtractors;
	}

	/**
	 * Obtains the dependency.
	 * 
	 * @param qualifier       Qualifier. May be <code>null</code>.
	 * @param type            Type.
	 * @param supplierContext {@link SupplierSourceContext}.
	 * @return Dependency.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getDependency(String qualifier, Class<? extends T> type, SupplierSourceContext supplierContext) {
		return (T) this.getInjectDependency(qualifier, type, supplierContext).proxyDependency;
	}

	/**
	 * Registers the {@link Class} for injection.
	 * 
	 * @param clazz           {@link Class} to have dependencies injected.
	 * @param supplierContext {@link SupplierSourceContext} for the dependencies.
	 */
	public void registerForInjection(Class<?> clazz, SupplierSourceContext supplierContext) {

		// Keep track of original class
		final Class<?> originalClass = clazz;

		// Load the dependencies
		List<FieldToDependency> fieldsToDependencies = new ArrayList<>();
		while (clazz != null) {

			// Load injectors for declared fields
			NEXT_FIELD: for (Field field : clazz.getDeclaredFields()) {

				// Determine if dependency
				RequiredDependency requiredDependency = null;
				FOUND_DEPENDENCY: for (FieldDependencyExtractor extractor : this.fieldDependencyExtractors) {
					requiredDependency = extractor.extractRequiredDependency(field);
					if (requiredDependency != null) {
						break FOUND_DEPENDENCY;
					}
				}
				if (requiredDependency == null) {
					continue NEXT_FIELD; // not dependency
				}

				// Obtain the inject dependency
				InjectDependency injectDependency = this.getInjectDependency(requiredDependency.getQualifier(),
						requiredDependency.getType(), supplierContext);

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
	 * Obtains the {@link InjectDependency}.
	 * 
	 * @param qualifier       Qualifier. May be <code>null</code>.
	 * @param type            Type.
	 * @param supplierContext {@link SupplierSourceContext}.
	 * @return {@link InjectDependency}.
	 */
	private InjectDependency getInjectDependency(String qualifier, Class<?> type,
			SupplierSourceContext supplierContext) {

		// Obtain the class loader
		ClassLoader classLoader = supplierContext.getClassLoader();

		// Obtain the supplier dependency
		InjectDependency injectDependency = null;
		SEARCH_DEPENDENCY: for (InjectDependency check : this.dependencies) {

			// Determine if match
			if (!type.equals(check.type)) {
				continue SEARCH_DEPENDENCY; // not type, so go to next
			}

			// Determine if qualifier match
			if ((qualifier != null) && (check.qualifier != null) && (qualifier.equals(check.qualifier))) {
				injectDependency = check;
				break SEARCH_DEPENDENCY; // found dependency
			}

			// Determine if no qualifier
			if ((qualifier == null) && (check.qualifier == null)) {
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
			if (type.isInterface()) {

				// Create proxy for dependency
				dependency = Proxy.newProxyInstance(classLoader, new Class<?>[] { type }, (proxy, method, args) -> {

					// Obtain the dependency
					Object service = InjectContext.getActiveDependency(dependencyIndex);

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
				Objenesis objensis = new ObjenesisStd();
				Factory dependencyFactory = (Factory) objensis.getInstantiatorOf(dependencyClass).newInstance();

				// Register call back handling
				InvocationHandler handler = (obj, method, args) -> {

					// Obtain the dependency
					Object service = InjectContext.getActiveDependency(dependencyIndex);

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

			// Create the supplier thread local
			SupplierThreadLocal<?> threadLocal = supplierContext.addSupplierThreadLocal(qualifier, type);

			// Create and add the dependency
			injectDependency = new InjectDependency(qualifier, type, threadLocal, dependency);
			this.dependencies.add(injectDependency);
		}

		// Return the dependency
		return injectDependency;
	}

	/**
	 * Iterate over all dependencies.
	 * 
	 * @param visitor Visitor for all dependencies.
	 */
	public void forEachDependency(BiConsumer<String, Class<?>> visitor) {
		for (InjectDependency dependency : this.dependencies) {
			visitor.accept(dependency.qualifier, dependency.type);
		}
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
