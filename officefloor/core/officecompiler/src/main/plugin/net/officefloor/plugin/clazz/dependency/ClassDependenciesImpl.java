package net.officefloor.plugin.clazz.dependency;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.InvalidConfigurationError;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext.ClassDependency;
import net.officefloor.plugin.clazz.dependency.ClassDependencyManufacturerContext.ClassFlow;
import net.officefloor.plugin.clazz.dependency.impl.ObjectClassDependencyManufacturer;
import net.officefloor.plugin.clazz.state.StatePoint;

/**
 * {@link Class} dependencies.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassDependenciesImpl implements ClassDependencies {

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
	 * Fallback {@link ClassDependencyManufacturer}.
	 */
	private static final ClassDependencyManufacturer fallbackDependencyManufacturer = new ObjectClassDependencyManufacturer();

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
	 * Flags whether using the {@link ClassDependencyFactory}.
	 */
	private boolean isUseFactory = true;

	/**
	 * Instantiate.
	 * 
	 * @param sourceContext       {@link SourceContext}.
	 * @param dependenciesContext {@link ClassDependenciesContext}.
	 */
	public ClassDependenciesImpl(SourceContext sourceContext, ClassDependenciesContext dependenciesContext) {
		this.sourceContext = sourceContext;
		this.dependenciesContext = dependenciesContext;
	}

	/**
	 * Allow registering an additional {@link Escalation}.
	 * 
	 * @param <E>            {@link Escalation} type.
	 * @param escalationType {@link Escalation} type.
	 */
	public <E extends Throwable> void addEscalation(Class<E> escalationType) {

		// Determine if already registered
		if (ClassDependenciesImpl.this.registeredEscalations.contains(escalationType)) {
			return; // already registered
		}

		// Add escalation and register
		ClassDependenciesImpl.this.dependenciesContext.addEscalation(escalationType);
		ClassDependenciesImpl.this.registeredEscalations.add(escalationType);
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
		return this.createClassDependencyFactory(StatePoint.of(field), field.getType(), field.getGenericType(),
				qualifier, field.getAnnotations());
	}

	@Override
	public ClassDependencyFactory createClassDependencyFactory(Executable executable, int parameterIndex,
			String qualifier) throws Exception {
		return this.createClassDependencyFactory(StatePoint.of(executable, parameterIndex),
				executable.getParameterTypes()[parameterIndex], executable.getGenericParameterTypes()[parameterIndex],
				qualifier, executable.getParameterAnnotations()[parameterIndex]);
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
			Type dependencyType, String qualifier, Annotation[] annotations) throws Exception {

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

		// Obtain the dependency manufacturer
		// (iterates through all manufacturers to ensure all annotations added)
		this.isUseFactory = true; // reset to use
		ClassDependencyFactory useFactory = null;
		ClassDependencyManufacturer useManufacturer = null;
		for (ClassDependencyManufacturer manufacturer : this.sourceContext
				.loadServices(ClassDependencyManufacturerServiceFactory.class, null)) {

			// Attempt to create the factory
			ClassDependencyFactory factory = manufacturer.createParameterFactory(context);
			if (factory != null) {

				// Determine if have factory to use
				if (this.isUseFactory) {
					// Use and register the factory
					useFactory = factory;
					useManufacturer = manufacturer;
					this.createdFactories.add(useFactory);

				} else {
					// Log that another factory potentially could be used
					this.sourceContext.getLogger()
							.info(statePoint.toLocation() + " ignoring " + manufacturer.getClass().getName()
									+ " as provided by " + useManufacturer.getClass().getSimpleName()
									+ " earlier in services listing");
				}
			}
		}
		if (useFactory != null) {
			return useFactory; // use factory
		}

		// As here, assume just a plain dependency
		return fallbackDependencyManufacturer.createParameterFactory(context);
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
		private final Annotation[] annotations;

		/**
		 * Instantiate.
		 * 
		 * @param statePoint      {@link StatePoint}.
		 * @param dependencyClass Dependency {@link Class}.
		 * @param dependencyType  Dependency {@link Type}.
		 * @param qualifier       Possible qualifier.
		 * @param annotations     {@link Annotation} instances.
		 */
		private ClassDependencyManufacturerContextImpl(StatePoint statePoint, Class<?> dependencyClass,
				Type dependencyType, String qualifier, Annotation[] annotations) {
			this.statePoint = statePoint;
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
			return ClassDependenciesImpl.this.sourceContext.getName();
		}

		@Override
		public Logger getLogger() {
			return ClassDependenciesImpl.this.sourceContext.getLogger();
		}

		@Override
		public SourceContext getSourceContext() {
			return ClassDependenciesImpl.this.sourceContext;
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
			return new ClassDependencyImpl(objectType);
		}

		@Override
		public ClassFlow addFlow(String name) {
			return new ClassFlowImpl(name);
		}

		@Override
		public <E extends Throwable> void addEscalation(Class<E> escalationType) {

			// Do not add if not using factory
			if (!ClassDependenciesImpl.this.isUseFactory) {
				return; // not using
			}

			// Add the escalation
			ClassDependenciesImpl.this.addEscalation(escalationType);
		}

		@Override
		public void addAnnotation(Object annotation) {

			// Always allow annotations to be added
			ClassDependenciesImpl.this.dependenciesContext.addAnnotation(annotation);
		}
	}

	/**
	 * {@link ClassDependency} implementation.
	 */
	private class ClassDependencyImpl implements ClassDependency {

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
		 * Index of this dependency.
		 */
		private int index = -1;

		/**
		 * Instantiate.
		 * 
		 * @param objectType Object {@link Class}.
		 */
		private ClassDependencyImpl(Class<?> objectType) {
			this.objectType = objectType;
		}

		/**
		 * Ensures still able to edit dependency.
		 * 
		 * @throws IllegalStateException If locked down.
		 */
		private void ensureNotIndexed() throws IllegalStateException {
			if (this.index >= 0) {
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
		public int getIndex() {

			// Easy access to dependencies
			ClassDependenciesImpl dependencies = ClassDependenciesImpl.this;

			// Do not add if not using factory
			if (!dependencies.isUseFactory) {
				return -1; // indicate not using
			}

			// Find matching dependency
			NEXT_DEPENDENCY: for (ClassDependencyImpl existing : dependencies.indexedDependencies) {

				// Ensure same type
				if (!isSameObjectType(this.objectType, existing.objectType)) {
					continue NEXT_DEPENDENCY; // not same
				}

				// Ensure same qualifier
				if (!isSameQualifier(this.qualifier, existing.qualifier)) {
					continue NEXT_DEPENDENCY; // not same
				}

				// Same dependency
				this.index = existing.index;
				return this.index;
			}

			// As here, new dependency
			Object[] annotations = this.annotations.toArray(new Object[this.annotations.size()]);
			this.index = dependencies.dependenciesContext.addDependency(this.qualifier, this.objectType, annotations);
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
		 * Index of this dependency.
		 */
		private int index = -1;

		/**
		 * Instantiate.
		 * 
		 * @param name Name of {@link Flow}.
		 */
		private ClassFlowImpl(String name) {
			this.name = name;
		}

		/**
		 * Ensures still able to edit dependency.
		 * 
		 * @throws IllegalStateException If locked down.
		 */
		private void ensureNotIndexed() throws IllegalStateException {
			if (this.index >= 0) {
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
		public int getIndex() {

			// Easy access to dependencies
			ClassDependenciesImpl dependencies = ClassDependenciesImpl.this;

			// Do not add if not using factory
			if (!dependencies.isUseFactory) {
				return -1; // indicate not using
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

				// Same flow
				this.index = existing.index;
				return this.index;
			}

			// As here, new flow
			Object[] annotations = this.annotations.toArray(new Object[this.annotations.size()]);
			this.index = dependencies.dependenciesContext.addFlow(this.name, this.argumentType, annotations);
			dependencies.indexedFlows.add(this);
			return this.index;
		}
	}

}