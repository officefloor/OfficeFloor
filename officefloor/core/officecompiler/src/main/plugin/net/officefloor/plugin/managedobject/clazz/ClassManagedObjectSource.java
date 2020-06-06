/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.managedobject.clazz;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.officefloor.compile.ManagedObjectSourceService;
import net.officefloor.compile.ManagedObjectSourceServiceFactory;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.plugin.clazz.Dependency;
import net.officefloor.plugin.clazz.constructor.ClassConstructorInterrogatorServiceFactory;
import net.officefloor.plugin.clazz.dependency.ClassDependencies;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.interrogate.ClassInjections;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogation;
import net.officefloor.plugin.clazz.state.StatePoint;

/**
 * {@link ManagedObjectSource} that manages an {@link Object} via reflection.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassManagedObjectSource extends AbstractManagedObjectSource<Indexed, Indexed> implements
		ManagedObjectSourceService<Indexed, Indexed, ClassManagedObjectSource>, ManagedObjectSourceServiceFactory {

	/**
	 * Convenience method to aid in unit testing.
	 * 
	 * @param <T>                              {@link Class} type.
	 * @param clazz                            {@link Class} to instantiate and have
	 *                                         dependencies injected.
	 * @param constructorDependencies          {@link Constructor dependencies.
	 * @param fieldDependencyNameObjectListing Listing of dependency name and
	 *                                         dependency object pairs to be
	 *                                         injected.
	 * @return Instance of the {@link Class} with the dependencies injected.
	 * @throws Exception If fails to instantiate the instance and inject the
	 *                   dependencies.
	 */
	public static <T> T newInstance(Class<T> clazz, Object[] constructorDependencies,
			Object... fieldDependencyNameObjectListing) throws Exception {

		// Create the map of dependencies
		Map<String, Object> dependencies = new HashMap<String, Object>();
		for (int i = 0; i < fieldDependencyNameObjectListing.length; i += 2) {
			String name = fieldDependencyNameObjectListing[i].toString();
			Object dependency = fieldDependencyNameObjectListing[i + 1];
			dependencies.put(name, dependency);
		}

		// Return the new instance
		return newInstance(clazz, constructorDependencies, dependencies);
	}

	/**
	 * <p>
	 * Convenience method to aid in unit testing.
	 * <p>
	 * As many {@link Dependency} {@link Field} instances will be
	 * <code>private</code> they are unlikely to be accessible in unit tests unless
	 * a specific constructor is provided. This method enables instantiation and
	 * injecting of dependencies to enable unit testing.
	 * 
	 * @param <T>                     {@link Class} type.
	 * @param clazz                   {@link Class} to instantiate and have
	 *                                dependencies injected.
	 * @param constructorDependencies {@link Constructor dependencies.
	 * @param fieldDependencies       Map of dependencies by the dependency name.
	 *                                The dependency name is the {@link Dependency}
	 *                                {@link Field} name. Should two {@link Field}
	 *                                instances in the class hierarchy have the same
	 *                                name, the dependency name is qualified with
	 *                                the declaring {@link Class} name.
	 * @return Instance of the {@link Class} with the dependencies injected.
	 * @throws Exception If fails to instantiate the instance and inject the
	 *                   dependencies.
	 */
	public static <T> T newInstance(Class<T> clazz, Object[] constructorDependencies,
			Map<String, Object> fieldDependencies) throws Exception {

//		// Obtain the constructor
//		Constructor<T> constructor = retrieveConstructor(clazz);
//
//		// Instantiate the object
//		T object = constructor.newInstance(constructorDependencies);
//
//		// Obtain the listing of dependency fields
//		List<Field> dependencyFields = retrieveDependencyFields(clazz);
//		orderFields(dependencyFields);
//
//		// Inject the dependencies
//		for (Field dependencyField : dependencyFields) {
//
//			// Obtain the dependency name
//			String dependencyName = retrieveDependencyName(dependencyField, dependencyFields);
//
//			// Obtain the dependency
//			Object dependency = fieldDependencies.get(dependencyName);
//			if (dependency == null) {
//				throw new IllegalStateException("No dependency found for field " + dependencyName);
//			}
//
//			// Inject the dependency
//			dependencyField.setAccessible(true);
//			dependencyField.set(object, dependency);
//		}
//
//		// Obtain the listing of process fields
//		List<ProcessStruct> processStructs = retrieveOrderedProcessStructs(clazz);
//		List<Field> processFields = new ArrayList<Field>(processStructs.size());
//		for (ProcessStruct processStruct : processStructs) {
//			processFields.add(processStruct.field);
//		}
//
//		// Inject the processes
//		for (Field processField : processFields) {
//
//			// Obtain the process name (as dependency inject interface)
//			String dependencyName = retrieveDependencyName(processField, processFields);
//
//			// Obtain the dependency (process interface)
//			Object dependency = fieldDependencies.get(dependencyName);
//
//			// Inject the process interface
//			processField.setAccessible(true);
//			processField.set(object, dependency);
//		}
//
//		// Return the instance with dependencies and process interfaces injected
//		return object;

		return null;
	}

	/**
	 * Property name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/**
	 * {@link Constructor} for the {@link Object} being managed.
	 */
	private Constructor<?> objectConstructor;

	/**
	 * {@link ClassDependencyFactory} instances for the {@link Constructor}
	 * {@link Parameter} instances.
	 */
	private ClassDependencyFactory[] constructorDependencyFactories;

	/**
	 * {@link ClassDependencies}.
	 */
	private ClassDependencies dependencies;

	/**
	 * {@link ClassDependencyInjector} instances.
	 */
	private ClassDependencyInjector[] dependencyInjectors;

	/*
	 * =================== ManagedObjectSourceService ==========================
	 */

	@Override
	public ManagedObjectSourceService<?, ?, ?> createService(ServiceContext context) throws Throwable {
		return this;
	}

	@Override
	public String getManagedObjectSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassManagedObjectSource> getManagedObjectSourceClass() {
		return ClassManagedObjectSource.class;
	}

	/*
	 * ==================== AbstractManagedObjectSource ========================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CLASS_NAME_PROPERTY_NAME, "Class");
	}

	@Override
	protected void loadMetaData(MetaDataContext<Indexed, Indexed> context) throws Exception {
		ManagedObjectSourceContext<Indexed> mosContext = context.getManagedObjectSourceContext();

		// Obtain the class
		String className = mosContext.getProperty(CLASS_NAME_PROPERTY_NAME);
		final Class<?> objectClass = mosContext.getClassLoader().loadClass(className);

		// Provide managed object class to indicate coordinating
		context.setManagedObjectClass(ClassManagedObject.class);

		// Class is the object type returned from the managed object
		context.setObjectClass(objectClass);

		// Obtain the constructor
		this.objectConstructor = ClassConstructorInterrogatorServiceFactory.extractConstructor(objectClass, mosContext);

		// Create the type qualification
		TypeQualifierInterrogation qualifierInterrogation = new TypeQualifierInterrogation(mosContext);

		// Create the dependencies
		this.dependencies = new ClassDependencies(mosContext.getName(), mosContext.getLogger(), mosContext, context);

		// Obtain the constructor dependency factories
		int constructorParameterCount = this.objectConstructor.getParameterCount();
		this.constructorDependencyFactories = new ClassDependencyFactory[constructorParameterCount];
		for (int i = 0; i < constructorParameterCount; i++) {

			// Determine the qualifier
			String qualifier = qualifierInterrogation.extractTypeQualifier(StatePoint.of(this.objectConstructor, i));

			// Obtain the parameter factories to construct object
			this.constructorDependencyFactories[i] = this.dependencies
					.createClassDependencyFactory(this.objectConstructor, i, qualifier);
		}

		// Interrogate dependency injection fields and methods
		ClassInjections injections = new ClassInjections(objectClass, mosContext);

		// Listing of injectors
		List<ClassDependencyInjector> injectors = new LinkedList<>();

		// Load the fields
		for (Field field : injections.getInjectionFields()) {

			// Determine the qualifier
			String qualifier = qualifierInterrogation.extractTypeQualifier(StatePoint.of(field));

			// Create the dependency factory
			ClassDependencyFactory factory = this.dependencies.createClassDependencyFactory(field, qualifier);

			// Add the field injector
			injectors.add(new FieldClassDependencyInjector(field, factory));
		}

		// Load the methods
		for (Method method : injections.getInjectionMethods()) {

			// Obtain the method dependency factories
			int methodParameterCount = method.getParameterCount();
			ClassDependencyFactory[] parameterFactories = new ClassDependencyFactory[methodParameterCount];
			for (int i = 0; i < methodParameterCount; i++) {

				// Determine the qualifier
				String qualifier = qualifierInterrogation.extractTypeQualifier(StatePoint.of(method, i));

				// Obtain the parameter factory to invoke method
				parameterFactories[i] = this.dependencies.createClassDependencyFactory(method, i, qualifier);
			}

			// Add the method injector
			injectors.add(new MethodClassDependencyInjector(method, parameterFactories));
		}

		// Capture the dependency injectors
		this.dependencyInjectors = injectors.toArray(new ClassDependencyInjector[injectors.size()]);

		// Add the object class as extension interface
		ClassExtensionFactory.registerExtension(context, objectClass);
	}

	@Override
	public void start(ManagedObjectExecuteContext<Indexed> context) throws Exception {

		// Load the execute context to dependency factories
		this.dependencies.loadManagedObjectExecuteContext(context);

		// Clear to allow garbage collection
		this.dependencies = null;
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new ClassManagedObject(this.objectConstructor, this.constructorDependencyFactories,
				this.dependencyInjectors);
	}

}