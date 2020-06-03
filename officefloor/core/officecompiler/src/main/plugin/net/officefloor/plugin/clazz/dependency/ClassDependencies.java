package net.officefloor.plugin.clazz.dependency;

import java.lang.annotation.Annotation;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.plugin.clazz.dependency.impl.ObjectClassDependencyManufacturer;
import net.officefloor.plugin.clazz.state.StatePoint;

/**
 * {@link Class} dependencies.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassDependencies implements ClassDependencyManufacturerContext {

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
	 * Created {@link ClassDependencyFactory} instances.
	 */
	private final List<ClassDependencyFactory> createdFactories = new LinkedList<>();

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
	 * @param name          Name.
	 * @param logger        {@link Logger}.
	 * @param sourceContext {@link SourceContext}.
	 */
	public ClassDependencies(String name, Logger logger, SourceContext sourceContext) {
		this.name = name;
		this.logger = logger;
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
		// TODO implement ClassDependencyManufacturerContext.addDependency
		throw new UnsupportedOperationException("TODO implement ClassDependencyManufacturerContext.addDependency");
	}

	@Override
	public ClassFlow addFlow() {
		// TODO implement ClassDependencyManufacturerContext.addFlow
		throw new UnsupportedOperationException("TODO implement ClassDependencyManufacturerContext.addFlow");
	}

	@Override
	public <E extends Throwable> ClassEscalation addEscalation(Class<E> escalationType) {
		// TODO implement ClassDependencyManufacturerContext.addEscalation
		throw new UnsupportedOperationException("TODO implement ClassDependencyManufacturerContext.addEscalation");
	}

}