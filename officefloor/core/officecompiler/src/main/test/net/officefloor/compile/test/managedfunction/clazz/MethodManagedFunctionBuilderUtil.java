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
package net.officefloor.compile.test.managedfunction.clazz;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import net.officefloor.compile.managedfunction.FunctionNamespaceType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.managedfunction.source.FunctionNamespaceBuilder;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSource;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionSourceContext;
import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionTypeBuilder;
import net.officefloor.compile.spi.managedfunction.source.impl.AbstractManagedFunctionSource;
import net.officefloor.compile.test.managedfunction.ManagedFunctionLoaderUtil;
import net.officefloor.compile.test.managedfunction.MockAsynchronousFlow;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.FlowCallback;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.manage.InvalidParameterTypeException;
import net.officefloor.frame.api.manage.UnknownFunctionException;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedfunction.method.MethodManagedFunctionBuilder;
import net.officefloor.plugin.managedfunction.method.MethodObjectInstanceFactory;

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
		ManagedFunctionTypeBuilder<Indexed, Indexed> function = namespace.addManagedFunctionType(functionName, null,
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
	 * Convenience means to create {@link MethodObjectInstanceFactory}.
	 * 
	 * @param object Instance.
	 * @return {@link MethodObjectInstanceFactory}.
	 */
	public static MethodObjectInstanceFactory instance(Object object) {
		return (context) -> object;
	}

	/**
	 * Convenience method to build the {@link ManagedFunctionType}.
	 * 
	 * @param instance           Instance.
	 * @param methodName         Name of {@link Method}.
	 * @param epectedTypeBuilder Builds expected {@link ManagedFunctionType}.
	 * @return {@link ManagedFunctionType}.
	 * @throws Exception If fails to build the {@link ManagedFunctionType}.
	 */
	public static ManagedFunctionType<Indexed, Indexed> buildMethod(Object instance, String methodName,
			Consumer<ManagedFunctionTypeBuilder<Indexed, Indexed>> epectedTypeBuilder) throws Exception {
		return buildMethod(instance.getClass(), method(methodName), instance(instance),
				createManagedFunctionTypeBuilder(methodName, epectedTypeBuilder));
	}

	/**
	 * Convenience method to build the {@link ManagedFunctionType}.
	 * 
	 * @param clazz              {@link Class} containing the static {@link Method}.
	 * @param methodName         Name of the static {@link Method}.
	 * @param epectedTypeBuilder Builds expected {@link ManagedFunctionType}.
	 * @return {@link ManagedFunctionType}.
	 * @throws Exception If fails to build the {@link ManagedFunctionType}.
	 */
	public static ManagedFunctionType<Indexed, Indexed> buildStaticMethod(Class<?> clazz, String methodName,
			Consumer<ManagedFunctionTypeBuilder<Indexed, Indexed>> epectedTypeBuilder) throws Exception {
		return buildMethod(clazz, method(methodName), (context) -> null,
				createManagedFunctionTypeBuilder(methodName, epectedTypeBuilder));
	}

	/**
	 * Builds the {@link ManagedFunctionType}.
	 * 
	 * @param <T>                           Type of {@link Class}.
	 * @param clazz                         {@link Class}.
	 * @param methodFactory                 Factory to create the {@link Method}.
	 * @param objectInstanceFactory         {@link MethodObjectInstanceFactory}.
	 * @param expectedFunctionNamespaceType Expected
	 *                                      {@link FunctionNamespaceBuilder}.
	 * @return {@link ManagedFunctionType}.
	 * @throws Exception If fails to build the {@link ManagedFunctionType}.
	 */
	@SuppressWarnings("unchecked")
	public static <T> ManagedFunctionType<Indexed, Indexed> buildMethod(Class<T> clazz,
			Function<Class<T>, Method> methodFactory, MethodObjectInstanceFactory objectInstanceFactory,
			FunctionNamespaceBuilder expectedFunctionNamespaceType) throws Exception {

		// Obtain method
		Method method = methodFactory.apply(clazz);
		assertNotNull("No method supplied for class " + clazz.getName());

		// Create source to load method
		MethodManagedFunctionSource mos = new MethodManagedFunctionSource(method, clazz, objectInstanceFactory);

		// Load the function name space type (ensuring correct type)
		FunctionNamespaceType functionNamespaceType = ManagedFunctionLoaderUtil
				.validateManagedFunctionType(expectedFunctionNamespaceType, mos);

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
		 * Instance {@link Class}.
		 */
		private final Class<?> instanceClass;

		/**
		 * {@link MethodObjectInstanceFactory}.
		 */
		private final MethodObjectInstanceFactory objectInstanceFactory;

		/**
		 * Instantiate.
		 * 
		 * @param method                {@link Method}.
		 * @param instanceClass         Instance {@link Class}.
		 * @param objectInstanceFactory {@link MethodObjectInstanceFactory}.
		 */
		public MethodManagedFunctionSource(Method method, Class<?> instanceClass,
				MethodObjectInstanceFactory objectInstanceFactory) {
			this.method = method;
			this.instanceClass = instanceClass;
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
			MethodManagedFunctionBuilder builder = new MethodManagedFunctionBuilder();
			builder.buildMethod(this.method, this.instanceClass, () -> this.objectInstanceFactory,
					functionNamespaceTypeBuilder, context);
		}
	}

	/**
	 * Convenience method to build and run a {@link ManagedFunction}.
	 * 
	 * @param instance           Instance.
	 * @param methodName         Name of {@link Method}.
	 * @param epectedTypeBuilder Builds expected {@link ManagedFunctionType}.
	 * @param contextBuilder     Builds up the {@link ManagedFunctionContext}.
	 * @return {@link MethodResult}.
	 */
	public static MethodResult runMethod(Object instance, String methodName,
			Consumer<ManagedFunctionTypeBuilder<Indexed, Indexed>> epectedTypeBuilder,
			Consumer<ManagedFunctionContextBuilder> contextBuilder) {
		try {
			ManagedFunctionType<Indexed, Indexed> function = buildMethod(instance, methodName, epectedTypeBuilder);
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
	 * @return {@link MethodResult}.
	 */
	public static MethodResult runStaticMethod(Class<?> clazz, String methodName,
			Consumer<ManagedFunctionTypeBuilder<Indexed, Indexed>> epectedTypeBuilder,
			Consumer<ManagedFunctionContextBuilder> contextBuilder) {
		try {
			ManagedFunctionType<Indexed, Indexed> function = buildStaticMethod(clazz, methodName, epectedTypeBuilder);
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

		// Setup the context
		MockManagedFunctionContext context = new MockManagedFunctionContext();
		if (contextBuilder != null) {
			contextBuilder.accept(context);
		}

		// Run the function
		ManagedFunction<Indexed, Indexed> function = functionType.getManagedFunctionFactory().createManagedFunction();
		Object result;
		Throwable failure;
		try {
			result = function.execute(context);
			failure = null;
		} catch (AssertionError ex) {
			throw ex; // propagate assertion failures
		} catch (Throwable ex) {
			result = null;
			failure = ex;
		}

		// Return the result
		return new MethodResult(result, failure,
				context.asyncFlows.toArray(new MockAsynchronousFlow[context.asyncFlows.size()]));
	}

	/**
	 * Result of running the {@link ManagedFunction}.
	 */
	public static class MethodResult {

		/**
		 * Return value.
		 */
		private final Object returnValue;

		/**
		 * Possible failure.
		 */
		private final Throwable failure;

		/**
		 * Created {@link AsynchronousFlow} in running the {@link ManagedFunction}.
		 */
		private final MockAsynchronousFlow[] asyncFlows;

		/**
		 * Instantiate.
		 * 
		 * @param returnValue Return value.
		 * @param failure     Possible failure.
		 * @param asyncFlows  Created {@link AsynchronousFlow} in running the
		 *                    {@link ManagedFunction}.
		 */
		private MethodResult(Object returnValue, Throwable failure, MockAsynchronousFlow[] asyncFlows) {
			this.returnValue = returnValue;
			this.failure = failure;
			this.asyncFlows = asyncFlows;
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
			return this.returnValue;
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

		/*
		 * =============== ManagedFunctionContextBuilder ===========
		 */

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
	}

}