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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.ManagedObjectSourceService;
import net.officefloor.compile.ManagedObjectSourceServiceFactory;
import net.officefloor.compile.impl.util.CompileUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogatorServiceFactory;
import net.officefloor.plugin.clazz.state.StatePoint;
import net.officefloor.plugin.managedobject.clazz.injection.DependencyClassConstructorInterrogator;

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
		ClassConstructorInterrogatorContextImpl constructorContext = new ClassConstructorInterrogatorContextImpl(
				objectClass);
		CONSTRUCTOR_FOUND: for (ClassConstructorInterrogator interrogator : mosContext.loadServices(
				ClassConstructorInterrogatorServiceFactory.class, new DependencyClassConstructorInterrogator())) {

			// Obtain the constructor
			this.objectConstructor = interrogator.interrogate(constructorContext);
			if (this.objectConstructor != null) {
				break CONSTRUCTOR_FOUND;
			}
		}
		if (this.objectConstructor == null) {

			// Must find constructor
			String errorMessage = constructorContext.errorInformation;
			if (CompileUtil.isBlank(errorMessage)) {
				errorMessage = "Unable to find suitable constructor for " + objectClass.getName();
			}
			throw new IllegalStateException(errorMessage);
		}

		// Create the dependency context
		ClassDependencyManufacturerContextImpl dependencyContext = new ClassDependencyManufacturerContextImpl(
				mosContext.getName(), mosContext);

		// Obtain the constructor dependency factories
		int constructorParameterCount = this.objectConstructor.getParameterCount();
		this.constructorDependencyFactories = new ClassDependencyFactory[constructorParameterCount];
		for (int i = 0; i < constructorParameterCount; i++) {

			// Determine the qualifier
			String qualifier = TypeQualifierInterrogatorServiceFactory
					.extractTypeQualifier(StatePoint.of(this.objectConstructor, i), mosContext);

			// Obtain the parameter factories to construct object
			this.constructorDependencyFactories[i] = dependencyContext
					.createClassDependencyFactory(this.objectConstructor, i, qualifier);
		}

		// Interrogate dependency injection fields and methods
		ClassInjectionInterrogatorContextImpl interrogatorContext = new ClassInjectionInterrogatorContextImpl(
				objectClass);
		interrogatorContext.loadFields(mosContext);
		interrogatorContext.loadMethods(mosContext);

		// Listing of injectors
		List<ClassDependencyInjector> injectors = new LinkedList<>();

		// Load the fields
		for (Field field : interrogatorContext.fields.keySet()) {

			// Determine the qualifier
			String qualifier = TypeQualifierInterrogatorServiceFactory.extractTypeQualifier(StatePoint.of(field),
					mosContext);

			// Create the dependency factory
			ClassDependencyFactory factory = dependencyContext.createClassDependencyFactory(field, qualifier);

			// Add the field injector
			injectors.add(new FieldClassDependencyInjector(field, factory));
		}

		// Load the methods
		for (Method method : interrogatorContext.methods.keySet()) {

			// Obtain the method dependency factories
			int methodParameterCount = method.getParameterCount();
			ClassDependencyFactory[] parameterFactories = new ClassDependencyFactory[methodParameterCount];
			for (int i = 0; i < methodParameterCount; i++) {

				// Determine the qualifier
				String qualifier = TypeQualifierInterrogatorServiceFactory
						.extractTypeQualifier(StatePoint.of(method, i), mosContext);

				// Obtain the parameter factory to invoke method
				parameterFactories[i] = dependencyContext.createClassDependencyFactory(method, i, qualifier);
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

		// TODO load the execute context into dependency factories
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new ClassManagedObject(this.objectConstructor, this.constructorDependencyFactories,
				this.dependencyInjectors);
	}

	/**
	 * {@link ClassConstructorInterrogatorContext} implementation.
	 */
	private static class ClassConstructorInterrogatorContextImpl implements ClassConstructorInterrogatorContext {

		/**
		 * Object {@link Class}
		 */
		private final Class<?> objectClass;

		/**
		 * Error information.
		 */
		private String errorInformation;

		/**
		 * Instantiate.
		 * 
		 * @param objectClass Object {@link Class}.
		 */
		private ClassConstructorInterrogatorContextImpl(Class<?> objectClass) {
			this.objectClass = objectClass;
		}

		/*
		 * ==================== ClassConstructorInterrogatorContext ====================
		 */

		@Override
		public Class<?> getObjectClass() {
			return this.objectClass;
		}

		@Override
		public void setErrorInformation(String errorInformation) {
			this.errorInformation = errorInformation;
		}
	}

	/**
	 * {@link ClassDependencyManufacturerContext} implementation.
	 */
	private static class ClassDependencyManufacturerContextImpl implements ClassDependencyManufacturerContext {

		/**
		 * {@link ManagedObject} name.
		 */
		private final String objectName;

		/**
		 * {@link SourceContext}.
		 */
		private final SourceContext sourceContext;

		/**
		 * {@link StatePoint}.
		 */
		private StatePoint statePoint = null;

		/**
		 * Dependency {@link Class}.
		 */
		private Class<?> dependencyClass = null;

		/**
		 * Dependency {@link Type}.
		 */
		private Type dependencyType = null;

		/**
		 * Possible qualifier.
		 */
		private String qualifier = null;

		/**
		 * {@link Annotation} instances.
		 */
		private Annotation[] annotations = null;

		/**
		 * Instantiate.
		 * 
		 * @param objectName    {@link ManagedObject} name.
		 * @param sourceContext {@link SourceContext}.
		 */
		private ClassDependencyManufacturerContextImpl(String objectName, SourceContext sourceContext) {
			this.objectName = objectName;
			this.sourceContext = sourceContext;
		}

		/**
		 * Creates the {@link ClassDependencyFactory} for a {@link Field}.
		 * 
		 * @param field     {@link Field}.
		 * @param qualifier Qualifier.
		 * @return {@link ClassDependencyFactory}.
		 * @throws Exception If fails to create.
		 */
		private ClassDependencyFactory createClassDependencyFactory(Field field, String qualifier) throws Exception {
			return this.createClassDependencyFactory(StatePoint.of(field), field.getType(), field.getGenericType(),
					qualifier, field.getAnnotations());
		}

		/**
		 * Creates the {@link ClassDependencyFactory} for an {@link Executable}
		 * {@link Parameter}.
		 * 
		 * @param executable     {@link Executable}.
		 * @param parameterIndex Index of the {@link Parameter}.
		 * @param qualifier      Qualifier.
		 * @return {@link ClassDependencyFactory}.
		 * @throws Exception If fails to create.
		 */
		private ClassDependencyFactory createClassDependencyFactory(Executable executable, int parameterIndex,
				String qualifier) throws Exception {
			return this.createClassDependencyFactory(StatePoint.of(executable, parameterIndex),
					executable.getParameterTypes()[parameterIndex],
					executable.getGenericParameterTypes()[parameterIndex], qualifier,
					executable.getParameterAnnotations()[parameterIndex]);
		}

		/**
		 * Creates the {@link ClassDependencyFactory}.
		 * 
		 * @param statePoint      {@link StatePoint}.
		 * @param dependencyClass Dependency {@link Class}.
		 * @param dependencyType  Dependency {@link Type}.
		 * @param qualifier       Qualifier.
		 * @param annoations      {@link Annotation} instances.
		 * @return {@link ClassDependencyFactory}.
		 * @throws Exception If fails to create.
		 */
		private ClassDependencyFactory createClassDependencyFactory(StatePoint statePoint, Class<?> dependencyClass,
				Type dependencyType, String qualifier, Annotation[] annoations) throws Exception {
			this.statePoint = statePoint;
			this.dependencyClass = dependencyClass;
			this.dependencyType = dependencyType;
			this.qualifier = qualifier;
			this.annotations = annoations;

			// Obtain the dependency manufacturer
			for (ClassDependencyManufacturer manufacturer : this.sourceContext
					.loadServices(ClassDependencyManufacturerServiceFactory.class, null)) {
				ClassDependencyFactory factory = manufacturer.createParameterFactory(this);
				if (factory != null) {
					return factory; // found factory for dependency
				}
			}

			// As here no factory for dependency
			return null;
		}

		/*
		 * =================== ClassDependencyManufacturerContext ===================
		 */

		@Override
		public String getObjectName() {
			return this.objectName;
		}

		@Override
		public SourceContext getSourceContext() {
			return this.sourceContext;
		}

		@Override
		public Class<?> getDependencyClass() {
			return this.dependencyClass;
		}

		@Override
		public Type getDependencyType() {
			return this.dependencyType;
		}

		@Override
		public String getDependencyQualifier() {
			return this.qualifier;
		}

		@Override
		public Annotation[] getDependencyAnnotations() {
			return this.annotations;
		}

		@Override
		public Field getField() {
			return this.statePoint.getField();
		}

		@Override
		public Executable getExecutable() {
			return this.statePoint.getExecutable();
		}

		@Override
		public int getExecutableParameterIndex() {
			return this.statePoint.getExecutableParameterIndex();
		}

		@Override
		public ClassDependency addDependency(Class<?> objectType) {
			// TODO implement ClassDependencyManufacturerContext.addDependency
			throw new UnsupportedOperationException("TODO implement ClassDependencyManufacturerContext.addDependency");
		}

		@Override
		public ClassFlow addFlow() {
			// TODO implement ClassDependencyManufacturerContext.addFlow
			throw new UnsupportedOperationException("TODO implement ClassDependencyManufacturerContext.addFlow");
		}
	}

	/**
	 * {@link ClassInjectionInterrogatorContext} implementation.
	 */
	private static class ClassInjectionInterrogatorContextImpl
			implements ClassFieldInjectionInterrogatorContext, ClassMethodInjectionInterrogatorContext {

		/**
		 * Object {@link Class}.
		 */
		private final Class<?> objectClass;

		/**
		 * Dependency injection {@link Field} instances.
		 */
		private final Map<Field, Set<Annotation>> fields = new HashMap<>();

		/**
		 * Dependency injection {@link Method} instances.
		 */
		private final Map<Method, Set<Annotation>> methods = new HashMap<>();

		/**
		 * {@link Field}.
		 */
		private Field field = null;

		/**
		 * {@link Method}.
		 */
		private Method method = null;

		/**
		 * Loads the dependency injection {@link Field} instances.
		 * 
		 * @param sourceContext {@link SourceContext}.
		 * @throws Exception If fails to load {@link Field} instances.
		 */
		public void loadFields(SourceContext sourceContext) throws Exception {

			// Reset
			this.method = null;

			// Interrogate the fields
			for (ClassFieldInjectionInterrogator interrogator : sourceContext
					.loadServices(ClassFieldInjectionInterrogatorServiceFactory.class, null)) {
				Class<?> clazz = this.objectClass;
				while (clazz != null) {
					for (Field field : clazz.getDeclaredFields()) {
						this.field = field;
						interrogator.interrogate(this);
					}
					clazz = clazz.getSuperclass();
				}
			}
		}

		/**
		 * Loads the dependency injection {@link Method} instances.
		 * 
		 * @param sourceContext {@link SourceContext}.
		 * @throws Exception If fails to load {@link Method} instances.
		 */
		public void loadMethods(SourceContext sourceContext) throws Exception {

			// Reset
			this.field = null;

			// Interrogate the methods
			for (ClassMethodInjectionInterrogator interrogator : sourceContext
					.loadServices(ClassMethodInjectionInterrogatorServiceFactory.class, null)) {
				Class<?> clazz = this.objectClass;
				while (clazz != null) {
					for (Method method : clazz.getDeclaredMethods()) {
						this.method = method;
						interrogator.interrogate(this);
					}
					clazz = clazz.getSuperclass();
				}
			}
		}

		/**
		 * Instantiate.
		 * 
		 * @param objectClass Object {@link Class}.
		 */
		private ClassInjectionInterrogatorContextImpl(Class<?> objectClass) {
			this.objectClass = objectClass;
		}

		/*
		 * ===================== ClassInjectionInterrogatorContext ===================
		 */

		@Override
		public Class<?> getObjectClass() {
			return this.objectClass;
		}

		@Override
		public void registerInjectionPoint(Field field, Annotation... additionalAnnotations) {
			Set<Annotation> annotations = this.fields.get(field);
			if (annotations == null) {
				annotations = new HashSet<>();
				this.fields.put(field, annotations);
			}
			annotations.addAll(Arrays.asList(additionalAnnotations));
		}

		@Override
		public void registerInjectionPoint(Method method, Annotation... additionalAnnotations) {
			Set<Annotation> annotations = this.methods.get(method);
			if (annotations == null) {
				annotations = new HashSet<>();
				this.methods.put(method, annotations);
			}
			annotations.addAll(Arrays.asList(additionalAnnotations));
		}

		/*
		 * =================== ClassFieldInjectionInterrogatorContext =================
		 */

		@Override
		public Field getField() {
			return this.field;
		}

		/*
		 * ================== ClassMethodInjectionInterrogatorContext =================
		 */

		@Override
		public Method getMethod() {
			return this.method;
		}
	}

}