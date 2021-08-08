/*-
 * #%L
 * OfficeCompiler
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

package net.officefloor.plugin.clazz.dependency;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.InvalidConfigurationError;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext.ClassDependency;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext.ClassFlow;
import net.officefloor.plugin.clazz.dependency.impl.ObjectClassDependencyManufacturer;
import net.officefloor.plugin.clazz.state.StatePoint;

/**
 * Manages {@link Class} dependencies.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassDependenciesManager implements ClassDependencies {

	/**
	 * Creates the {@link ClassDependenciesManager} to inject both
	 * {@link ManagedObject} and {@link Flow} instances.
	 * 
	 * @param clazz               {@link Class} being interrogated for injection.
	 * @param sourceContext       {@link SourceContext}.
	 * @param dependenciesContext {@link ClassDependenciesContext}.
	 */
	public static ClassDependenciesManager create(Class<?> clazz, SourceContext sourceContext,
			ClassDependenciesContext dependenciesContext) {
		return new ClassDependenciesManager(clazz, sourceContext, dependenciesContext);
	}

	/**
	 * Creates the {@link ClassDependenciesManager} to inject both
	 * {@link ManagedObject} and {@link Flow} instances.
	 * 
	 * @param clazz         {@link Class} being interrogated for injection.
	 * @param sourceContext {@link SourceContext}.
	 * @param flowsContext  {@link ClassDependenciesFlowContext}.
	 */
	public static ClassDependenciesManager createNoObjects(Class<?> clazz, SourceContext sourceContext,
			ClassDependenciesFlowContext flowsContext) {
		return new ClassDependenciesManager(clazz, sourceContext, new ClassDependenciesContext() {

			@Override
			public ClassItemIndex addFlow(String flowName, Class<?> argumentType, Object[] annotations) {
				return flowsContext.addFlow(flowName, argumentType, annotations);
			}

			@Override
			public void addEscalation(Class<? extends Throwable> escalationType) {
				flowsContext.addEscalation(escalationType);
			}

			@Override
			public void addAnnotation(Object annotation) {
				flowsContext.addAnnotation(annotation);
			}

			@Override
			public ClassItemIndex addDependency(String dependencyName, String qualifier, Class<?> objectType,
					Object[] annotations) {

				// Avoid object dependencies (should be caught internally and ignored)
				throw new ObjectDependenciesNotAvailable();
			}
		});
	}

	/**
	 * Creates a {@link ClassItemIndex}.
	 * 
	 * @param index           Index of item.
	 * @param annotationAdder Adds additional annotations. May be <code>null</code>.
	 * @return Created {@link ClassItemIndex}.
	 */
	public static ClassItemIndex createClassItemIndex(int index, Consumer<Object> annotationAdder) {
		return new ClassItemIndex() {

			@Override
			public int getIndex() {
				return index;
			}

			@Override
			public void addAnnotation(Object annotation) {
				if (annotationAdder != null) {
					annotationAdder.accept(annotation);
				}
			}
		};
	}

	/**
	 * Determines if same object type.
	 * 
	 * @param objectTypeA First object type.
	 * @param objectTypeB Second object type.
	 * @return <code>true</code> if same.
	 */
	public static boolean isSameObjectType(Class<?> objectTypeA, Class<?> objectTypeB) {
		return objectTypeA.equals(objectTypeB);
	}

	/**
	 * Determines if same argument type.
	 * 
	 * @param argumentTypeA First argument type.
	 * @param argumentTypeB Second argument type.
	 * @return <code>true</code> if same (or no argument).
	 */
	public static boolean isSameArgumentType(Class<?> argumentTypeA, Class<?> argumentTypeB) {
		return ((argumentTypeA != null) && (isSameObjectType(argumentTypeA, argumentTypeB)))
				|| ((argumentTypeA == null) && (argumentTypeB == null));
	}

	/**
	 * Determines if same qualifier.
	 * 
	 * @param qualifierA First qualifier.
	 * @param qualifierB Second qualifier.
	 * @return <code>true</code> if same qualifier (or no qualification).
	 */
	public static boolean isSameQualifier(String qualifierA, String qualifierB) {
		return ((qualifierA != null) && (qualifierA.equals(qualifierB)))
				|| ((qualifierA == null) && (qualifierB == null));
	}

	/**
	 * Obtains the name of the dependency.
	 * 
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Dependency type.
	 * @return Name for the dependency.
	 */
	public static String getDependencyName(String qualifier, Class<?> objectType) {
		return (qualifier != null ? qualifier + "-" : "") + objectType.getName();
	}

	/**
	 * Object {@link ClassDependencyManufacturer}.
	 */
	private static final ClassDependencyManufacturer objectDependencyManufacturer = new ObjectClassDependencyManufacturer();

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext;

	/**
	 * {@link ClassDependenciesContext}.
	 */
	private final ClassDependenciesContext dependenciesContext;

	/**
	 * Created {@link ClassDependencyFactory} instances.
	 */
	private final List<ClassDependencyFactory> createdFactories = new LinkedList<>();

	/**
	 * {@link ClassDependency} instances that are indexed.
	 */
	private final List<ClassDependencyImpl> indexedDependencies = new LinkedList<>();

	/**
	 * {@link ClassFlow} instances that are indexed.
	 */
	private final List<ClassFlowImpl> indexedFlows = new LinkedList<>();

	/**
	 * Registered {@link Escalation} type instances.
	 */
	private final Set<Class<? extends Throwable>> registeredEscalations = new HashSet<>();

	/**
	 * Additional {@link ClassDependencyManufacturer} instances.
	 */
	private final List<ClassDependencyManufacturer> additionalManufacturers = new LinkedList<>();

	/**
	 * Flags whether using the {@link ClassDependencyFactory}.
	 */
	private boolean isUseFactory = true;

	/**
	 * Instantiate.
	 * 
	 * @param clazz               {@link Class} being interrogated for injection.
	 * @param sourceContext       {@link SourceContext}.
	 * @param dependenciesContext {@link ClassDependenciesContext}.
	 */
	private ClassDependenciesManager(Class<?> clazz, SourceContext sourceContext,
			ClassDependenciesContext dependenciesContext) {
		this.sourceContext = sourceContext;
		this.dependenciesContext = dependenciesContext;

		// Add class annotations
		for (Annotation annotation : clazz.getAnnotations()) {
			this.dependenciesContext.addAnnotation(annotation);
		}
	}

	/**
	 * Adds an additional {@link ClassDependencyManufacturer}.
	 * 
	 * @param manufacturer Additional {@link ClassDependencyManufacturer}.
	 */
	public void addClassDependencyManufacturer(ClassDependencyManufacturer manufacturer) {
		this.additionalManufacturers.add(manufacturer);
	}

	/**
	 * Allow registering an additional {@link Escalation}.
	 * 
	 * @param <E>            {@link Escalation} type.
	 * @param escalationType {@link Escalation} type.
	 */
	public <E extends Throwable> void addEscalation(Class<E> escalationType) {

		// Determine if already registered
		if (ClassDependenciesManager.this.registeredEscalations.contains(escalationType)) {
			return; // already registered
		}

		// Add escalation and register
		ClassDependenciesManager.this.dependenciesContext.addEscalation(escalationType);
		ClassDependenciesManager.this.registeredEscalations.add(escalationType);
	}

	/**
	 * Loads the {@link ManagedObjectExecuteContext} to the created
	 * {@link ClassDependencyFactory} instances.
	 * 
	 * @param executeContext {@link ManagedObjectExecuteContext}.
	 */
	public void loadManagedObjectExecuteContext(ManagedObjectExecuteContext<Indexed> executeContext) {
		for (ClassDependencyFactory factory : this.createdFactories) {
			factory.loadManagedObjectExecuteContext(executeContext);
		}
	}

	/*
	 * ======================= ClassDependencies =========================
	 */

	@Override
	public ClassDependencyFactory createClassDependencyFactory(Field field, String qualifier) throws Exception {

		// Create the listing of annotations
		List<Annotation> annotations = new LinkedList<>();
		for (Annotation annotation : field.getAnnotations()) {
			annotations.add(annotation);
		}
		for (Annotation annotation : field.getType().getAnnotations()) {
			annotations.add(annotation);
		}

		// Create and return dependency factory
		return this.createClassDependencyFactory(StatePoint.of(field), field.getType(), field.getGenericType(),
				qualifier, annotations);
	}

	@Override
	public ClassDependencyFactory createClassDependencyFactory(Executable executable, int parameterIndex,
			String qualifier) throws Exception {

		// Create the listing of annotations
		List<Annotation> annotations = new LinkedList<>();
		for (Annotation annotation : executable.getParameterAnnotations()[parameterIndex]) {
			annotations.add(annotation);
		}
		for (Annotation annotation : executable.getParameterTypes()[parameterIndex].getAnnotations()) {
			annotations.add(annotation);
		}

		// Create and return dependency factory
		return this.createClassDependencyFactory(StatePoint.of(executable, parameterIndex),
				executable.getParameterTypes()[parameterIndex], executable.getGenericParameterTypes()[parameterIndex],
				qualifier, annotations);
	}

	@Override
	public ClassDependencyFactory createClassDependencyFactory(String dependencyName, Class<?> dependencyType,
			String qualifier) throws Exception {

		// Create the context
		ClassDependencyManufacturerContext context = new ClassDependencyManufacturerContextImpl(dependencyName,
				dependencyType, dependencyType, qualifier, Collections.emptyList());

		// Return the object dependency
		return objectDependencyManufacturer.createParameterFactory(context);
	}

	/**
	 * Creates the {@link ClassDependencyFactory}.
	 * 
	 * @param statePoint      {@link StatePoint}.
	 * @param dependencyClass Dependency {@link Class}.
	 * @param dependencyType  Dependency {@link Type}.
	 * @param qualifier       Qualifier.
	 * @param annotations     {@link Annotation} instances.
	 * @return {@link ClassDependencyFactory}.
	 * @throws Exception If fails to create.
	 */
	private ClassDependencyFactory createClassDependencyFactory(StatePoint statePoint, Class<?> dependencyClass,
			Type dependencyType, String qualifier, List<Annotation> annotations) throws Exception {

		// Handle primitives
		if (boolean.class.equals(dependencyClass)) {
			dependencyClass = Boolean.class;
		} else if (byte.class.equals(dependencyClass)) {
			dependencyClass = Byte.class;
		} else if (short.class.equals(dependencyClass)) {
			dependencyClass = Short.class;
		} else if (char.class.equals(dependencyClass)) {
			dependencyClass = Character.class;
		} else if (int.class.equals(dependencyClass)) {
			dependencyClass = Integer.class;
		} else if (long.class.equals(dependencyClass)) {
			dependencyClass = Long.class;
		} else if (float.class.equals(dependencyClass)) {
			dependencyClass = Float.class;
		} else if (double.class.equals(dependencyClass)) {
			dependencyClass = Double.class;
		}

		// Create the context
		ClassDependencyManufacturerContext context = new ClassDependencyManufacturerContextImpl(statePoint,
				dependencyClass, dependencyType, qualifier, annotations);

		// Create loader of dependency
		this.isUseFactory = true; // reset to use
		ClassDependencyFactory[] useFactory = new ClassDependencyFactory[] { null };
		ClassDependencyManufacturer[] useManufacturer = new ClassDependencyManufacturer[] { null };
		DependencyLoader loader = (manufacturer) -> {

			// Attempt to create the factory
			ClassDependencyFactory factory = null;
			try {
				factory = manufacturer.createParameterFactory(context);
			} catch (ObjectDependenciesNotAvailable ignore) {
				// Object not supported
			}
			if (factory != null) {

				// Determine if have factory to use
				if (this.isUseFactory) {
					// Use and register the factory
					useFactory[0] = factory;
					useManufacturer[0] = manufacturer;
					this.createdFactories.add(factory);

				} else {
					// Log that another factory potentially could be used
					this.sourceContext.getLogger()
							.info(statePoint.toLocation() + " ignoring " + manufacturer.getClass().getName()
									+ " as provided by " + useManufacturer.getClass().getSimpleName()
									+ " earlier in services listing");
				}
			}
		};

		// Iterates through all manufacturers to ensure all annotations added
		for (ClassDependencyManufacturer manufacturer : this.additionalManufacturers) {
			loader.loadDependency(manufacturer);
		}
		for (ClassDependencyManufacturer manufacturer : this.sourceContext
				.loadServices(ClassDependencyManufacturerServiceFactory.class, null)) {
			loader.loadDependency(manufacturer);
		}
		if (useFactory[0] != null) {
			return useFactory[0]; // use factory
		}

		// As here, assume just a plain dependency
		return objectDependencyManufacturer.createParameterFactory(context);
	}

	/**
	 * Loads the dependency.
	 */
	@FunctionalInterface
	private static interface DependencyLoader {

		/**
		 * Attempts to load dependency with {@link ClassDependencyManufacturer}.
		 * 
		 * @param manufacturer {@link ClassDependencyManufacturer}.
		 * @throws Exception If fails to load dependency.
		 */
		void loadDependency(ClassDependencyManufacturer manufacturer) throws Exception;
	}

	/**
	 * {@link ClassDependencyManufacturerContext} implementation.
	 */
	private class ClassDependencyManufacturerContextImpl implements ClassDependencyManufacturerContext {

		/**
		 * {@link StatePoint}.
		 */
		private final StatePoint statePoint;

		/**
		 * Name for dependency.
		 */
		private final String dependencyName;

		/**
		 * Dependency {@link Class}.
		 */
		private final Class<?> dependencyClass;

		/**
		 * Dependency {@link Type}.
		 */
		private final Type dependencyType;

		/**
		 * Possible qualifier.
		 */
		private final String qualifier;

		/**
		 * {@link Annotation} instances.
		 */
		private final List<Annotation> annotations;

		/**
		 * Instantiate to reflectively inject the dependency.
		 * 
		 * @param statePoint      {@link StatePoint}.
		 * @param dependencyClass Dependency {@link Class}.
		 * @param dependencyType  Dependency {@link Type}.
		 * @param qualifier       Possible qualifier.
		 * @param annotations     {@link Annotation} instances.
		 */
		private ClassDependencyManufacturerContextImpl(StatePoint statePoint, Class<?> dependencyClass,
				Type dependencyType, String qualifier, List<Annotation> annotations) {
			this.statePoint = statePoint;
			this.dependencyName = null;
			this.dependencyClass = dependencyClass;
			this.dependencyType = dependencyType;
			this.qualifier = qualifier;
			this.annotations = annotations;
		}

		/**
		 * Instantiate to create particular dependency.
		 * 
		 * @param dependencyName  Name of dependency.
		 * @param dependencyClass Dependency {@link Class}.
		 * @param dependencyType  Dependency {@link Type}.
		 * @param qualifier       Possible qualifier.
		 * @param annotations     {@link Annotation} instances.
		 */
		private ClassDependencyManufacturerContextImpl(String dependencyName, Class<?> dependencyClass,
				Type dependencyType, String qualifier, List<Annotation> annotations) {
			this.statePoint = null;
			this.dependencyName = dependencyName;
			this.dependencyClass = dependencyClass;
			this.dependencyType = dependencyType;
			this.qualifier = qualifier;
			this.annotations = annotations;
		}

		/*
		 * =================== ClassDependencyManufacturerContext ===================
		 */

		@Override
		public String getName() {
			return ClassDependenciesManager.this.sourceContext.getName();
		}

		@Override
		public Logger getLogger() {
			return ClassDependenciesManager.this.sourceContext.getLogger();
		}

		@Override
		public SourceContext getSourceContext() {
			return ClassDependenciesManager.this.sourceContext;
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
			return this.annotations.toArray(new Annotation[this.annotations.size()]);
		}

		@Override
		@SuppressWarnings("unchecked")
		public <A extends Annotation> A getDependencyAnnotation(Class<? extends A> annotationType) {

			// Obtain the annotations
			Annotation[] annotations = this.getDependencyAnnotations();
			if (annotations.length == 0) {
				return null; // no annotation
			}

			// First pass to find by exact type
			for (Annotation annotation : annotations) {
				if (annotation.annotationType().equals(annotationType)) {
					return (A) annotation;
				}
			}

			// Second pass to find by sub type
			for (Annotation annotation : annotations) {
				if (annotationType.isAssignableFrom(annotation.annotationType())) {
					return (A) annotation;
				}
			}

			// As here, no match
			return null;
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
		public ClassDependency newDependency(Class<?> objectType) {
			return new ClassDependencyImpl(this.dependencyName, objectType, this.annotations);
		}

		@Override
		public ClassFlow newFlow(String name) {
			return new ClassFlowImpl(name, this.annotations);
		}

		@Override
		public <E extends Throwable> void addEscalation(Class<E> escalationType) {

			// Do not add if not using factory
			if (!ClassDependenciesManager.this.isUseFactory) {
				return; // not using
			}

			// Add the escalation
			ClassDependenciesManager.this.addEscalation(escalationType);
		}

		@Override
		public void addAnnotation(Object annotation) {

			// Always allow annotations to be added
			ClassDependenciesManager.this.dependenciesContext.addAnnotation(annotation);
		}
	}

	/**
	 * {@link ClassDependency} implementation.
	 */
	private class ClassDependencyImpl implements ClassDependency {

		/**
		 * Name for the dependency.
		 */
		private final String dependencyName;

		/**
		 * Object {@link Class}.
		 */
		private final Class<?> objectType;

		/**
		 * Listing of annotations.
		 */
		private final List<Object> annotations = new LinkedList<>();

		/**
		 * Qualifier.
		 */
		private String qualifier = null;

		/**
		 * {@link ClassItemIndex} of this dependency.
		 */
		private ClassItemIndex index = null;

		/**
		 * Instantiate.
		 * 
		 * @param dependencyName     Name of the dependency.
		 * @param objectType         Object {@link Class}.
		 * @param defaultAnnotations Default listing of {@link Annotation} instances for
		 *                           the dependency.
		 */
		private ClassDependencyImpl(String dependencyName, Class<?> objectType, List<Annotation> defaultAnnotations) {
			this.dependencyName = dependencyName;
			this.objectType = objectType;
			this.annotations.addAll(defaultAnnotations);
		}

		/**
		 * Ensures still able to edit dependency.
		 * 
		 * @throws IllegalStateException If locked down.
		 */
		private void ensureNotIndexed() throws IllegalStateException {
			if (this.index != null) {
				throw new IllegalStateException(
						"Can not alter " + ClassDependency.class.getSimpleName() + " once obtained index");
			}
		}

		/*
		 * ================= ClassDepenency ================
		 */

		@Override
		public ClassDependency setQualifier(String qualifier) {
			this.ensureNotIndexed();
			this.qualifier = qualifier;
			return this;
		}

		@Override
		public ClassDependency addAnnotation(Object annotation) {
			this.ensureNotIndexed();
			this.annotations.add(annotation);
			return this;
		}

		@Override
		public ClassDependency addAnnotations(Collection<? extends Object> annotations) {
			this.ensureNotIndexed();
			this.annotations.addAll(annotations);
			return this;
		}

		@Override
		public ClassItemIndex build() {

			// Easy access to dependencies
			ClassDependenciesManager dependencies = ClassDependenciesManager.this;

			// Do not add if not using factory
			if (!dependencies.isUseFactory) {
				return createClassItemIndex(-1, null); // indicate not using
			}

			// Find matching dependency
			for (ClassDependencyImpl existing : dependencies.indexedDependencies) {

				// Ensure same qualifier and type
				if (isSameObjectType(this.objectType, existing.objectType)
						&& isSameQualifier(this.qualifier, existing.qualifier)) {

					// Same dependency (add additional annotations)
					this.index = existing.index;
					for (Object annotation : this.annotations) {
						this.index.addAnnotation(annotation);
					}
					return this.index;
				}
			}

			// Obtain the dependency name
			String dependencyName = this.dependencyName != null ? this.dependencyName
					: getDependencyName(qualifier, objectType);

			// As here, new dependency
			Object[] annotations = this.annotations.toArray(new Object[this.annotations.size()]);
			this.index = dependencies.dependenciesContext.addDependency(dependencyName, this.qualifier, this.objectType,
					annotations);
			dependencies.indexedDependencies.add(this);
			return index;
		}
	}

	/**
	 * {@link ClassFlow} implementation.
	 */
	private class ClassFlowImpl implements ClassFlow {

		/**
		 * Name of {@link Flow}.
		 */
		private final String name;

		/**
		 * Argument {@link Class}.
		 */
		private Class<?> argumentType;

		/**
		 * Listing of annotations.
		 */
		private final List<Object> annotations = new LinkedList<>();

		/**
		 * {@link ClassItemIndex} of this dependency.
		 */
		private ClassItemIndex index = null;

		/**
		 * Instantiate.
		 * 
		 * @param name               Name of {@link Flow}.
		 * @param defaultAnnotations Default listing of {@link Annotation} instances for
		 *                           the dependency.
		 */
		private ClassFlowImpl(String name, List<Annotation> defaultAnnotations) {
			this.name = name;
			this.annotations.addAll(defaultAnnotations);
		}

		/**
		 * Ensures still able to edit dependency.
		 * 
		 * @throws IllegalStateException If locked down.
		 */
		private void ensureNotIndexed() throws IllegalStateException {
			if (this.index != null) {
				throw new IllegalStateException(
						"Can not alter " + ClassDependency.class.getSimpleName() + " once obtained index");
			}
		}

		/*
		 * ================= ClassFlow ================
		 */

		@Override
		public ClassFlow setArgumentType(Class<?> argumentType) {
			this.ensureNotIndexed();
			this.argumentType = argumentType;
			return this;
		}

		@Override
		public ClassFlow addAnnotation(Object annotation) {
			this.ensureNotIndexed();
			this.annotations.add(annotation);
			return this;
		}

		@Override
		public ClassFlow addAnnotations(Collection<? extends Object> annotations) {
			this.ensureNotIndexed();
			this.annotations.addAll(annotations);
			return this;
		}

		@Override
		public ClassItemIndex build() {

			// Easy access to dependencies
			ClassDependenciesManager dependencies = ClassDependenciesManager.this;

			// Do not add if not using factory
			if (!dependencies.isUseFactory) {
				return createClassItemIndex(-1, null); // indicate not using
			}

			// Find matching flow
			NEXT_FLOW: for (ClassFlowImpl existing : dependencies.indexedFlows) {

				// Determine if existing
				if (!this.name.equals(existing.name)) {
					continue NEXT_FLOW; // not same
				}

				// Ensure same argument type
				if (!isSameArgumentType(this.argumentType, existing.argumentType)) {
					throw new InvalidConfigurationError(
							"Flows by same name " + this.name + " have different argument types (" + ")");
				}

				// Same flow (add additional annotations)
				this.index = existing.index;
				for (Object annotation : this.annotations) {
					this.index.addAnnotation(annotation);
				}
				return this.index;
			}

			// As here, new flow
			Object[] annotations = this.annotations.toArray(new Object[this.annotations.size()]);
			this.index = dependencies.dependenciesContext.addFlow(this.name, this.argumentType, annotations);
			dependencies.indexedFlows.add(this);
			return this.index;
		}
	}

	/**
	 * Flags that dependency object not available.
	 */
	private static class ObjectDependenciesNotAvailable extends Error {
		private static final long serialVersionUID = 1L;
	}
}
