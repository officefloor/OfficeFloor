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
package net.officefloor.plugin.work.clazz;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.officefloor.compile.ManagedFunctionSourceService;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.WorkFactory;
import net.officefloor.frame.api.function.FlowFuture;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.function.Work;

/**
 * {@link ManagedFunctionSource} for a {@link Class} having the {@link Object} as the
 * {@link Work} and {@link Method} instances as the {@link ManagedFunction} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class ClassWorkSource extends AbstractManagedFunctionSource<ClassWork> implements
		ManagedFunctionSourceService<ClassWork, ClassWorkSource> {

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
	public ClassWorkSource() {
		// Add the default manufacturers
		this.manufacturers.add(new TaskContextParameterManufacturer());
		this.manufacturers
				.add(new FlowInterfaceParameterManufacturer<FlowInterface>(
						FlowInterface.class));

		// Load any additional manufacturers
		this.loadParameterManufacturers(this.manufacturers);
	}

	/**
	 * Override to add additional {@link ParameterManufacturer} instances.
	 * 
	 * @param manufacturers
	 *            List of {@link ParameterManufacturer} instances to use.
	 */
	protected void loadParameterManufacturers(
			List<ParameterManufacturer> manufacturers) {
		// By default adds no further manufacturers
	}

	/**
	 * Allows overriding the creation of the {@link ManagedFunctionFactory}.
	 * 
	 * @param clazz
	 *            {@link Work} class.
	 * @return {@link WorkFactory}.
	 */
	protected WorkFactory<ClassWork> createWorkFactory(Class<?> clazz) {
		return new ClassWorkFactory(clazz);
	}

	/**
	 * Allows overriding the creation of the {@link ManagedFunctionFactory}.
	 * 
	 * @param clazz
	 *            {@link Work} class.
	 * @param method
	 *            {@link Method} on the class.
	 * @param isStatic
	 *            Indicates if the {@link Method} is static.
	 * @param parameters
	 *            {@link ParameterFactory} instances.
	 * @return {@link ManagedFunctionFactory}.
	 */
	protected ManagedFunctionFactory<ClassWork, Indexed, Indexed> createTaskFactory(
			Class<?> clazz, Method method, boolean isStatic,
			ParameterFactory[] parameters) {
		return new ClassTaskFactory(method, isStatic, parameters);
	}

	/**
	 * Allows overriding the addition of the {@link ManagedFunctionTypeBuilder}.
	 * 
	 * @param clazz
	 *            {@link Work} class.
	 * @param workTypeBuilder
	 *            {@link FunctionNamespaceBuilder}.
	 * @param taskName
	 *            Name of the {@link ManagedFunction}.
	 * @param taskFactory
	 *            {@link ManagedFunctionFactory}.
	 * @param objectSequence
	 *            Object {@link Sequence}.
	 * @param flowSequence
	 *            Flow {@link Sequence}.
	 * @return Added {@link ManagedFunctionTypeBuilder}.
	 */
	protected ManagedFunctionTypeBuilder<Indexed, Indexed> addTaskType(Class<?> clazz,
			FunctionNamespaceBuilder<ClassWork> workTypeBuilder, String taskName,
			ManagedFunctionFactory<ClassWork, Indexed, Indexed> taskFactory,
			Sequence objectSequence, Sequence flowSequence) {

		// Include method as task in type definition
		ManagedFunctionTypeBuilder<Indexed, Indexed> taskTypeBuilder = workTypeBuilder
				.addManagedFunctionType(taskName, taskFactory, null, null);

		// Return the task type builder
		return taskTypeBuilder;
	}

	/*
	 * =================== WorkSourceService ================================
	 */

	@Override
	public String getWorkSourceAlias() {
		return "CLASS";
	}

	@Override
	public Class<ClassWorkSource> getWorkSourceClass() {
		return ClassWorkSource.class;
	}

	/*
	 * =================== AbstractWorkLoader ==============================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		context.addProperty(CLASS_NAME_PROPERTY_NAME, "Class");
	}

	@SuppressWarnings("unchecked")
	public void sourceManagedFunctions(FunctionNamespaceBuilder<ClassWork> workTypeBuilder,
			ManagedFunctionSourceContext context) throws Exception {

		// Obtain the class loader
		ClassLoader classLoader = context.getClassLoader();

		// Obtain the class
		String className = context.getProperty(CLASS_NAME_PROPERTY_NAME);
		Class<?> clazz = context.loadClass(className);

		// Define the work factory
		WorkFactory<ClassWork> workFactory = this.createWorkFactory(clazz);
		workTypeBuilder.setWorkFactory(workFactory);

		// Work up the hierarchy of classes to inherit methods by name
		Set<String> includedMethodNames = new HashSet<String>();
		while ((clazz != null) && (!(Object.class.equals(clazz)))) {

			// Obtain the listing of tasks from the methods of the class
			Set<String> currentClassMethods = new HashSet<String>();
			for (Method method : clazz.getDeclaredMethods()) {

				// Ignore non-public methods
				if (!Modifier.isPublic(method.getModifiers())) {
					continue;
				}

				// Ignore methods annotated to not be tasks
				if (method.isAnnotationPresent(NonTaskMethod.class)) {
					continue;
				}

				// Obtain details of the method
				String methodName = method.getName();
				Class<?>[] paramTypes = method.getParameterTypes();

				// Determine if method already exists on the current class
				if (currentClassMethods.contains(methodName)) {
					throw new IllegalStateException(
							"Two methods by the same name '"
									+ methodName
									+ "' in class "
									+ clazz.getName()
									+ ".  Either rename one of the methods or annotate one with @"
									+ NonTaskMethod.class.getSimpleName());
				}
				currentClassMethods.add(methodName);

				// Ignore if already included method
				if (includedMethodNames.contains(methodName)) {
					continue;
				}
				includedMethodNames.add(methodName);

				// Create to parameters to method to be populated
				ParameterFactory[] parameters = new ParameterFactory[paramTypes.length];

				// Determine if the method is static
				boolean isStatic = Modifier.isStatic(method.getModifiers());

				// Create the sequences for indexes to the objects and flows
				Sequence objectSequence = new Sequence();
				Sequence flowSequence = new Sequence();

				// Create the task factory
				ManagedFunctionFactory<ClassWork, Indexed, Indexed> taskFactory = this
						.createTaskFactory(clazz, method, isStatic, parameters);

				// Include method as task in type definition
				ManagedFunctionTypeBuilder<Indexed, Indexed> taskTypeBuilder = this
						.addTaskType(clazz, workTypeBuilder, methodName,
								taskFactory, objectSequence, flowSequence);

				// Define the return type (it not void)
				Class<?> returnType = method.getReturnType();
				if ((returnType != null) && (!Void.TYPE.equals(returnType))) {
					taskTypeBuilder.setReturnType(returnType);
				}

				// Obtain the parameter annotations (for qualifying)
				Annotation[][] methodParamAnnotations = method
						.getParameterAnnotations();

				// Define the listing of task objects and flows
				for (int i = 0; i < paramTypes.length; i++) {

					// Obtain the parameter type and its annotations
					Class<?> paramType = paramTypes[i];
					Annotation[] paramAnnotations = methodParamAnnotations[i];

					// Obtain the parameter factory
					ParameterFactory parameterFactory = null;
					CREATED: for (ParameterManufacturer manufacturer : this.manufacturers) {
						parameterFactory = manufacturer.createParameterFactory(
								methodName, paramType, taskTypeBuilder,
								objectSequence, flowSequence, classLoader);
						if (parameterFactory != null) {
							// Created parameter factory, so use
							break CREATED;
						}
					}

					// Default to object if no parameter factory
					if (parameterFactory == null) {
						// Otherwise must be an dependency object
						parameterFactory = new ObjectParameterFactory(
								objectSequence.nextIndex());
						ManagedFunctionObjectTypeBuilder<Indexed> objectTypeBuilder = taskTypeBuilder
								.addObject(paramType);

						// Determine type qualifier
						String typeQualifier = null;
						for (Annotation annotation : paramAnnotations) {

							// Obtain the annotation type
							Class<?> annotationType = annotation
									.annotationType();

							// Determine if qualifier annotation
							if (annotationType
									.isAnnotationPresent(Qualifier.class)) {

								// Allow only one qualifier
								if (typeQualifier != null) {
									throw new IllegalArgumentException(
											"Method "
													+ methodName
													+ " parameter "
													+ i
													+ " has more than one "
													+ Qualifier.class
															.getSimpleName());
								}

								// Provide type qualifier
								typeQualifier = annotationType.getName();
								objectTypeBuilder
										.setTypeQualifier(typeQualifier);
							}
						}

						// Specify the label
						String label = (typeQualifier != null ? typeQualifier
								+ "-" : "")
								+ paramType.getName();
						objectTypeBuilder.setLabel(label);
					}

					// Load the parameter factory
					parameters[i] = parameterFactory;
				}

				// Define the escalation listing
				for (Class<?> escalationType : method.getExceptionTypes()) {
					taskTypeBuilder
							.addEscalation((Class<Throwable>) escalationType);
				}
			}

			// Add methods from the parent class on next iteration
			clazz = clazz.getSuperclass();
		}
	}

	/**
	 * Manufactures the {@link ParameterFactory}.
	 */
	protected static interface ParameterManufacturer {

		/**
		 * Creates the {@link ParameterFactory}.
		 * 
		 * @param taskName
		 *            Name of the {@link ManagedFunction}.
		 * @param parameterType
		 *            Parameter type.
		 * @param taskTypeBuilder
		 *            {@link ManagedFunctionTypeBuilder}.
		 * @param objectSequence
		 *            Object {@link Sequence}.
		 * @param flowSequence
		 *            Flow {@link Sequence}.
		 * @param classLoader
		 *            {@link ClassLoader}.
		 * @return {@link ParameterFactory} or <code>null</code> if not
		 *         appropriate for this to manufacture a
		 *         {@link ParameterFactory}.
		 * @throws Exception
		 *             If fails to create the {@link ParameterFactory}.
		 */
		ParameterFactory createParameterFactory(String taskName,
				Class<?> parameterType,
				ManagedFunctionTypeBuilder<Indexed, Indexed> taskTypeBuilder,
				Sequence objectSequence, Sequence flowSequence,
				ClassLoader classLoader) throws Exception;
	}

	/**
	 * {@link ParameterManufacturer} for the {@link ManagedFunctionContext}.
	 */
	protected static class TaskContextParameterManufacturer implements
			ParameterManufacturer {
		@Override
		public ParameterFactory createParameterFactory(String taskName,
				Class<?> parameterType,
				ManagedFunctionTypeBuilder<Indexed, Indexed> taskTypeBuilder,
				Sequence objectSequence, Sequence flowSequence,
				ClassLoader classLoader) {

			// Determine if task context
			if (ManagedFunctionContext.class.equals(parameterType)) {
				// Parameter is a task context
				return new TaskContextParameterFactory();
			}

			// Not task context
			return null;
		}
	}

	/**
	 * {@link ParameterManufacturer} for the {@link FlowInterface}.
	 */
	protected static class FlowInterfaceParameterManufacturer<A extends Annotation>
			implements ParameterManufacturer {

		/**
		 * Annotation class expected on the parameter type.
		 */
		private final Class<A> annotationClass;

		/**
		 * Initiate.
		 * 
		 * @param annotationClass
		 *            Annotation class expected on the parameter type.
		 */
		public FlowInterfaceParameterManufacturer(Class<A> annotationClass) {
			this.annotationClass = annotationClass;
		}

		/*
		 * ================== ParameterManufacturer ====================
		 */

		@Override
		public ParameterFactory createParameterFactory(String taskName,
				Class<?> parameterType,
				ManagedFunctionTypeBuilder<Indexed, Indexed> taskTypeBuilder,
				Sequence objectSequence, Sequence flowSequence,
				ClassLoader classLoader) throws Exception {

			// Determine if flow interface
			if (parameterType.getAnnotation(this.annotationClass) == null) {
				return null; // not a flow interface
			}

			// Ensure is an interface
			if (!parameterType.isInterface()) {
				throw new Exception(
						"Parameter "
								+ parameterType.getSimpleName()
								+ " on method "
								+ taskName
								+ " must be an interface as parameter type is annotated with "
								+ this.annotationClass.getSimpleName());
			}

			// Obtain the methods sorted (deterministic order)
			Method[] flowMethods = parameterType.getMethods();
			Arrays.sort(flowMethods, new Comparator<Method>() {
				@Override
				public int compare(Method a, Method b) {
					return a.getName().compareTo(b.getName());
				}
			});

			// Create a flow for each method of the interface
			Map<String, FlowMethodMetaData> flowMethodMetaDatas = new HashMap<String, FlowMethodMetaData>(
					flowMethods.length);
			for (int m = 0; m < flowMethods.length; m++) {
				Method flowMethod = flowMethods[m];
				String flowMethodName = flowMethod.getName();

				// Not include object methods
				if (Object.class.equals(flowMethod.getDeclaringClass())) {
					continue;
				}

				// Ensure not duplicate flow names
				if (flowMethodMetaDatas.containsKey(flowMethodName)) {
					throw new Exception(
							"May not have duplicate flow method names (task="
									+ taskName + ", flow="
									+ parameterType.getSimpleName() + "."
									+ flowMethodName + ")");
				}

				// Ensure at most one parameter
				Class<?> flowParameterType;
				Class<?>[] flowMethodParams = flowMethod.getParameterTypes();
				if (flowMethodParams.length == 0) {
					flowParameterType = null;
				} else if (flowMethodParams.length == 1) {
					flowParameterType = flowMethodParams[0];
				} else {
					// Invalid to have more than one parameter
					throw new Exception(
							"Flow methods may only have at most one parameter (task "
									+ taskName + ", flow "
									+ parameterType.getSimpleName() + "."
									+ flowMethodName + ")");
				}

				// Ensure correct return type
				boolean isReturnFlowFuture;
				Class<?> flowReturnType = flowMethod.getReturnType();
				if (FlowFuture.class.equals(flowReturnType)) {
					// Returns a flow future
					isReturnFlowFuture = true;
				} else if (Void.TYPE.equals(flowReturnType)
						|| (flowReturnType == null)) {
					// Does not return value
					isReturnFlowFuture = false;
				} else {
					// Invalid return type
					throw new Exception("Flow method "
							+ parameterType.getSimpleName() + "." + taskName
							+ " return type is invalid (return type="
							+ flowReturnType.getName() + ", task=" + taskName
							+ ")");
				}

				// Create and register the flow method meta-data
				FlowMethodMetaData flowMethodMetaData = new FlowMethodMetaData(
						parameterType, flowMethod, flowSequence.nextIndex(),
						(flowParameterType != null), isReturnFlowFuture);
				flowMethodMetaDatas.put(flowMethodName, flowMethodMetaData);

				// Register the flow
				ManagedFunctionFlowTypeBuilder<Indexed> flowTypeBuilder = taskTypeBuilder
						.addFlow();
				flowTypeBuilder.setLabel(flowMethodName);
				if (flowParameterType != null) {
					flowTypeBuilder.setArgumentType(flowParameterType);
				}
			}

			// Create and return the flow interface parameter factory
			return new FlowParameterFactory(classLoader, parameterType,
					flowMethodMetaDatas);
		}
	}

}