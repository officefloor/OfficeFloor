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
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.internal.structure.EscalationFlow;
import net.officefloor.plugin.clazz.dependency.ClassDependencies;
import net.officefloor.plugin.clazz.dependency.ClassDependenciesContext;
import net.officefloor.plugin.clazz.dependency.ClassDependenciesManager;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.dependency.ClassItemIndex;
import net.officefloor.plugin.clazz.factory.ClassObjectFactory;
import net.officefloor.plugin.clazz.factory.ClassObjectManufacturer;
import net.officefloor.plugin.clazz.qualifier.TypeQualifierInterrogation;
import net.officefloor.plugin.clazz.state.StatePoint;

/**
 * Builder to wrap execution of a {@link Method} with a {@link ManagedFunction}.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodManagedFunctionBuilder {

	/**
	 * {@link Class} being interrogated for injection.
	 */
	private final Class<?> clazz;

	/**
	 * {@link FunctionNamespaceBuilder}.
	 */
	private final FunctionNamespaceBuilder namespaceBuilder;

	/**
	 * {@link ManagedFunctionSourceContext}.
	 */
	private final ManagedFunctionSourceContext context;

	/**
	 * Instantiate.
	 * 
	 * @param clazz            {@link Class} being interrogated for injection.
	 * @param namespaceBuilder {@link FunctionNamespaceBuilder}.
	 * @param context          {@link ManagedFunctionSourceContext}.
	 */
	public MethodManagedFunctionBuilder(Class<?> clazz, FunctionNamespaceBuilder namespaceBuilder,
			ManagedFunctionSourceContext context) {
		this.clazz = clazz;
		this.namespaceBuilder = namespaceBuilder;
		this.context = context;
	}

	/**
	 * Builds the {@link ManagedFunction}.
	 * 
	 * @param method {@link Method} for the {@link ManagedFunction}.
	 * @return {@link ManagedFunctionTypeBuilder} for the {@link Method}.
	 * @throws Exception If fails to create the {@link ManagedFunction} from the
	 *                   {@link Method}.
	 */
	public ManagedFunctionTypeBuilder<Indexed, Indexed> buildMethod(Method method) throws Exception {

		// Build and return with default object instantiation
		return this.buildMethod(method, (context) -> {

			// No object required for static method
			if (Modifier.isStatic(method.getModifiers())) {
				return new StaticMethodObjectFactory();
			}

			// Create the default object factory
			ClassObjectManufacturer manufacturer = new ClassObjectManufacturer(context.getClassDependencies(),
					context.getSourceContext());
			ClassObjectFactory objectFactory = manufacturer.constructClassObjectFactory(this.clazz);

			// Return default object instantiation
			return new DefaultMethodObjectFactory(objectFactory);
		});
	}

	/**
	 * Builds the {@link ManagedFunction}.
	 * 
	 * @param method                           {@link Method} for the
	 *                                         {@link ManagedFunction}.
	 * @param methodObjectInstanceManufacturer {@link MethodObjectManufacturer} to
	 *                                         customise the instantiation of the
	 *                                         {@link Object} to invoke the
	 *                                         {@link Method} against.
	 * @return {@link ManagedFunctionTypeBuilder} for the {@link Method}.
	 * @throws Exception If fails to create the {@link ManagedFunction} from the
	 *                   {@link Method}.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ManagedFunctionTypeBuilder<Indexed, Indexed> buildMethod(Method method,
			MethodObjectManufacturer methodObjectInstanceManufacturer) throws Exception {

		// Obtain details of the method
		String methodName = method.getName();
		Class<?>[] paramClasses = method.getParameterTypes();

		// Obtain the method annotations
		Annotation[] methodAnnotations = method.getAnnotations();

		// Define the return type
		Class<?> returnType = method.getReturnType();

		// Escalation types
		Map<Class<? extends Throwable>, ManagedFunctionEscalationTypeBuilder> escalationTypes = new HashMap<>();

		// Include method as function in type definition
		ManagedFunctionTypeBuilder<Indexed, Indexed> functionTypeBuilder = this.namespaceBuilder
				.addManagedFunctionType(methodName, Indexed.class, Indexed.class);

		// Enable obtaining qualifier
		TypeQualifierInterrogation qualification = new TypeQualifierInterrogation(this.context);

		// Enable creating parameter dependencies
		ClassDependenciesManager dependencies = ClassDependenciesManager.create(this.clazz, this.context,
				new ClassDependenciesContext() {

					@Override
					public ClassItemIndex addFlow(String flowName, Class<?> argumentType, Object[] annotations) {

						// Add the flow
						ManagedFunctionFlowTypeBuilder<Indexed> flow = functionTypeBuilder.addFlow();
						flow.setLabel(flowName);
						if (argumentType != null) {
							flow.setArgumentType(argumentType);
						}
						for (Object annotation : annotations) {
							flow.addAnnotation(annotation);
						}
						return ClassDependenciesManager.createClassItemIndex(flow.getIndex(), null);
					}

					@Override
					public ClassItemIndex addDependency(String dependencyName, String qualifier, Class<?> objectType,
							Object[] annotations) {

						// Add dependency
						ManagedFunctionObjectTypeBuilder<Indexed> object = functionTypeBuilder.addObject(objectType);
						object.setLabel(dependencyName);
						if (qualifier != null) {
							object.setTypeQualifier(qualifier);
						}
						for (Object annotation : annotations) {
							object.addAnnotation(annotation);
						}

						// Return the index
						return ClassDependenciesManager.createClassItemIndex(object.getIndex(),
								(annotation) -> object.addAnnotation(annotation));
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

		// Obtain the factory to obtain object instance (if not static)
		boolean isStatic = Modifier.isStatic(method.getModifiers());
		MethodObjectFactory methodObjectFactory;
		if (isStatic) {
			// Static method, so no object required
			methodObjectFactory = null;

		} else {
			// Non-static method, so must invoke method against object
			methodObjectFactory = methodObjectInstanceManufacturer.createMethodObjectInstanceFactory(
					new MethodObjectManufacturerContextImpl(methodName, method, dependencies, this.context));
		}

		// Obtain the parameter dependencies
		ClassDependencyFactory[] parameters = new ClassDependencyFactory[paramClasses.length];
		for (int i = 0; i < paramClasses.length; i++) {

			// Obtain the type qualification
			String qualifier = qualification.extractTypeQualifier(StatePoint.of(method, i));

			// Obtain factory to create parameter dependency
			parameters[i] = dependencies.createClassDependencyFactory(method, i, qualifier);
		}

		// Register the function factory
		MethodFunctionFactory functionFactory = new MethodFunctionFactory(methodObjectFactory, method, parameters);
		functionTypeBuilder.setFunctionFactory(functionFactory);

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

		// Define the escalation listing (avoiding duplicates)
		for (Class<?> escalationType : method.getExceptionTypes()) {
			if (!escalationTypes.containsKey(escalationType)) {
				functionTypeBuilder.addEscalation((Class<Throwable>) escalationType);
			}
		}

		// Return the managed function builder
		return functionTypeBuilder;
	}

	/**
	 * {@link MethodObjectManufacturerContext} implementation.
	 */
	private static class MethodObjectManufacturerContextImpl implements MethodObjectManufacturerContext {

		/**
		 * {@link Method}.
		 */
		private final Method method;

		/**
		 * Name of {@link ManagedFunction} for the {@link Method}.
		 */
		private final String functionName;

		/**
		 * {@link ClassDependenciesManager}.
		 */
		private final ClassDependenciesManager classDependencies;

		/**
		 * {@link SourceContext}.
		 */
		private final SourceContext sourceContext;

		/**
		 * Instantiate.
		 * 
		 * @param functionName  Name of {@link ManagedFunction} for the {@link Method}.
		 * @param method        {@link Method}.
		 * @param sourceContext {@link SourceContext}.
		 */
		private MethodObjectManufacturerContextImpl(String functionName, Method method,
				ClassDependenciesManager classDependencies, SourceContext sourceContext) {
			this.functionName = functionName;
			this.method = method;
			this.classDependencies = classDependencies;
			this.sourceContext = sourceContext;
		}

		/*
		 * ====================== MethodObjectManufacturerContext ======================
		 */

		@Override
		public Method getMethod() {
			return this.method;
		}

		@Override
		public String getFunctionName() {
			return this.functionName;
		}

		@Override
		public ClassDependencies getClassDependencies() {
			return this.classDependencies;
		}

		@Override
		public <E extends Throwable> void addEscalation(Class<E> escalationType) {
			this.classDependencies.addEscalation(escalationType);
		}

		@Override
		public SourceContext getSourceContext() {
			return this.sourceContext;
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
