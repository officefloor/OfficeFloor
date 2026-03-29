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

package net.officefloor.compile.test.managedfunction.clazz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.compile.test.managedfunction.MockAsynchronousFlow;
import net.officefloor.frame.api.OfficeFrame;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.method.MethodManagedFunctionBuilder;
import net.officefloor.plugin.clazz.method.MethodObjectFactory;

/**
 * <p>
 * Utility class for testing {@link MethodManagedFunctionBuilder}.
 * <p>
 * In particular, this is for ensuring extensions load and work as expected.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodManagedFunctionBuilderUtil {

	/**
	 * Creates the {@link FunctionNamespaceBuilder} to create the expected
	 * {@link FunctionNamespaceType}.
	 * 
	 * @return {@link FunctionNamespaceBuilder} to build the expected
	 *         {@link FunctionNamespaceType}.
	 */
	public static FunctionNamespaceBuilder createManagedFunctionTypeBuilder() {
		return ManagedFunctionLoaderUtil.createManagedFunctionTypeBuilder();
	}

	/**
	 * Convenience method to create a {@link FunctionNamespaceBuilder} for single
	 * {@link ManagedFunctionType}.
	 * 
	 * @param functionName Name of {@link ManagedFunctionType}.
	 * @param decorator    Optional decorator of {@link ManagedFunctionTypeBuilder}.
	 *                     May be <code>null</code>.
	 * @return {@link FunctionNamespaceBuilder}.
	 */
	public static FunctionNamespaceBuilder createManagedFunctionTypeBuilder(String functionName,
			Consumer<ManagedFunctionTypeBuilder<Indexed, Indexed>> decorator) {

		// Create the function
		FunctionNamespaceBuilder namespace = createManagedFunctionTypeBuilder();
		ManagedFunctionTypeBuilder<Indexed, Indexed> function = namespace.addManagedFunctionType(functionName,
				Indexed.class, Indexed.class);

		// Possibly decorate the function
		if (decorator != null) {
			decorator.accept(function);
		}

		// Return the function namespace
		return namespace;
	}

	/**
	 * Convenience means to obtain the {@link Method} from the {@link Class}.
	 * 
	 * @param <T>        Type of {@link Class}.
	 * @param methodName Name of {@link Method}.
	 * @return {@link Method}.
	 */
	public static <T> Function<Class<T>, Method> method(String methodName) {
		return (clazz) -> {
			for (Method method : clazz.getMethods()) {
				if (method.getName().equals(methodName)) {
					return method;
				}
			}
			fail("Can not find method " + methodName + " on class " + clazz.getName());
			throw new IllegalStateException("Should not reach this point");
		};
	}

	/**
	 * Convenience means to create {@link MethodObjectFactory}.
	 * 
	 * @param object Instance.
	 * @return {@link MethodObjectFactory}.
	 */
	public static MethodObjectFactory instance(Object object) {
		return (context) -> object;
	}

	/**
	 * Convenience method to build the {@link ManagedFunctionType}.
	 * 
	 * @param instance           Instance.
	 * @param methodName         Name of {@link Method}.
	 * @param epectedTypeBuilder Builds expected {@link ManagedFunctionType}.
	 * @param propertyNameValues {@link Property} name/value pairs.
	 * @return {@link ManagedFunctionType}.
	 * @throws Exception If fails to build the {@link ManagedFunctionType}.
	 */
	public static ManagedFunctionType<Indexed, Indexed> buildMethod(Object instance, String methodName,
			Consumer<ManagedFunctionTypeBuilder<Indexed, Indexed>> epectedTypeBuilder, String... propertyNameValues)
			throws Exception {
		return buildMethod(instance.getClass(), method(methodName), instance(instance),
				createManagedFunctionTypeBuilder(methodName, epectedTypeBuilder), propertyNameValues);
	}

	/**
	 * Convenience method to build the {@link ManagedFunctionType}.
	 * 
	 * @param clazz              {@link Class} containing the static {@link Method}.
	 * @param methodName         Name of the static {@link Method}.
	 * @param epectedTypeBuilder Builds expected {@link ManagedFunctionType}.
	 * @param propertyNameValues {@link Property} name/value pairs.
	 * @return {@link ManagedFunctionType}.
	 * @throws Exception If fails to build the {@link ManagedFunctionType}.
	 */
	public static ManagedFunctionType<Indexed, Indexed> buildStaticMethod(Class<?> clazz, String methodName,
			Consumer<ManagedFunctionTypeBuilder<Indexed, Indexed>> epectedTypeBuilder, String... propertyNameValues)
			throws Exception {
		return buildMethod(clazz, method(methodName), (context) -> null,
				createManagedFunctionTypeBuilder(methodName, epectedTypeBuilder), propertyNameValues);
	}

	/**
	 * Builds the {@link ManagedFunctionType}.
	 * 
	 * @param <T>                           Type of {@link Class}.
	 * @param clazz                         {@link Class}.
	 * @param methodFactory                 Factory to create the {@link Method}.
	 * @param objectInstanceFactory         {@link MethodObjectFactory}.
	 * @param expectedFunctionNamespaceType Expected
	 *                                      {@link FunctionNamespaceBuilder}.
	 * @param propertyNameValues            {@link Property} name/value pairs.
	 * @return {@link ManagedFunctionType}.
	 * @throws Exception If fails to build the {@link ManagedFunctionType}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> ManagedFunctionType<Indexed, Indexed> buildMethod(Class<T> clazz,
			Function<Class<T>, Method> methodFactory, MethodObjectFactory objectInstanceFactory,
			FunctionNamespaceBuilder expectedFunctionNamespaceType, String... propertyNameValues) throws Exception {

		// Obtain method
		Method method = methodFactory.apply(clazz);
		assertNotNull("No method supplied for class " + clazz.getName());

		// Create source to load method
		MethodManagedFunctionSource mos = new MethodManagedFunctionSource(method, objectInstanceFactory);

		// Load the function name space type (ensuring correct type)
		FunctionNamespaceType functionNamespaceType = ManagedFunctionLoaderUtil
				.validateManagedFunctionType(expectedFunctionNamespaceType, mos, propertyNameValues);

		// Return the managed function factory
		ManagedFunctionType<?, ?>[] managedFunctionTypes = functionNamespaceType.getManagedFunctionTypes();
		assertEquals("Should only have the one" + ManagedFunctionType.class.getSimpleName(), 1,
				managedFunctionTypes.length);
		return (ManagedFunctionType<Indexed, Indexed>) managedFunctionTypes[0];
	}

	/**
	 * {@link ManagedFunctionSource} to run the
	 * {@link MethodManagedFunctionBuilder}.
	 */
	private static class MethodManagedFunctionSource extends AbstractManagedFunctionSource {

		/**
		 * {@link Method}.
		 */
		private final Method method;

		/**
		 * {@link MethodObjectFactory}.
		 */
		private final MethodObjectFactory objectInstanceFactory;

		/**
		 * Instantiate.
		 * 
		 * @param method                {@link Method}.
		 * @param objectInstanceFactory {@link MethodObjectFactory}.
		 */
		public MethodManagedFunctionSource(Method method, MethodObjectFactory objectInstanceFactory) {
			this.method = method;
			this.objectInstanceFactory = objectInstanceFactory;
		}

		/*
		 * ================== ManagedFunctionSource =====================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification required
		}

		@Override
		public void sourceManagedFunctions(FunctionNamespaceBuilder functionNamespaceTypeBuilder,
				ManagedFunctionSourceContext context) throws Exception {

			// Build the method
			Class<?> clazz = this.method.getDeclaringClass();
			MethodManagedFunctionBuilder builder = new MethodManagedFunctionBuilder(clazz, functionNamespaceTypeBuilder,
					context);
			builder.buildMethod(this.method, (objectContext) -> this.objectInstanceFactory);
		}
	}

	/**
	 * Convenience method to build and run a {@link ManagedFunction}.
	 * 
	 * @param instance           Instance.
	 * @param methodName         Name of {@link Method}.
	 * @param epectedTypeBuilder Builds expected {@link ManagedFunctionType}.
	 * @param contextBuilder     Builds up the {@link ManagedFunctionContext}.
	 * @param propertyNameValues {@link Property} name/value pairs.
	 * @return {@link MethodResult}.
	 */
	public static MethodResult runMethod(Object instance, String methodName,
			Consumer<ManagedFunctionTypeBuilder<Indexed, Indexed>> epectedTypeBuilder,
			Consumer<ManagedFunctionContextBuilder> contextBuilder, String... propertyNameValues) {
		try {
			ManagedFunctionType<Indexed, Indexed> function = buildMethod(instance, methodName, epectedTypeBuilder,
					propertyNameValues);
			return runMethod(function, contextBuilder);
		} catch (Throwable ex) {
			throw OfficeFrameTestCase.fail(ex);
		}
	}

	/**
	 * Convenience method to build and run a {@link ManagedFunction} for static
	 * {@link Method}.
	 * 
	 * @param clazz              {@link Class} containing the static {@link Method}.
	 * @param methodName         Name of the static {@link Method}.
	 * @param epectedTypeBuilder Builds expected {@link ManagedFunctionType}.
	 * @param contextBuilder     Builds up the {@link ManagedFunctionContext}.
	 * @param propertyNameValues {@link Property} name/value pairs.
	 * @return {@link MethodResult}.
	 */
	public static MethodResult runStaticMethod(Class<?> clazz, String methodName,
			Consumer<ManagedFunctionTypeBuilder<Indexed, Indexed>> epectedTypeBuilder,
			Consumer<ManagedFunctionContextBuilder> contextBuilder, String... propertyNameValues) {
		try {
			ManagedFunctionType<Indexed, Indexed> function = buildStaticMethod(clazz, methodName, epectedTypeBuilder,
					propertyNameValues);
			return runMethod(function, contextBuilder);
		} catch (Throwable ex) {
			throw OfficeFrameTestCase.fail(ex);
		}
	}

	/**
	 * Runs the {@link ManagedFunction}.
	 * 
	 * @param functionType   {@link ManagedFunctionType}.
	 * @param contextBuilder Builds up the {@link ManagedFunctionContext}.
	 * @return {@link MethodResult}.
	 * @throws Throwable If fails to create {@link ManagedFunction}.
	 */
	public static MethodResult runMethod(ManagedFunctionType<Indexed, Indexed> functionType,
			Consumer<ManagedFunctionContextBuilder> contextBuilder) throws Throwable {

		// Create the logger
		Logger logger = OfficeFrame.getLogger(functionType.getFunctionName());

		// Setup the context
		MockManagedFunctionContext context = new MockManagedFunctionContext(logger);
		if (contextBuilder != null) {
			contextBuilder.accept(context);
		}

		// Run the function
		ManagedFunction<Indexed, Indexed> function = functionType.getManagedFunctionFactory().createManagedFunction();
		Throwable failure;
		try {
			function.execute(context);
			failure = null;
		} catch (AssertionError ex) {
			throw ex; // propagate assertion failures
		} catch (Throwable ex) {
			failure = ex;
		}

		// Return the result
		return new MethodResult(context, failure,
				context.asyncFlows.toArray(new MockAsynchronousFlow[context.asyncFlows.size()]),
				context.executorRunnables.toArray(new Runnable[context.executorRunnables.size()]));
	}

	/**
	 * Result of running the {@link ManagedFunction}.
	 */
	public static class MethodResult {

		/**
		 * {@link MockManagedFunctionContext}.
		 */
		private final MockManagedFunctionContext context;

		/**
		 * Possible failure.
		 */
		private final Throwable failure;

		/**
		 * Created {@link AsynchronousFlow} in running the {@link ManagedFunction}.
		 */
		private final MockAsynchronousFlow[] asyncFlows;

		/**
		 * {@link Runnable} instances passed to the {@link Executor}.
		 */
		private final Runnable[] executorRunnables;

		/**
		 * Instantiate.
		 * 
		 * @param context           {@link MockManagedFunctionContext}.
		 * @param failure           Possible failure.
		 * @param asyncFlows        Created {@link AsynchronousFlow} in running the
		 *                          {@link ManagedFunction}.
		 * @param executorRunnables {@link Runnable} instances passed to the
		 *                          {@link Executor}.
		 */
		private MethodResult(MockManagedFunctionContext context, Throwable failure, MockAsynchronousFlow[] asyncFlows,
				Runnable[] executorRunnables) {
			this.context = context;
			this.failure = failure;
			this.asyncFlows = asyncFlows;
			this.executorRunnables = executorRunnables;
		}

		/**
		 * Obtains the return value.
		 * 
		 * @return Return value.
		 */
		public Object getReturnValue() {
			if (this.failure != null) {
				fail("No return value as function threw " + this.failure.getMessage() + " ["
						+ this.failure.getClass().getName() + "]");
			}
			return this.context.nextFunctionArgument;
		}

		/**
		 * Obtains the possible failure.
		 * 
		 * @return Possible failure.
		 */
		public Throwable getFailure() {
			return this.failure;
		}

		/**
		 * Obtains the created {@link AsynchronousFlow} in running the
		 * {@link ManagedFunction}.
		 * 
		 * @return Created {@link AsynchronousFlow} in running the
		 *         {@link ManagedFunction}.
		 */
		public MockAsynchronousFlow[] getAsynchronousFlows() {
			return this.asyncFlows;
		}

		/**
		 * Obtains the {@link Runnable} instances passed to the {@link Executor}.
		 * 
		 * @return {@link Runnable} instances passed to the {@link Executor}.
		 */
		public Runnable[] getExecutorRunnables() {
			return this.executorRunnables;
		}
	}

	/**
	 * Builds the {@link ManagedFunctionContext}.
	 */
	public static interface ManagedFunctionContextBuilder {

		/**
		 * Specifies the object for index.
		 * 
		 * @param dependencyIndex Index of object.
		 * @param object          Object for the index.
		 */
		void setObject(int dependencyIndex, Object object);

		/**
		 * Specifies the {@link ManagedFunctionFlowHandler} for {@link Flow} index.
		 * 
		 * @param flowIndex Index of the {@link Flow}.
		 * @param handler   {@link ManagedFunctionFlowHandler} for the {@link Flow}.
		 */
		void setFlow(int flowIndex, ManagedFunctionFlowHandler handler);

		/**
		 * Specifies the {@link ManagedFunctionFlowHandler} for invoking another
		 * {@link ManagedFunction} reflectively.
		 * 
		 * @param functionName Name of the {@link ManagedFunction}.
		 * @param handler      {@link ManagedFunctionFlowHandler} for the {@link Flow}.
		 */
		void setFlow(String functionName, ManagedFunctionFlowHandler handler);
	}

	/**
	 * Handles {@link Flow}.
	 */
	@FunctionalInterface
	public static interface ManagedFunctionFlowHandler {

		/**
		 * Handles the {@link Flow}.
		 * 
		 * @param parameter Parameter to the {@link Flow}.
		 * @param callback  {@link FlowCallback} to the {@link Flow}.
		 */
		void handle(Object parameter, FlowCallback callback);
	}

	/**
	 * Mock {@link ManagedFunctionContext}.
	 */
	private static class MockManagedFunctionContext
			implements ManagedFunctionContext<Indexed, Indexed>, ManagedFunctionContextBuilder {

		/**
		 * {@link Map} of object to index.
		 */
		private final Map<Integer, Object> objects = new HashMap<>();

		/**
		 * {@link Map} of {@link ManagedFunctionFlowHandler} to index.
		 */
		private final Map<Integer, ManagedFunctionFlowHandler> indexedFlows = new HashMap<>();

		/**
		 * {@link Map} of {@link ManagedFunctionFlowHandler} to named {@link Flow}.
		 */
		private final Map<String, ManagedFunctionFlowHandler> namedFlows = new HashMap<>();

		/**
		 * Listing of {@link MockAsynchronousFlow} instances.
		 */
		private final List<MockAsynchronousFlow> asyncFlows = new LinkedList<>();

		/**
		 * Listing of {@link Runnable} instances passed to the {@link Executor}.
		 */
		private final List<Runnable> executorRunnables = new LinkedList<>();

		/**
		 * {@link Logger}.
		 */
		private final Logger logger;

		/**
		 * Argument for the next {@link ManagedFunction}.
		 */
		private volatile Object nextFunctionArgument = null;

		/**
		 * Instantiate.
		 * 
		 * @param logger {@link Logger}.
		 */
		private MockManagedFunctionContext(Logger logger) {
			this.logger = logger;
		}

		/*
		 * =============== ManagedFunctionContextBuilder ===========
		 */

		@Override
		public Logger getLogger() {
			return this.logger;
		}

		@Override
		public void setObject(int dependencyIndex, Object object) {
			this.objects.put(dependencyIndex, object);
		}

		@Override
		public void setFlow(int flowIndex, ManagedFunctionFlowHandler handler) {
			this.indexedFlows.put(flowIndex, handler);
		}

		@Override
		public void setFlow(String flowName, ManagedFunctionFlowHandler handler) {
			this.namedFlows.put(flowName, handler);
		}

		/*
		 * ================== ManagedFunctionContext ===================
		 */

		@Override
		public Object getObject(Indexed key) {
			fail("Should not getObject by enum");
			throw new IllegalStateException("Should not reach here");
		}

		@Override
		public Object getObject(int dependencyIndex) {
			Object object = this.objects.get(dependencyIndex);
			assertNotNull("No object configured for index " + dependencyIndex, object);
			return object;
		}

		@Override
		public void doFlow(Indexed key, Object parameter, FlowCallback callback) {
			fail("Should execute flow by enum");
			throw new IllegalStateException("Should not reach here");
		}

		@Override
		public void doFlow(int flowIndex, Object parameter, FlowCallback callback) {
			ManagedFunctionFlowHandler handler = this.indexedFlows.get(flowIndex);
			assertNotNull("No flow handler for index " + flowIndex, handler);
			handler.handle(parameter, callback);
		}

		@Override
		public void doFlow(String functionName, Object parameter, FlowCallback callback)
				throws UnknownFunctionException, InvalidParameterTypeException {
			ManagedFunctionFlowHandler handler = this.namedFlows.get(functionName);
			assertNotNull("No flow handler for named flow " + functionName, handler);
			handler.handle(parameter, callback);
		}

		@Override
		public AsynchronousFlow createAsynchronousFlow() {
			MockAsynchronousFlow asyncFlow = new MockAsynchronousFlow();
			this.asyncFlows.add(asyncFlow);
			return asyncFlow;
		}

		@Override
		public Executor getExecutor() {
			return (runnable) -> this.executorRunnables.add(runnable);
		}

		@Override
		public void setNextFunctionArgument(Object argument) {
			this.nextFunctionArgument = argument;
		}
	}

}
