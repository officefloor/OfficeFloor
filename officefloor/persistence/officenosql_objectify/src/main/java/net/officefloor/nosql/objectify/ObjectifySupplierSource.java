/*-
 * #%L
 * Objectify Persistence
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

package net.officefloor.nosql.objectify;

import java.lang.reflect.Constructor;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.util.Closeable;

import net.officefloor.compile.classes.OfficeFloorJavaCompiler;
import net.officefloor.compile.classes.OfficeFloorJavaCompiler.JavaSource;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link SupplierSource} to provide {@link Objectify}.
 * 
 * @author Daniel Sagenschneider
 */
public class ObjectifySupplierSource extends AbstractSupplierSource {

	/**
	 * <p>
	 * {@link Property} name for the comma separate list of
	 * {@link ObjectifyEntityLocator} {@link Class} names.
	 * <p>
	 * {@link ObjectifyEntityLocator} instances configured are instantiated by
	 * default constructors.
	 */
	public static final String PROPERTY_ENTITY_LOCATORS = "objectify.entity.locators";

	/**
	 * Default {@link ObjectifyFactoryManufacturer}.
	 */
	private static final ObjectifyFactoryManufacturer DEFAULT_MANUFACTURER = () -> new ObjectifyFactory();

	/**
	 * {@link ObjectifyFactoryManufacturer}.
	 */
	private static ObjectifyFactoryManufacturer objectifyFactoryManufacturer = DEFAULT_MANUFACTURER;

	/**
	 * Creates the {@link ObjectifyFactory}.
	 */
	@FunctionalInterface
	public static interface ObjectifyFactoryManufacturer {

		/**
		 * Creates the {@link ObjectifyFactory}.
		 * 
		 * @return {@link ObjectifyFactory}.
		 */
		ObjectifyFactory createObjectifyFactory();
	}

	/**
	 * Specifies the {@link ObjectifyFactoryManufacturer}.
	 * 
	 * @param manufacturer {@link ObjectifyFactoryManufacturer}.
	 */
	public static void setObjectifyFactoryManufacturer(ObjectifyFactoryManufacturer manufacturer) {
		objectifyFactoryManufacturer = (manufacturer != null) ? manufacturer : DEFAULT_MANUFACTURER;
	}

	/**
	 * Factory for wrapping {@link Objectify}.
	 */
	@FunctionalInterface
	private static interface ObjectifyWrapperFactory {

		/**
		 * Creates the {@link Objectify}.
		 * 
		 * @return {@link Objectify}.
		 * @throws Exception If fails to create the {@link Objectify}.
		 */
		Objectify createObjectify() throws Exception;
	}

	/*
	 * ======================= SupplierSource =============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	public void supply(SupplierSourceContext context) throws Exception {

		// Load the entity types
		Set<Class<?>> entityTypes = new HashSet<>();

		// Load from property configurations
		String propertyEntityLocators = context.getProperty(PROPERTY_ENTITY_LOCATORS, null);
		if (propertyEntityLocators != null) {
			NEXT_LOCATOR: for (String entityLocatorClassName : propertyEntityLocators.split(",")) {

				// Ignore if no class name
				if (CompileUtil.isBlank(entityLocatorClassName)) {
					continue NEXT_LOCATOR;
				}

				// Include the entity class
				Class<?> entityLocatorClass = context.loadClass(entityLocatorClassName.trim());

				// Determine if entity locator
				if (ObjectifyEntityLocator.class.isAssignableFrom(entityLocatorClass)) {

					// Load the located entities
					ObjectifyEntityLocator locator = (ObjectifyEntityLocator) entityLocatorClass.getConstructor()
							.newInstance();
					for (Class<?> entity : locator.locateEntities()) {
						entityTypes.add(entity);
					}

				} else {
					// Not locator, so assume the entity
					entityTypes.add(entityLocatorClass);
				}
			}
		}

		// Load from services
		for (ObjectifyEntityLocator locator : context
				.loadOptionalServices(ObjectifyEntityLocatorServiceFactory.class)) {
			for (Class<?> entity : locator.locateEntities()) {
				entityTypes.add(entity);
			}
		}

		// Capture the entity types
		Class<?>[] entities = entityTypes.toArray(new Class[entityTypes.size()]);

		// Obtain the objectify wrapper factory
		ObjectifyWrapperFactory objectifyWrapperFactory;
		ClassLoader classLoader = context.getClassLoader();
		OfficeFloorJavaCompiler compiler = OfficeFloorJavaCompiler.newInstance(context);
		if (compiler == null) {

			// No compiler, fall back to proxy
			Class<?>[] interfaces = new Class[] { Objectify.class };
			objectifyWrapperFactory = () -> {
				return (Objectify) Proxy.newProxyInstance(classLoader, interfaces, (proxy, method, arguments) -> {

					// Obtain the appropriate objectify
					Objectify objectify = ObjectifyService.ofy();

					// Invoke the method and return the value
					return objectify.getClass().getMethod(method.getName(), method.getParameterTypes())
							.invoke(objectify, arguments);
				});
			};

		} else {

			// Use compiled wrapper
			JavaSource javaSource = compiler.addWrapper(new Class[] { Objectify.class }, Objectify.class,
					ObjectifyService.class.getName() + ".ofy()", null, null);
			Constructor<?> constructor = javaSource.compile().getConstructor(Objectify.class);
			objectifyWrapperFactory = () -> (Objectify) constructor.newInstance((Objectify) null);
		}

		// Create the objectify factory
		ObjectifyFactory objectifyFactory = objectifyFactoryManufacturer.createObjectifyFactory();

		// Register the Objectify managed object source
		context.addManagedObjectSource(null, Objectify.class,
				new ObjectifyManagedObjectSource(entities, objectifyFactory, objectifyWrapperFactory));

		// Register the thread synchronising
		context.addThreadSynchroniser(new ObjectifyThreadSynchroniserFactory(objectifyFactory));
	}

	@Override
	public void terminate() {
		// Nothing to terminate
	}

	/**
	 * {@link Objectify} {@link ManagedObjectSource}.
	 * 
	 * @author Daniel Sagenschneider
	 */
	private class ObjectifyManagedObjectSource extends AbstractManagedObjectSource<None, None> {

		/**
		 * {@link Objectify} {@link Entity} types.
		 */
		private final Class<?>[] entityTypes;

		/**
		 * {@link ObjectifyFactory}.
		 */
		private final ObjectifyFactory objectifyFactory;

		/**
		 * {@link ObjectifyWrapperFactory}.
		 */
		private final ObjectifyWrapperFactory objectifyWrapperFactory;

		/**
		 * Instantiate.
		 *
		 * @param entityTypes             {@link Objectify} {@link Entity} types.
		 * @param objectifyFactory        {@link ObjectifyFactory}.
		 * @param objectifyWrapperFactory {@link ObjectifyWrapperFactory}.
		 */
		private ObjectifyManagedObjectSource(Class<?>[] entityTypes, ObjectifyFactory objectifyFactory,
				ObjectifyWrapperFactory objectifyWrapperFactory) {
			this.entityTypes = entityTypes;
			this.objectifyFactory = objectifyFactory;
			this.objectifyWrapperFactory = objectifyWrapperFactory;
		}

		/*
		 * ================= ManagedObjectSource ===================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {

			// Load the meta-data
			context.setObjectClass(Objectify.class);

			// Recycle the Objectify
			context.getManagedObjectSourceContext().getRecycleFunction(() -> (recyleContext) -> {
				RecycleManagedObjectParameter<ObjectifyManagedObject> recycle = RecycleManagedObjectParameter
						.getRecycleManagedObjectParameter(recyleContext);
				recycle.getManagedObject().closable.close();
			}).linkParameter(0, RecycleManagedObjectParameter.class);

		}

		@Override
		public void start(ManagedObjectExecuteContext<None> context) throws Exception {

			// Initialise
			ObjectifyService.init(this.objectifyFactory);

			// Register the entity types
			for (Class<?> entityType : this.entityTypes) {
				ObjectifyService.register(entityType);
			}
		}

		@Override
		protected ManagedObject getManagedObject() throws Throwable {
			Closeable closeable = ObjectifyService.begin();
			Objectify wrappingObjectify = this.objectifyWrapperFactory.createObjectify();
			return new ObjectifyManagedObject(wrappingObjectify, closeable);
		}
	}

	/**
	 * {@link ManagedObject} for {@link Objectify}.
	 */
	private static class ObjectifyManagedObject implements ManagedObject {

		private final Objectify objectify;

		private final Closeable closable;

		private ObjectifyManagedObject(Objectify objectify, Closeable closable) {
			this.objectify = objectify;
			this.closable = closable;
		}

		/*
		 * ================= ManagedObject ======================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this.objectify;
		}
	}

}
