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

package net.officefloor.plugin.clazz.method;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionEscalationTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionFlowTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.escalate.Escalation;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionFactory;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.plugin.clazz.NonFunctionMethod;
import net.officefloor.plugin.clazz.Sequence;
import net.officefloor.plugin.clazz.dependency.ClassDependencies;
import net.officefloor.plugin.clazz.dependency.ClassDependenciesContext;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogation;
import net.officefloor.plugin.clazz.state.StatePoint;

/**
 * Builder to wrap execution of a {@link Method} with a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodManagedFunctionBuilder {

	/**
	 * Allows overriding the creation of the {@link ManagedFunctionFactory}.
	 * 
	 * @param context {@link MethodManagedFunctionFactoryContext}.
	 * @return {@link MethodFunctionFactory}.
	 * @throws Exception If fails to create {@link ManagedFunctionFactory}.
	 */
	protected MethodFunctionFactory createManagedFunctionFactory(MethodManagedFunctionFactoryContext context)
			throws Exception {
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
	 * @param annotations        {@link Annotation} instances.
	 * @param functionObjectType {@link ManagedFunctionObjectTypeBuilder}.
	 */
	protected void enrichManagedFunctionObjectType(Class<?> objectType, Object[] annotations,
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
	 * @param methodObjectInstanceManufacturer {@link MethodObjectInstanceManufacturer}.
	 * @param namespaceBuilder                 {@link FunctionNamespaceBuilder}.
	 * @param context                          {@link ManagedFunctionSourceContext}.
	 * @return {@link ManagedFunctionTypeBuilder} for the {@link Method} or
	 *         <code>null</code> if can not to be a {@link ManagedFunction}.
	 * @throws Exception If fails to create the {@link ManagedFunction} from the
	 *                   {@link Method}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedFunctionTypeBuilder<Indexed, Indexed> buildMethod(Method method,
			MethodObjectInstanceManufacturer methodObjectInstanceManufacturer,
			FunctionNamespaceBuilder namespaceBuilder, ManagedFunctionSourceContext context) throws Exception {

		// Obtain details of the method
		String methodName = method.getName();
		Class<?>[] paramClasses = method.getParameterTypes();

		// Create parameters to method to be populated
		ClassDependencyFactory[] parameters = new ClassDependencyFactory[paramClasses.length];

		// Create the sequences for indexes to the objects and flows
		Sequence objectSequence = new Sequence();
		Sequence flowSequence = new Sequence();

		// Obtain the factory to obtain object instance (if not static)
		boolean isStatic = Modifier.isStatic(method.getModifiers());
		MethodObjectInstanceFactory methodObjectInstanceFactory = null;
		if (!isStatic) {
			methodObjectInstanceFactory = methodObjectInstanceManufacturer.createMethodObjectInstanceFactory();
		}

		// Obtain the method annotations
		Annotation[] methodAnnotations = method.getAnnotations();

		// Define the return type
		Class<?> returnType = method.getReturnType();

		// Escalation types
		Map<Class<? extends Throwable>, ManagedFunctionEscalationTypeBuilder> escalationTypes = new HashMap<>();

		// Create the function factory
		MethodFunctionFactory functionFactory = this.createManagedFunctionFactory(
				new MethodManagedFunctionFactoryContext(methodName, method, methodObjectInstanceFactory, parameters));

		// Include method as function in type definition
		ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder = this
				.addManagedFunctionType(new MethodManagedFunctionTypeContext(methodName, method,
						methodObjectInstanceFactory, functionFactory, namespaceBuilder, objectSequence, flowSequence));

		// Determine if translate return type
		// Note: even if void, allows successful after method execution
		MethodReturnTranslator returnTranslator = null;
		MethodReturnManufacturerContextImpl<?> returnContext = new MethodReturnManufacturerContextImpl<>(returnType,
				methodAnnotations, methodName, method, functionTypeBuilder, escalationTypes, context);
		FOUND_TRANSLATOR: for (MethodReturnManufacturer manufacturer : context
				.loadOptionalServices(MethodReturnManufacturerServiceFactory.class)) {
			returnTranslator = manufacturer.createReturnTranslator(returnContext);
			if (returnTranslator != null) {

				// Use the return translator
				functionFactory.setMethodReturnTranslator(returnTranslator);

				// Determine if translate return
				if (returnContext.isTranslatedReturn) {
					returnType = returnContext.translatedReturnClass;
				}

				// Found translator
				break FOUND_TRANSLATOR;
			}
		}

		// Load return type if not void
		if ((returnType != null) && (!Void.TYPE.equals(returnType))) {
			functionTypeBuilder.setReturnType(returnType);
		}

		// Load the function annotations
		for (Annotation annotation : methodAnnotations) {
			functionTypeBuilder.addAnnotation(annotation);
		}

		// Enable obtaining qualifier
		TypeQualifierInterrogation qualification = new TypeQualifierInterrogation(context);

		// Enable creating parameter dependencies
		ClassDependencies dependencies = new ClassDependencies(context.getName(), context.getLogger(), context,
				new ClassDependenciesContext() {

					@Override
					public int addFlow(String flowName, Class<?> argumentType, Object[] annotations) {

						// Add the flow
						ManagedFunctionFlowTypeBuilder<Indexed> flow = functionTypeBuilder.addFlow();
						flow.setLabel(flowName);
						if (argumentType != null) {
							flow.setArgumentType(argumentType);
						}
						return flow.getIndex();
					}

					@Override
					public int addDependency(String qualifier, Class<?> objectType, Object[] annotations) {

						// Add dependency
						String label = ClassDependencies.getDependencyName(qualifier, objectType);
						ManagedFunctionObjectTypeBuilder<Indexed> object = functionTypeBuilder.addObject(objectType);
						object.setLabel(label);
						if (qualifier != null) {
							object.setTypeQualifier(qualifier);
						}
						for (Object annotation : annotations) {
							object.addAnnotation(annotation);
						}

						// Enrich the object
						MethodManagedFunctionBuilder.this.enrichManagedFunctionObjectType(objectType, annotations,
								object);

						// Return the index
						return object.getIndex();
					}

					@Override
					public void addEscalation(Class<? extends Throwable> escalationType) {
						ManagedFunctionEscalationTypeBuilder escalation = functionTypeBuilder
								.addEscalation(escalationType);
						escalationTypes.put(escalationType, escalation);
					}

					@Override
					public void addAnnotation(Object annotation) {
						functionTypeBuilder.addAnnotation(annotation);
					}
				});

		// Obtain the parameter dependencies
		for (int i = 0; i < paramClasses.length; i++) {

			// Obtain the type qualification
			String qualifier = qualification.extractTypeQualifier(StatePoint.of(method, i));

			// Obtain factory to create parameter dependency
			parameters[i] = dependencies.createClassDependencyFactory(method, i, qualifier);
		}

		// Define the escalation listing (avoiding duplicates)
		for (Class<?> escalationType : method.getExceptionTypes()) {
			if (!escalationTypes.containsKey(escalationType)) {
				functionTypeBuilder.addEscalation((Class<Throwable>) escalationType);
			}
		}

		// Enrich the managed function
		this.enrichManagedFunctionType(new EnrichManagedFunctionTypeContext(methodName, method,
				methodObjectInstanceFactory, parameters, functionTypeBuilder));

		// Return the managed function builder
		return functionTypeBuilder;
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
		 * {@link MethodObjectInstanceFactory}. Will be <code>null</code> if static.
		 */
		private final MethodObjectInstanceFactory methodObjectInstanceFactory;

		/**
		 * Instantiate.
		 * 
		 * @param functionName                Name of {@link ManagedFunction} for the
		 *                                    {@link Method}.
		 * @param method                      {@link Method}.
		 * @param methodObjectInstanceFactory {@link MethodObjectInstanceFactory}. Will
		 *                                    be <code>null</code> if static.
		 */
		protected MethodContext(String functionName, Method method,
				MethodObjectInstanceFactory methodObjectInstanceFactory) {
			this.functionName = functionName;
			this.method = method;
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
		 * {@link ClassDependencyFactory} instances.
		 */
		private final ClassDependencyFactory[] parameters;

		/**
		 * Instantiate.
		 * 
		 * @param functionName                Name of {@link ManagedFunction} for the
		 *                                    {@link Method}.
		 * @param method                      {@link Method}.
		 * @param methodObjectInstanceFactory {@link MethodObjectInstanceFactory}. Will
		 *                                    be <code>null</code> if static.
		 * @param parameters                  {@link ClassDependencyFactory} instances.
		 */
		protected MethodManagedFunctionFactoryContext(String functionName, Method method,
				MethodObjectInstanceFactory methodObjectInstanceFactory, ClassDependencyFactory[] parameters) {
			super(functionName, method, methodObjectInstanceFactory);
			this.parameters = parameters;
		}

		/**
		 * Obtains the {@link ClassDependencyFactory} instances.
		 * 
		 * @return {@link ClassDependencyFactory} instances.
		 */
		public ClassDependencyFactory[] getParameters() {
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
		 * @param methodObjectInstanceFactory {@link MethodObjectInstanceFactory}. Will
		 *                                    be <code>null</code> if static.
		 * @param functionFactory             {@link ManagedFunctionFactory}.
		 * @param namespaceBuilder            {@link FunctionNamespaceBuilder}.
		 * @param objectSequence              {@link Sequence} for {@link Object}
		 *                                    indexes.
		 * @param flowSequence                {@link Sequence} for the {@link Flow}
		 *                                    indexes.
		 */
		public MethodManagedFunctionTypeContext(String functionName, Method method,
				MethodObjectInstanceFactory methodObjectInstanceFactory,
				ManagedFunctionFactory<Indexed, Indexed> functionFactory, FunctionNamespaceBuilder namespaceBuilder,
				Sequence objectSequence, Sequence flowSequence) {
			super(functionName, method, methodObjectInstanceFactory);
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
		 * @param methodObjectInstanceFactory {@link MethodObjectInstanceFactory}. Will
		 *                                    be <code>null</code> if static.
		 * @param parameters                  {@link ClassDependencyFactory} instances.
		 * @param functionType                {@link ManagedFunctionTypeBuilder}.
		 */
		public EnrichManagedFunctionTypeContext(String functionName, Method method,
				MethodObjectInstanceFactory methodObjectInstanceFactory, ClassDependencyFactory[] parameters,
				ManagedFunctionTypeBuilder<Indexed, Indexed> functionType) {
			super(functionName, method, methodObjectInstanceFactory, parameters);
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
	 * {@link MethodReturnManufacturerContext} implementation.
	 * 
	 * @author Daniel Sagenschneider
	 */
	private class MethodReturnManufacturerContextImpl<T> implements MethodReturnManufacturerContext<T> {

		/**
		 * {@link Method} return type.
		 */
		private final Class<?> returnClass;

		/**
		 * {@link Method} {@link Annotation} instances.
		 */
		private final Annotation[] methodAnnotations;

		/**
		 * Name of {@link ManagedFunction}.
		 */
		private final String functionName;

		/**
		 * {@link Method}.
		 */
		private final Method method;

		/**
		 * {@link SourceContext}.
		 */
		private final SourceContext sourceContext;

		/**
		 * Translated return {@link Class}.
		 */
		private Class<? super T> translatedReturnClass = null;

		/**
		 * Indicates if there is a translated return. Allows to specify
		 * <code>null</code> translated return.
		 */
		private boolean isTranslatedReturn = false;

		/**
		 * {@link ManagedFunctionTypeBuilder}.
		 */
		private final ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder;

		/**
		 * {@link Map} of {@link Throwable} type to
		 * {@link ManagedFunctionEscalationTypeBuilder}.
		 */
		private final Map<Class<? extends Throwable>, ManagedFunctionEscalationTypeBuilder> escalationTypes;

		/**
		 * Instantiate.
		 * 
		 * @param returnClass         {@link Method} return type.
		 * @param methodAnnotations   {@link Method} {@link Annotation} instances.
		 * @param functionName        Name of {@link ManagedFunction}.
		 * @param method              {@link Method}.
		 * @param functionTypeBuilder {@link ManagedFunctionTypeBuilder}.
		 * @param escalationTypes     {@link Map} of {@link Throwable} type to
		 *                            {@link ManagedFunctionEscalationTypeBuilder}.
		 * @param sourceContext       {@link SourceContext}.
		 */
		public MethodReturnManufacturerContextImpl(Class<?> returnClass, Annotation[] methodAnnotations,
				String functionName, Method method, ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder,
				Map<Class<? extends Throwable>, ManagedFunctionEscalationTypeBuilder> escalationTypes,
				SourceContext sourceContext) {
			this.returnClass = returnClass;
			this.methodAnnotations = methodAnnotations;
			this.functionName = functionName;
			this.method = method;
			this.functionTypeBuilder = functionTypeBuilder;
			this.escalationTypes = escalationTypes;
			this.sourceContext = sourceContext;
		}

		/*
		 * =================== MethodReturnManufacturerContext =================
		 */

		@Override
		public Class<?> getReturnClass() {
			return this.returnClass;
		}

		@Override
		public void setTranslatedReturnClass(Class<? super T> translatedReturnClass) {
			this.translatedReturnClass = translatedReturnClass;
			this.isTranslatedReturn = true;
		}

		@Override
		public Annotation[] getMethodAnnotations() {
			return this.methodAnnotations;
		}

		@Override
		public String getFunctionName() {
			return this.functionName;
		}

		@Override
		public Method getMethod() {
			return this.method;
		}

		@Override
		public <E extends Throwable> ManagedFunctionEscalationTypeBuilder addEscalation(Class<E> escalationType) {
			return MethodManagedFunctionBuilder.addEscalation(escalationType, functionTypeBuilder,
					this.escalationTypes);
		}

		@Override
		public SourceContext getSourceContext() {
			return this.sourceContext;
		}
	}

	/**
	 * Adds a {@link ManagedFunctionEscalationTypeBuilder} to the
	 * {@link ManagedFunctionTypeBuilder} definition only once.
	 * 
	 * @param <E>                 {@link Escalation} type.
	 * @param escalationType      Type to be handled by an {@link EscalationFlow}.
	 * @param functionTypeBuilder {@link ManagedFunctionTypeBuilder}.
	 * @param escalationTypes     {@link Map} of {@link Throwable} type to
	 *                            {@link ManagedFunctionEscalationTypeBuilder}.
	 * @return {@link ManagedFunctionEscalationTypeBuilder} to provide the
	 *         <code>type definition</code>.
	 */
	private static <E extends Throwable> ManagedFunctionEscalationTypeBuilder addEscalation(Class<E> escalationType,
			ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder,
			Map<Class<? extends Throwable>, ManagedFunctionEscalationTypeBuilder> escalationTypes) {

		// Single builder per escalation type
		ManagedFunctionEscalationTypeBuilder builder = escalationTypes.get(escalationType);
		if (builder == null) {
			builder = functionTypeBuilder.addEscalation(escalationType);
			escalationTypes.put(escalationType, builder);
		}
		return builder;
	}

}
