package net.officefloor.plugin.clazz.dependency;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.DependencyLabeller;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.Labeller;
import net.officefloor.frame.api.managedobject.source.impl.AbstractAsyncManagedObjectSource.MetaDataContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.InvalidConfigurationError;
import net.officefloor.plugin.clazz.dependency.impl.ObjectClassDependencyManufacturer;
import net.officefloor.plugin.clazz.state.StatePoint;

/**
 * {@link Class} dependencies.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassDependencies implements ClassDependencyManufacturerContext {

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
	 * Fallback {@link ClassDependencyManufacturer}.
	 */
	private static final ClassDependencyManufacturer fallbackDependencyManufacturer = new ObjectClassDependencyManufacturer();

	/**
	 * Name.
	 */
	private final String name;

	/**
	 * {@link Logger}.
	 */
	private final Logger logger;

	/**
	 * {@link SourceContext}.
	 */
	private final SourceContext sourceContext;

	/**
	 * {@link MetaDataContext} for the {@link ManagedObject}.
	 */
	private final MetaDataContext<Indexed, Indexed> managedObjectContext;

	/**
	 * {@link ManagedFunctionTypeBuilder} for the {@link ManagedFunction}.
	 */
	private final ManagedFunctionTypeBuilder<Indexed, Indexed> managedFunctionContext;

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
	 * @param name                 Name.
	 * @param logger               {@link Logger}.
	 * @param sourceContext        {@link SourceContext}.
	 * @param managedObjectContext {@link MetaDataContext} for the
	 *                             {@link ManagedObject}.
	 */
	public ClassDependencies(String name, Logger logger, SourceContext sourceContext,
			MetaDataContext<Indexed, Indexed> managedObjectContext) {
		this.name = name;
		this.logger = logger;
		this.sourceContext = sourceContext;
		this.managedObjectContext = managedObjectContext;
		this.managedFunctionContext = null;
	}

	/**
	 * Instantiate.
	 * 
	 * @param name                   Name.
	 * @param logger                 {@link Logger}.
	 * @param sourceContext          {@link SourceContext}.
	 * @param managedFunctionContext {@link ManagedFunctionTypeBuilder} for the
	 *                               {@link ManagedFunction}.
	 */
	public ClassDependencies(String name, Logger logger, SourceContext sourceContext,
			ManagedFunctionTypeBuilder<Indexed, Indexed> managedFunctionContext) {
		this.name = name;
		this.logger = logger;
		this.sourceContext = sourceContext;
		this.managedObjectContext = null;
		this.managedFunctionContext = managedFunctionContext;
	}

	/**
	 * Creates the {@link ClassDependencyFactory} for a {@link Field}.
	 * 
	 * @param field     {@link Field}.
	 * @param qualifier Qualifier.
	 * @return {@link ClassDependencyFactory}.
	 * @throws Exception If fails to create.
	 */
	public ClassDependencyFactory createClassDependencyFactory(Field field, String qualifier) throws Exception {
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
	public ClassDependencyFactory createClassDependencyFactory(Executable executable, int parameterIndex,
			String qualifier) throws Exception {
		return this.createClassDependencyFactory(StatePoint.of(executable, parameterIndex),
				executable.getParameterTypes()[parameterIndex], executable.getGenericParameterTypes()[parameterIndex],
				qualifier, executable.getParameterAnnotations()[parameterIndex]);
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

				// Register the factory
				this.createdFactories.add(factory);

				// Return the factory for dependency
				return factory;
			}
		}

		// As here, assume just a plain dependency
		return fallbackDependencyManufacturer.createParameterFactory(this);
	}

	/*
	 * =================== ClassDependencyManufacturerContext ===================
	 */

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Logger getLogger() {
		return this.logger;
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
		return new ClassDependencyImpl(objectType);
	}

	@Override
	public ClassFlow addFlow(String name) {
		return new ClassFlowImpl(name);
	}

	@Override
	public <E extends Throwable> void addEscalation(Class<E> escalationType) {
		// TODO implement ClassDependencyManufacturerContext.addEscalation
		throw new UnsupportedOperationException("TODO implement ClassDependencyManufacturerContext.addEscalation");
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
		public int getIndex() {

			// Easy access to dependencies
			ClassDependencies dependencies = ClassDependencies.this;

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

			// Create the label
			String label = (this.qualifier != null ? this.qualifier + "-" : "") + this.objectType.getName();

			// As here, new dependency
			int index;
			if (dependencies.managedObjectContext != null) {
				// Add dependency to managed object
				DependencyLabeller<Indexed> dependencyLabeller = dependencies.managedObjectContext
						.addDependency(this.objectType);
				dependencyLabeller.setLabel(label);
				if (this.qualifier != null) {
					dependencyLabeller.setTypeQualifier(this.qualifier);
				}
				index = dependencyLabeller.getIndex();

			} else {
				// Add dependency to managed function
				ManagedFunctionObjectTypeBuilder<Indexed> dependencyBuilder = dependencies.managedFunctionContext
						.addObject(this.objectType);
				dependencyBuilder.setLabel(label);
				if (this.qualifier != null) {
					dependencyBuilder.setTypeQualifier(this.qualifier);
				}
				for (Object annotation : this.annotations) {
					dependencyBuilder.addAnnotation(annotation);
				}

				// Determine the index of the dependency
				index = dependencies.indexedDependencies.size();
			}

			// Register dependency and return index
			this.index = index;
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
		public int getIndex() {

			// Easy access to dependencies
			ClassDependencies dependencies = ClassDependencies.this;

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

			// As here, add flow
			int index;
			if (dependencies.managedObjectContext != null) {
				// Add flow to managed object
				Labeller<Indexed> flowLabeller = dependencies.managedObjectContext.addFlow(this.argumentType);
				flowLabeller.setLabel(this.name);
				index = flowLabeller.getIndex();

			} else {
				// Add flow to managed function
				ManagedFunctionFlowTypeBuilder<Indexed> flowBuilder = dependencies.managedFunctionContext.addFlow();
				flowBuilder.setLabel(this.name);
				if (this.argumentType != null) {
					flowBuilder.setArgumentType(this.argumentType);
				}

				// Determine the index of the dependency
				index = dependencies.indexedDependencies.size();
			}

			// Register dependency and return index
			this.index = index;
			dependencies.indexedFlows.add(this);
			return index;
		}
	}

}