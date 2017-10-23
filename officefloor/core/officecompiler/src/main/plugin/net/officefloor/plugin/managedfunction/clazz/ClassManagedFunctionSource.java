/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.managedfunction.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.officefloor.compile.ManagedFunctionSourceService;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.plugin.clazz.ClassFlowBuilder;
import net.officefloor.plugin.clazz.ClassFlowParameterFactory;
import net.officefloor.plugin.clazz.ClassFlowRegistry;
import net.officefloor.plugin.clazz.Sequence;

/**
 * {@link ManagedFunctionSource} for a {@link Class} having the {@link Method}
 * instances as the {@link ManagedFunction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassManagedFunctionSource extends AbstractManagedFunctionSource
		implements ManagedFunctionSourceService<ClassManagedFunctionSource> {

	/**
	 * Property name providing the {@link Class} name.
	 */
	public static final String CLASS_NAME_PROPERTY_NAME = "class.name";

	/**
	 * {@link ParameterManufacturer} instances.
	 */
	private final List<ParameterManufacturer> manufacturers = new LinkedList<ParameterManufacturer>();

	/**
	 * Initiate.
	 */
	public ClassManagedFunctionSource() {
		// Add the default manufacturers
		this.manufacturers.add(new ManagedFunctionContextParameterManufacturer());
		this.manufacturers.add(new FlowParameterManufacturer<FlowInterface>(FlowInterface.class));

		// Load any additional manufacturers
		this.loadParameterManufacturers(this.manufacturers);
	}

	/**
	 * Override to add additional {@link ParameterManufacturer} instances.
	 * 
	 * @param manufacturers
	 *            List of {@link ParameterManufacturer} instances to use.
	 */
	protected void loadParameterManufacturers(List<ParameterManufacturer> manufacturers) {
		// By default adds no further manufacturers
	}

	/**
	 * Allows overriding the creation of the {@link ManagedFunctionFactory}.
	 * 
	 * @param constructor
	 *            Default {@link Constructor} for the {@link Class} containing
	 *            the {@link Method}. Will be <code>null</code> if static
	 *            method.
	 * @param method
	 *            {@link Method} on the class.
	 * @param parameters
	 *            {@link ManagedFunctionParameterFactory} instances.
	 * @return {@link ManagedFunctionFactory}.
	 */
	protected ManagedFunctionFactory<Indexed, Indexed> createManagedFunctionFactory(Constructor<?> constructor,
			Method method, ManagedFunctionParameterFactory[] parameters) {
		return new ClassFunctionFactory(constructor, method, parameters);
	}

	/**
	 * Allows overriding the addition of the {@link ManagedFunctionTypeBuilder}.
	 * 
	 * @param clazz
	 *            {@link Class} containing the {@link Method}.
	 * @param namespaceBuilder
	 *            {@link FunctionNamespaceBuilder}.
	 * @param functionName
	 *            Name of the {@link ManagedFunction}.
	 * @param functionFactory
	 *            {@link ManagedFunctionFactory}.
	 * @param objectSequence
	 *            Object {@link Sequence}.
	 * @param flowSequence
	 *            Flow {@link Sequence}.
	 * @return Added {@link ManagedFunctionTypeBuilder}.
	 */
	protected ManagedFunctionTypeBuilder<Indexed, Indexed> addManagedFunctionType(Class<?> clazz,
			FunctionNamespaceBuilder namespaceBuilder, String functionName,
			ManagedFunctionFactory<Indexed, Indexed> functionFactory, Sequence objectSequence, Sequence flowSequence) {

		// Include method as function in type definition
		ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder = namespaceBuilder
				.addManagedFunctionType(functionName, functionFactory, Indexed.class, Indexed.class);

		// Return the function type builder
		return functionTypeBuilder;
	}

	/*
	 * =================== ManagedFunctionSourceService ===================
	 */

	@Override
	public String getManagedFunctionSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassManagedFunctionSource> getManagedFunctionSourceClass() {
		return ClassManagedFunctionSource.class;
	}

	/*
	 * =================== AbstractManagedFunctionSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CLASS_NAME_PROPERTY_NAME, "Class");
	}

	@SuppressWarnings("unchecked")
	public void sourceManagedFunctions(FunctionNamespaceBuilder namespaceBuilder, ManagedFunctionSourceContext context)
			throws Exception {

		// Obtain the class loader
		ClassLoader classLoader = context.getClassLoader();

		// Obtain the class
		String className = context.getProperty(CLASS_NAME_PROPERTY_NAME);
		Class<?> clazz = context.loadClass(className);

		// Work up the hierarchy of classes to inherit methods by name
		Set<String> includedMethodNames = new HashSet<String>();
		while ((clazz != null) && (!(Object.class.equals(clazz)))) {

			// Obtain the listing of functions from the methods of the class
			Set<String> currentClassMethods = new HashSet<String>();
			for (Method method : clazz.getDeclaredMethods()) {

				// Ignore non-public methods
				if (!Modifier.isPublic(method.getModifiers())) {
					continue;
				}

				// Ignore methods annotated to not be functions
				if (method.isAnnotationPresent(NonFunctionMethod.class)) {
					continue;
				}

				// Obtain details of the method
				String methodName = method.getName();
				Class<?>[] paramTypes = method.getParameterTypes();

				// Determine if method already exists on the current class
				if (currentClassMethods.contains(methodName)) {
					throw new IllegalStateException("Two methods by the same name '" + methodName + "' in class "
							+ clazz.getName() + ".  Either rename one of the methods or annotate one with @"
							+ NonFunctionMethod.class.getSimpleName());
				}
				currentClassMethods.add(methodName);

				// Ignore if already included method
				if (includedMethodNames.contains(methodName)) {
					continue;
				}
				includedMethodNames.add(methodName);

				// Create parameters to method to be populated
				ManagedFunctionParameterFactory[] parameters = new ManagedFunctionParameterFactory[paramTypes.length];

				// Determine if the method is static
				boolean isStatic = Modifier.isStatic(method.getModifiers());

				// Create the sequences for indexes to the objects and flows
				Sequence objectSequence = new Sequence();
				Sequence flowSequence = new Sequence();

				// Obtain the constructor (if not static)
				Constructor<?> constructor = null;
				if (!isStatic) {
					constructor = clazz.getConstructor(new Class<?>[0]);
				}

				// Create the function factory
				ManagedFunctionFactory<Indexed, Indexed> functionFactory = this
						.createManagedFunctionFactory(constructor, method, parameters);

				// Include method as function in type definition
				ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder = this.addManagedFunctionType(clazz,
						namespaceBuilder, methodName, functionFactory, objectSequence, flowSequence);

				// Define the return type (it not void)
				Class<?> returnType = method.getReturnType();
				if ((returnType != null) && (!Void.TYPE.equals(returnType))) {
					functionTypeBuilder.setReturnType(returnType);
				}

				// Obtain the parameter annotations (for qualifying)
				Annotation[][] methodParamAnnotations = method.getParameterAnnotations();

				// Define the listing of task objects and flows
				for (int i = 0; i < paramTypes.length; i++) {

					// Obtain the parameter type and its annotations
					Class<?> paramType = paramTypes[i];
					Annotation[] paramAnnotations = methodParamAnnotations[i];

					// Obtain the parameter factory
					ManagedFunctionParameterFactory parameterFactory = null;
					CREATED: for (ParameterManufacturer manufacturer : this.manufacturers) {
						parameterFactory = manufacturer.createParameterFactory(methodName, paramType,
								functionTypeBuilder, objectSequence, flowSequence, classLoader);
						if (parameterFactory != null) {
							// Created parameter factory, so use
							break CREATED;
						}
					}

					// Default to object if no parameter factory
					if (parameterFactory == null) {
						// Otherwise must be an dependency object
						parameterFactory = new ManagedFunctionObjectParameterFactory(objectSequence.nextIndex());
						ManagedFunctionObjectTypeBuilder<Indexed> objectTypeBuilder = functionTypeBuilder
								.addObject(paramType);

						// Determine type qualifier
						String typeQualifier = null;
						for (Annotation annotation : paramAnnotations) {

							// Obtain the annotation type
							Class<?> annotationType = annotation.annotationType();

							// Determine if qualifier annotation
							Qualifier qualifierAnnotation = annotationType.getAnnotation(Qualifier.class);
							if (qualifierAnnotation != null) {

								// Allow only one qualifier
								if (typeQualifier != null) {
									throw new IllegalArgumentException("Method " + methodName + " parameter " + i
											+ " has more than one " + Qualifier.class.getSimpleName());
								}

								// Obtain the qualifier name factory
								@SuppressWarnings("rawtypes")
								Class<? extends QualifierNameFactory> nameFactoryClass = qualifierAnnotation
										.nameFactory();
								QualifierNameFactory<Annotation> nameFactory = nameFactoryClass.newInstance();

								// Provide type qualifier
								typeQualifier = nameFactory.getQualifierName(annotation);
								objectTypeBuilder.setTypeQualifier(typeQualifier);
							}
						}

						// Specify the label
						String label = (typeQualifier != null ? typeQualifier + "-" : "") + paramType.getName();
						objectTypeBuilder.setLabel(label);
					}

					// Load the parameter factory
					parameters[i] = parameterFactory;
				}

				// Define the escalation listing
				for (Class<?> escalationType : method.getExceptionTypes()) {
					functionTypeBuilder.addEscalation((Class<Throwable>) escalationType);
				}
			}

			// Add methods from the parent class on next iteration
			clazz = clazz.getSuperclass();
		}
	}

	/**
	 * Manufactures the {@link ManagedFunctionParameterFactory}.
	 */
	protected static interface ParameterManufacturer {

		/**
		 * Creates the {@link ManagedFunctionParameterFactory}.
		 * 
		 * @param functionName
		 *            Name of the {@link ManagedFunction}.
		 * @param parameterType
		 *            Parameter type.
		 * @param functionTypeBuilder
		 *            {@link ManagedFunctionTypeBuilder}.
		 * @param objectSequence
		 *            Object {@link Sequence}.
		 * @param flowSequence
		 *            Flow {@link Sequence}.
		 * @param classLoader
		 *            {@link ClassLoader}.
		 * @return {@link ManagedFunctionParameterFactory} or <code>null</code>
		 *         if not appropriate for this to manufacture a
		 *         {@link ManagedFunctionParameterFactory}.
		 * @throws Exception
		 *             If fails to create the
		 *             {@link ManagedFunctionParameterFactory}.
		 */
		ManagedFunctionParameterFactory createParameterFactory(String functionName, Class<?> parameterType,
				ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder, Sequence objectSequence,
				Sequence flowSequence, ClassLoader classLoader) throws Exception;
	}

	/**
	 * {@link ParameterManufacturer} for the {@link ManagedFunctionContext}.
	 */
	protected static class ManagedFunctionContextParameterManufacturer implements ParameterManufacturer {
		@Override
		public ManagedFunctionParameterFactory createParameterFactory(String functionName, Class<?> parameterType,
				ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder, Sequence objectSequence,
				Sequence flowSequence, ClassLoader classLoader) {

			// Determine if task context
			if (ManagedFunctionContext.class.equals(parameterType)) {
				// Parameter is a task context
				return new ManagedFunctionContextParameterFactory();
			}

			// Not function context
			return null;
		}
	}

	/**
	 * {@link ParameterManufacturer} for the {@link FlowInterface}.
	 */
	protected static class FlowParameterManufacturer<A extends Annotation> implements ParameterManufacturer {

		/**
		 * {@link Class} of the {@link Annotation}.
		 */
		private final Class<A> annotationClass;

		/**
		 * Instantiate.
		 * 
		 * @param annotationClass
		 *            {@link Class} of the {@link Annotation}.
		 */
		public FlowParameterManufacturer(Class<A> annotationClass) {
			this.annotationClass = annotationClass;
		}

		/*
		 * =================== ParameterManufacturer ===================
		 */

		@Override
		public ManagedFunctionParameterFactory createParameterFactory(String functionName, Class<?> parameterType,
				ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder, Sequence objectSequence,
				Sequence flowSequence, ClassLoader classLoader) throws Exception {

			// Create the flow registry
			ClassFlowRegistry flowRegistry = (label, flowParameterType) -> {
				// Register the flow
				ManagedFunctionFlowTypeBuilder<Indexed> flowTypeBuilder = functionTypeBuilder.addFlow();
				flowTypeBuilder.setLabel(label);
				if (flowParameterType != null) {
					flowTypeBuilder.setArgumentType(flowParameterType);
				}
			};

			// Attempt to build flow parameter factory
			ClassFlowParameterFactory flowParameterFactory = new ClassFlowBuilder<A>(this.annotationClass)
					.buildFlowParameterFactory(functionName, parameterType, flowSequence, flowRegistry, classLoader);
			if (flowParameterFactory == null) {
				return null; // not flow interface
			}

			// Return wrapping managed function flow parameter factory
			return new ManagedFunctionFlowParameterFactory(flowParameterFactory);
		}
	}

}