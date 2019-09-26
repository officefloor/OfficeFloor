/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.plugin.managedfunction.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.ClassFlowBuilder;
import net.officefloor.plugin.clazz.ClassFlowParameterFactory;
import net.officefloor.plugin.clazz.ClassFlowRegistry;
import net.officefloor.plugin.clazz.FlowInterface;
import net.officefloor.plugin.clazz.NonFunctionMethod;
import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.clazz.QualifierNameFactory;
import net.officefloor.plugin.clazz.Sequence;
import net.officefloor.plugin.managedfunction.method.parameter.ManagedFunctionAsynchronousFlowParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.ManagedFunctionContextParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.ManagedFunctionFlowParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.ManagedFunctionInParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.ManagedFunctionObjectParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.ManagedFunctionOutParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.ManagedFunctionParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.ManagedFunctionValueParameterFactory;
import net.officefloor.plugin.managedfunction.method.parameter.ManagedFunctionVariableParameterFactory;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableAnnotation;

/**
 * Builder to take {@link Method} to produce a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodManagedFunctionBuilder {

	/**
	 * {@link ParameterManufacturer} instances.
	 */
	private final List<ParameterManufacturer> manufacturers = new LinkedList<ParameterManufacturer>();

	/**
	 * Initiate.
	 */
	public MethodManagedFunctionBuilder() {
		// Add the default manufacturers
		this.manufacturers.add(new ManagedFunctionContextParameterManufacturer());
		this.manufacturers.add(new AsynchronousFlowParameterManufacturer());
		this.manufacturers.add(new FlowParameterManufacturer<FlowInterface>(FlowInterface.class));

		// Load any additional manufacturers
		this.loadParameterManufacturers(this.manufacturers);
	}

	/**
	 * Override to add additional {@link ParameterManufacturer} instances.
	 * 
	 * @param manufacturers List of {@link ParameterManufacturer} instances to use.
	 */
	protected void loadParameterManufacturers(List<ParameterManufacturer> manufacturers) {
		// By default adds no further manufacturers
	}

	/**
	 * Allows overriding the creation of the {@link ManagedFunctionFactory}.
	 * 
	 * @param context {@link MethodManagedFunctionFactoryContext}.
	 * @return {@link ManagedFunctionFactory}.
	 * @throws Exception If fails to create {@link ManagedFunctionFactory}.
	 */
	protected ManagedFunctionFactory<Indexed, Indexed> createManagedFunctionFactory(
			MethodManagedFunctionFactoryContext context) throws Exception {
		return new MethodFunctionFactory(context.getMethodObjectInstanceFactory(), context.getMethod(),
				context.getParameters());
	}

	/**
	 * Allows overriding the addition of the {@link ManagedFunctionTypeBuilder}.
	 * 
	 * @param context {@link MethodManagedFunctionFactoryContext}.
	 * @return Added {@link ManagedFunctionTypeBuilder}.
	 * @throws Exception If fails to create {@link ManagedFunctionTypeBuilder}.
	 */
	protected ManagedFunctionTypeBuilder<Indexed, Indexed> addManagedFunctionType(
			MethodManagedFunctionTypeContext context) throws Exception {

		// Include method as function in type definition
		ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder = context.getNamespaceBuilder()
				.addManagedFunctionType(context.getFunctionName(), context.getFunctionFactory(), Indexed.class,
						Indexed.class);

		// Return the function type builder
		return functionTypeBuilder;
	}

	/**
	 * Enriches the {@link ManagedFunctionTypeBuilder}.
	 * 
	 * @param context {@link EnrichManagedFunctionTypeContext}.
	 */
	protected void enrichManagedFunctionType(EnrichManagedFunctionTypeContext context) {
		// No enrichment
	}

	/**
	 * Enriches the {@link ManagedFunctionObjectTypeBuilder}.
	 * 
	 * @param objectType         Object type.
	 * @param genericType        Generic type.
	 * @param annotations        {@link Annotation} instances.
	 * @param functionObjectType {@link ManagedFunctionObjectTypeBuilder}.
	 */
	protected void enrichManagedFunctionObjectType(Class<?> objectType, Type genericType, Annotation[] annotations,
			ManagedFunctionObjectTypeBuilder<Indexed> functionObjectType) {
		// No enrichment
	}

	/**
	 * Indicates if candidate {@link Method} for {@link ManagedFunction}.
	 * 
	 * @param method {@link Method}.
	 * @return <code>true</code> if candidate {@link Method} for
	 *         {@link ManagedFunction}.
	 */
	public boolean isCandidateFunctionMethod(Method method) {

		// Candidate if public and not flagged not function
		return Modifier.isPublic(method.getModifiers()) && (!(method.isAnnotationPresent(NonFunctionMethod.class)));
	}

	/**
	 * Builds the {@link ManagedFunction}.
	 * 
	 * @param method                           {@link Method} for the
	 *                                         {@link ManagedFunction}.
	 * @param instanceClass                    {@link Class} instance containing the
	 *                                         {@link Method}.
	 * @param methodObjectInstanceManufacturer {@link MethodObjectInstanceManufacturer}.
	 * @param namespaceBuilder                 {@link FunctionNamespaceBuilder}.
	 * @param context                          {@link ManagedFunctionSourceContext}.
	 * @return {@link ManagedFunctionTypeBuilder} for the {@link Method} or
	 *         <code>null</code> if can not to be a {@link ManagedFunction}.
	 * @throws Exception If fails to create the {@link ManagedFunction} from the
	 *                   {@link Method}.
	 */
	@SuppressWarnings("unchecked")
	public ManagedFunctionTypeBuilder<Indexed, Indexed> buildMethod(Method method, Class<?> instanceClass,
			MethodObjectInstanceManufacturer methodObjectInstanceManufacturer,
			FunctionNamespaceBuilder namespaceBuilder, ManagedFunctionSourceContext context) throws Exception {

		// Obtain details of the method
		String methodName = method.getName();
		Class<?>[] paramTypes = method.getParameterTypes();

		// Create parameters to method to be populated
		ManagedFunctionParameterFactory[] parameters = new ManagedFunctionParameterFactory[paramTypes.length];

		// Create the sequences for indexes to the objects and flows
		Sequence objectSequence = new Sequence();
		Sequence flowSequence = new Sequence();

		// Obtain the factory to obtain object instance (if not static)
		boolean isStatic = Modifier.isStatic(method.getModifiers());
		MethodObjectInstanceFactory methodObjectInstanceFactory = null;
		if (!isStatic) {
			methodObjectInstanceFactory = methodObjectInstanceManufacturer.createMethodObjectInstanceFactory();
		}

		// Create the function factory
		ManagedFunctionFactory<Indexed, Indexed> functionFactory = this
				.createManagedFunctionFactory(new MethodManagedFunctionFactoryContext(methodName, method, instanceClass,
						methodObjectInstanceFactory, parameters));

		// Include method as function in type definition
		ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder = this
				.addManagedFunctionType(new MethodManagedFunctionTypeContext(methodName, method, instanceClass,
						methodObjectInstanceFactory, functionFactory, namespaceBuilder, objectSequence, flowSequence));

		// Define the return type (it not void)
		Class<?> returnType = method.getReturnType();
		if ((returnType != null) && (!Void.TYPE.equals(returnType))) {
			functionTypeBuilder.setReturnType(returnType);
		}

		// Load the function annotations
		Annotation[] methodAnnotations = method.getAnnotations();
		for (Annotation annotation : methodAnnotations) {
			functionTypeBuilder.addAnnotation(annotation);
		}

		// Obtain the generic parameters
		Type[] methodGenericParameters = method.getGenericParameterTypes();

		// Obtain the parameter annotations (for qualifying)
		Annotation[][] methodParamAnnotations = method.getParameterAnnotations();

		// Define the listing of objects and flows
		for (int i = 0; i < paramTypes.length; i++) {

			// Obtain the parameter type and its annotations
			Class<?> paramType = paramTypes[i];
			Type paramGenericType = methodGenericParameters[i];
			Annotation[] typeAnnotations = paramType.getAnnotations();
			Annotation[] paramAnnotations = methodParamAnnotations[i];

			// Handle primitives
			if (boolean.class.equals(paramType)) {
				paramType = Boolean.class;
			} else if (byte.class.equals(paramType)) {
				paramType = Byte.class;
			} else if (short.class.equals(paramType)) {
				paramType = Short.class;
			} else if (char.class.equals(paramType)) {
				paramType = Character.class;
			} else if (int.class.equals(paramType)) {
				paramType = Integer.class;
			} else if (long.class.equals(paramType)) {
				paramType = Long.class;
			} else if (float.class.equals(paramType)) {
				paramType = Float.class;
			} else if (double.class.equals(paramType)) {
				paramType = Double.class;
			}

			// Obtain the parameter factory
			ManagedFunctionParameterFactory parameterFactory = null;
			CREATED: for (ParameterManufacturer manufacturer : this.manufacturers) {
				parameterFactory = manufacturer.createParameterFactory(methodName, paramType, functionTypeBuilder,
						objectSequence, flowSequence, context);
				if (parameterFactory != null) {
					// Created parameter factory, so use
					break CREATED;
				}
			}

			// If not context dependency, must be injected dependency
			if (parameterFactory == null) {

				// Create the listing of all annotations
				List<Annotation> allAnnotations = new ArrayList<>(typeAnnotations.length + paramAnnotations.length);
				allAnnotations.addAll(Arrays.asList(typeAnnotations));
				allAnnotations.addAll(Arrays.asList(paramAnnotations));

				// Determine if Val
				boolean isVal = false;
				for (Annotation annotation : allAnnotations) {
					if (Val.class.equals(annotation.annotationType())) {
						isVal = true;
					}
				}

				// Determine the dependency
				ManagedFunctionObjectTypeBuilder<Indexed> objectTypeBuilder;
				String typeQualifierSuffix;
				boolean isIncludeVariableAnnotation;
				String labelPrefix;
				String labelSuffix;
				final String VAR_LABEL_PREFIX = "VAR-";
				if (isVal) {
					// Value (from variable)
					parameterFactory = new ManagedFunctionValueParameterFactory(objectSequence.nextIndex());
					objectTypeBuilder = functionTypeBuilder.addObject(Var.class);
					typeQualifierSuffix = paramType.getTypeName();
					isIncludeVariableAnnotation = true;
					labelPrefix = VAR_LABEL_PREFIX;
					labelSuffix = typeQualifierSuffix;

				} else if (Var.class.equals(paramType)) {
					// Variable
					parameterFactory = new ManagedFunctionVariableParameterFactory(objectSequence.nextIndex());
					objectTypeBuilder = functionTypeBuilder.addObject(Var.class);
					typeQualifierSuffix = extractVariableType(paramGenericType);
					isIncludeVariableAnnotation = true;
					labelPrefix = VAR_LABEL_PREFIX;
					labelSuffix = typeQualifierSuffix;

				} else if (Out.class.equals(paramType)) {
					// Output (from variable)
					parameterFactory = new ManagedFunctionOutParameterFactory(objectSequence.nextIndex());
					objectTypeBuilder = functionTypeBuilder.addObject(Var.class);
					typeQualifierSuffix = extractVariableType(paramGenericType);
					isIncludeVariableAnnotation = true;
					labelPrefix = VAR_LABEL_PREFIX;
					labelSuffix = typeQualifierSuffix;

				} else if (In.class.equals(paramType)) {
					// Input (from variable)
					parameterFactory = new ManagedFunctionInParameterFactory(objectSequence.nextIndex());
					objectTypeBuilder = functionTypeBuilder.addObject(Var.class);
					typeQualifierSuffix = extractVariableType(paramGenericType);
					isIncludeVariableAnnotation = true;
					labelPrefix = VAR_LABEL_PREFIX;
					labelSuffix = typeQualifierSuffix;

				} else {
					// Otherwise must be an dependency object
					parameterFactory = new ManagedFunctionObjectParameterFactory(objectSequence.nextIndex());
					objectTypeBuilder = functionTypeBuilder.addObject(paramType);
					typeQualifierSuffix = null;
					isIncludeVariableAnnotation = false;
					labelPrefix = "";
					labelSuffix = paramType.getName();
				}

				// Determine type qualifier
				String typeQualifier = null;
				for (Annotation annotation : allAnnotations) {

					// Add the annotation for the object
					objectTypeBuilder.addAnnotation(annotation);

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
						Class<? extends QualifierNameFactory> nameFactoryClass = qualifierAnnotation.nameFactory();
						QualifierNameFactory<Annotation> nameFactory = nameFactoryClass.getDeclaredConstructor()
								.newInstance();

						// Provide type qualifier
						typeQualifier = nameFactory.getQualifierName(annotation);
						objectTypeBuilder.setTypeQualifier(
								typeQualifier + (typeQualifierSuffix != null ? "-" + typeQualifierSuffix : ""));
					}
				}

				// Specify the label
				String label = (typeQualifier != null ? typeQualifier + "-" : "") + labelSuffix;
				objectTypeBuilder.setLabel(labelPrefix + label);

				// Determine if include variable annotation
				if (isIncludeVariableAnnotation) {
					objectTypeBuilder.addAnnotation(new VariableAnnotation(label));

					// Add qualifier for non-qualified variable
					if (typeQualifier == null) {
						objectTypeBuilder.setTypeQualifier(typeQualifierSuffix);
					}
				}

				// Enrich the object
				this.enrichManagedFunctionObjectType(paramType, paramGenericType, paramAnnotations, objectTypeBuilder);
			}

			// Load the parameter factory
			parameters[i] = parameterFactory;
		}

		// Define the escalation listing
		for (Class<?> escalationType : method.getExceptionTypes()) {
			functionTypeBuilder.addEscalation((Class<Throwable>) escalationType);
		}

		// Enrich the managed function
		this.enrichManagedFunctionType(new EnrichManagedFunctionTypeContext(methodName, method, instanceClass,
				methodObjectInstanceFactory, parameters, functionTypeBuilder));

		// Return the managed function builder
		return functionTypeBuilder;
	}

	/**
	 * Extracts the type from the variable.
	 * 
	 * @param variableGenericType Variable {@link Type}.
	 * @return Variable type.
	 */
	protected static String extractVariableType(Type variableGenericType) {
		if (variableGenericType instanceof ParameterizedType) {
			// Use generics to determine exact type
			ParameterizedType paramType = (ParameterizedType) variableGenericType;
			Type[] generics = paramType.getActualTypeArguments();
			return (generics.length > 0) ? generics[0].getTypeName() : Object.class.getName();
		} else {
			// Not parameterized, so raw object
			return Object.class.getName();
		}
	}

	/**
	 * Useful details regarding the {@link Method}.
	 */
	public abstract static class MethodContext {

		/**
		 * Name of {@link ManagedFunction} for the {@link Method}.
		 */
		private final String functionName;

		/**
		 * {@link Method}.
		 */
		private final Method method;

		/**
		 * {@link Class} for the instance containing the {@link Method}.
		 */
		private final Class<?> instanceClass;

		/**
		 * {@link MethodObjectInstanceFactory}. Will be <code>null</code> if static.
		 */
		private final MethodObjectInstanceFactory methodObjectInstanceFactory;

		/**
		 * Instantiate.
		 * 
		 * @param functionName                Name of {@link ManagedFunction} for the
		 *                                    {@link Method}.
		 * @param method                      {@link Method}.
		 * @param instanceClass               {@link Class} for the instance containing
		 *                                    the {@link Method}.
		 * @param methodObjectInstanceFactory {@link MethodObjectInstanceFactory}. Will
		 *                                    be <code>null</code> if static.
		 */
		protected MethodContext(String functionName, Method method, Class<?> instanceClass,
				MethodObjectInstanceFactory methodObjectInstanceFactory) {
			this.functionName = functionName;
			this.method = method;
			this.instanceClass = instanceClass;
			this.methodObjectInstanceFactory = methodObjectInstanceFactory;
		}

		/**
		 * Obtains the name of the {@link ManagedFunction} for the {@link Method}.
		 * 
		 * @return Name of the {@link ManagedFunction} for the {@link Method}.
		 */
		public String getFunctionName() {
			return this.functionName;
		}

		/**
		 * Obtains the {@link Method}.
		 * 
		 * @return {@link Method}.
		 */
		public Method getMethod() {
			return this.method;
		}

		/**
		 * Obtains the {@link Class} for the instance containing the {@link Method}.
		 * 
		 * @return {@link Class} for the instance containing the {@link Method}.
		 */
		public Class<?> getInstanceClass() {
			return this.instanceClass;
		}

		/**
		 * Obtains the {@link MethodObjectInstanceFactory}.
		 * 
		 * @return {@link MethodObjectInstanceFactory} or <code>null</code> if static
		 *         {@link Method}.
		 */
		public MethodObjectInstanceFactory getMethodObjectInstanceFactory() {
			return this.methodObjectInstanceFactory;
		}
	}

	/**
	 * Context for creating the {@link ManagedFunctionFactory}.
	 */
	public static class MethodManagedFunctionFactoryContext extends MethodContext {

		/**
		 * {@link ManagedFunctionParameterFactory} instances.
		 */
		private final ManagedFunctionParameterFactory[] parameters;

		/**
		 * Instantiate.
		 * 
		 * @param functionName                Name of {@link ManagedFunction} for the
		 *                                    {@link Method}.
		 * @param method                      {@link Method}.
		 * @param instanceClass               {@link Class} for the instance containing
		 *                                    the {@link Method}.
		 * @param methodObjectInstanceFactory {@link MethodObjectInstanceFactory}. Will
		 *                                    be <code>null</code> if static.
		 * @param parameters                  {@link ManagedFunctionParameterFactory}
		 *                                    instances.
		 */
		protected MethodManagedFunctionFactoryContext(String functionName, Method method, Class<?> instanceClass,
				MethodObjectInstanceFactory methodObjectInstanceFactory, ManagedFunctionParameterFactory[] parameters) {
			super(functionName, method, instanceClass, methodObjectInstanceFactory);
			this.parameters = parameters;
		}

		/**
		 * Obtains the {@link ManagedFunctionParameterFactory} instances.
		 * 
		 * @return {@link ManagedFunctionParameterFactory} instances.
		 */
		public ManagedFunctionParameterFactory[] getParameters() {
			return this.parameters;
		}
	}

	/**
	 * Context for creating the {@link ManagedFunctionTypeBuilder}.
	 */
	public static class MethodManagedFunctionTypeContext extends MethodContext {

		/**
		 * {@link ManagedFunctionFactory}.
		 */
		private final ManagedFunctionFactory<Indexed, Indexed> functionFactory;

		/**
		 * {@link FunctionNamespaceBuilder}.
		 */
		private final FunctionNamespaceBuilder namespaceBuilder;

		/**
		 * {@link Sequence} for {@link Object} indexes.
		 */
		private final Sequence objectSequence;

		/**
		 * {@link Sequence} for the {@link Flow} indexes.
		 */
		private final Sequence flowSequence;

		/**
		 * Instantiate.
		 * 
		 * @param functionName                Name of {@link ManagedFunction} for the
		 *                                    {@link Method}.
		 * @param method                      {@link Method}.
		 * @param instanceClass               {@link Class} for the instance containing
		 *                                    the {@link Method}.
		 * @param methodObjectInstanceFactory {@link MethodObjectInstanceFactory}. Will
		 *                                    be <code>null</code> if static.
		 * @param functionFactory             {@link ManagedFunctionFactory}.
		 * @param namespaceBuilder            {@link FunctionNamespaceBuilder}.
		 * @param objectSequence              {@link Sequence} for {@link Object}
		 *                                    indexes.
		 * @param flowSequence                {@link Sequence} for the {@link Flow}
		 *                                    indexes.
		 */
		public MethodManagedFunctionTypeContext(String functionName, Method method, Class<?> instanceClass,
				MethodObjectInstanceFactory methodObjectInstanceFactory,
				ManagedFunctionFactory<Indexed, Indexed> functionFactory, FunctionNamespaceBuilder namespaceBuilder,
				Sequence objectSequence, Sequence flowSequence) {
			super(functionName, method, instanceClass, methodObjectInstanceFactory);
			this.functionFactory = functionFactory;
			this.namespaceBuilder = namespaceBuilder;
			this.objectSequence = objectSequence;
			this.flowSequence = flowSequence;
		}

		/**
		 * Obtains the {@link ManagedFunctionFactory}.
		 * 
		 * @return {@link ManagedFunctionFactory}.
		 */
		public ManagedFunctionFactory<Indexed, Indexed> getFunctionFactory() {
			return functionFactory;
		}

		/**
		 * Obtains the {@link FunctionNamespaceBuilder}.
		 * 
		 * @return {@link FunctionNamespaceBuilder}.
		 */
		public FunctionNamespaceBuilder getNamespaceBuilder() {
			return namespaceBuilder;
		}

		/**
		 * Obtains the next {@link Object} index.
		 * 
		 * @return Next {@link Object} index.
		 */
		public int nextObjectIndex() {
			return objectSequence.nextIndex();
		}

		/**
		 * Obtains the next {@link Flow} index.
		 * 
		 * @return Next {@link Flow} index.
		 */
		public int nextFlowIndex() {
			return flowSequence.nextIndex();
		}
	}

	/**
	 * Context for creating the {@link ManagedFunctionTypeBuilder}.
	 */
	public static class EnrichManagedFunctionTypeContext extends MethodManagedFunctionFactoryContext {

		/**
		 * {@link ManagedFunctionTypeBuilder}.
		 */
		private final ManagedFunctionTypeBuilder<Indexed, Indexed> managedFunctionTypeBuilder;

		/**
		 * Instantiate.
		 * 
		 * @param functionName                Name of {@link ManagedFunction} for the
		 *                                    {@link Method}.
		 * @param method                      {@link Method}.
		 * @param instanceClass               {@link Class} for the instance containing
		 *                                    the {@link Method}.
		 * @param methodObjectInstanceFactory {@link MethodObjectInstanceFactory}. Will
		 *                                    be <code>null</code> if static.
		 * @param parameters                  {@link ManagedFunctionParameterFactory}
		 *                                    instances.
		 * @param functionType                {@link ManagedFunctionTypeBuilder}.
		 */
		public EnrichManagedFunctionTypeContext(String functionName, Method method, Class<?> instanceClass,
				MethodObjectInstanceFactory methodObjectInstanceFactory, ManagedFunctionParameterFactory[] parameters,
				ManagedFunctionTypeBuilder<Indexed, Indexed> functionType) {
			super(functionName, method, instanceClass, methodObjectInstanceFactory, parameters);
			this.managedFunctionTypeBuilder = functionType;
		}

		/**
		 * Obtains the {@link ManagedFunctionTypeBuilder}.
		 * 
		 * @return {@link ManagedFunctionTypeBuilder}.
		 */
		public ManagedFunctionTypeBuilder<Indexed, Indexed> getManagedFunctionTypeBuilder() {
			return managedFunctionTypeBuilder;
		}
	}

	/**
	 * Manufactures the {@link MethodObjectInstanceFactory}.
	 */
	public static interface MethodObjectInstanceManufacturer {

		/**
		 * Creates the {@link MethodObjectInstanceFactory}.
		 * 
		 * @return {@link MethodObjectInstanceFactory}.
		 */
		MethodObjectInstanceFactory createMethodObjectInstanceFactory();
	}

	/**
	 * Manufactures the {@link ManagedFunctionParameterFactory}.
	 */
	public static interface ParameterManufacturer {

		/**
		 * Creates the {@link ManagedFunctionParameterFactory}.
		 * 
		 * @param functionName        Name of the {@link ManagedFunction}.
		 * @param parameterType       Parameter type.
		 * @param functionTypeBuilder {@link ManagedFunctionTypeBuilder}.
		 * @param objectSequence      Object {@link Sequence}.
		 * @param flowSequence        Flow {@link Sequence}.
		 * @param sourceContext       {@link SourceContext}.
		 * @return {@link ManagedFunctionParameterFactory} or <code>null</code> if not
		 *         appropriate for this to manufacture a
		 *         {@link ManagedFunctionParameterFactory}.
		 * @throws Exception If fails to create the
		 *                   {@link ManagedFunctionParameterFactory}.
		 */
		ManagedFunctionParameterFactory createParameterFactory(String functionName, Class<?> parameterType,
				ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder, Sequence objectSequence,
				Sequence flowSequence, SourceContext sourceContext) throws Exception;
	}

	/**
	 * {@link ParameterManufacturer} for the {@link ManagedFunctionContext}.
	 */
	public static class ManagedFunctionContextParameterManufacturer implements ParameterManufacturer {
		@Override
		public ManagedFunctionParameterFactory createParameterFactory(String functionName, Class<?> parameterType,
				ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder, Sequence objectSequence,
				Sequence flowSequence, SourceContext sourceContext) {

			// Determine if managed function context
			if (ManagedFunctionContext.class.equals(parameterType)) {
				// Parameter is a managed function context
				return new ManagedFunctionContextParameterFactory();
			}

			// Not function context
			return null;
		}
	}

	/**
	 * {@link ParameterManufacturer} for an {@link AsynchronousFlow}.
	 */
	public static class AsynchronousFlowParameterManufacturer implements ParameterManufacturer {
		@Override
		public ManagedFunctionParameterFactory createParameterFactory(String functionName, Class<?> parameterType,
				ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder, Sequence objectSequence,
				Sequence flowSequence, SourceContext sourceContext) {

			// Determine if asynchronous flow
			if (AsynchronousFlow.class.equals(parameterType)) {
				// Parameter is an asynchronous flow
				return new ManagedFunctionAsynchronousFlowParameterFactory();
			}

			// Not function context
			return null;
		}
	}

	/**
	 * {@link ParameterManufacturer} for the {@link FlowInterface}.
	 */
	public static class FlowParameterManufacturer<A extends Annotation> implements ParameterManufacturer {

		/**
		 * {@link Class} of the {@link Annotation}.
		 */
		private final Class<A> annotationClass;

		/**
		 * Instantiate.
		 * 
		 * @param annotationClass {@link Class} of the {@link Annotation}.
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
				Sequence flowSequence, SourceContext sourceContext) throws Exception {

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
					.buildFlowParameterFactory(functionName, parameterType, flowSequence, flowRegistry, sourceContext);
			if (flowParameterFactory == null) {
				return null; // not flow interface
			}

			// Return wrapping managed function flow parameter factory
			return new ManagedFunctionFlowParameterFactory(flowParameterFactory);
		}
	}

}