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

package net.officefloor.plugin.managedfunction.method;

import java.lang.reflect.Method;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil.MethodResult;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.section.clazz.Next;

/**
 * Tests the {@link MethodManagedFunctionBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodManagedFunctionBuilderTest extends OfficeFrameTestCase {

	/**
	 * Ensure can invoke {@link Method} with no parameters and no return.
	 */
	public void testNoParametersAndNoReturn() {
		NoParametersAndNoReturnFunction instance = new NoParametersAndNoReturnFunction();
		MethodResult result = MethodManagedFunctionBuilderUtil.runMethod(instance, "noParametersAndNoReturn", null,
				null);
		assertNull("Should be no return value", result.getReturnValue());
		assertTrue("Should run method", instance.isRun);
	}

	public static class NoParametersAndNoReturnFunction {

		public boolean isRun = false;

		public void noParametersAndNoReturn() {
			isRun = true;
		}
	}

	/**
	 * Ensure can obtain default return value.
	 */
	public void testReturnValue() {
		MethodResult result = MethodManagedFunctionBuilderUtil.runMethod(new ReturnValueFunction(), "returnValue",
				(type) -> type.setReturnType(String.class), null);
		assertEquals("Incorrect return value", ReturnValueFunction.RETURN_VALUE, result.getReturnValue());
	}

	public static class ReturnValueFunction {

		public static final String RETURN_VALUE = "RETURN";

		public String returnValue() {
			return RETURN_VALUE;
		}
	}

	/**
	 * Ensure invoke static {@link Method}.
	 */
	public void testStatic() {
		StaticFunction.isRun = false; // ensure correct state
		MethodResult result = MethodManagedFunctionBuilderUtil.runStaticMethod(StaticFunction.class, "staticMethod",
				null, null);
		assertNull("Should be no return value", result.getReturnValue());
		assertTrue("Should run method", StaticFunction.isRun);
	}

	public static class StaticFunction {

		public static boolean isRun = false;

		public static void staticMethod() {
			isRun = true;
		}
	}

	/**
	 * Ensure inject dependency.
	 */
	public void testDependency() {
		DependencyFunction dependency = new DependencyFunction();
		final String object = "DEPENDENCY";
		MethodManagedFunctionBuilderUtil.runMethod(dependency, "method", (type) -> {
			type.addObject(String.class).setLabel(String.class.getName());
		}, (context) -> {
			context.setObject(0, object);
		});
		assertEquals("Incorrect dependency", object, dependency.object);
	}

	public static class DependencyFunction {

		public String object = null;

		public void method(String dependency) {
			this.object = dependency;
		}
	}

	/**
	 * Ensure inject qualified dependency.
	 */
	public void testQualifiedDependency() throws Exception {
		Qualified annotation = (Qualified) QualifiedDependencyFunction.class.getMethod("method", String.class)
				.getParameterAnnotations()[0][0];
		QualifiedDependencyFunction dependency = new QualifiedDependencyFunction();
		final String object = "DEPENDENCY";
		MethodManagedFunctionBuilderUtil.runMethod(dependency, "method", (type) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> objectType = type.addObject(String.class);
			objectType.setLabel("qualified-" + String.class.getName());
			objectType.setTypeQualifier("qualified");
			objectType.addAnnotation(annotation);
		}, (context) -> {
			context.setObject(0, object);
		});
		assertEquals("Incorrect dependency", object, dependency.object);
	}

	public static class QualifiedDependencyFunction {

		public String object = null;

		public void method(@Qualified("qualified") String dependency) {
			this.object = dependency;
		}
	}

	/**
	 * Ensure can get {@link ManagedFunction} name.
	 */
	public void testFunctionNameParameter() throws Exception {
		MethodResult result = MockParameterManufacturer.run((context) -> {
			return (mc) -> context.getFunctionName();
		}, () -> MethodManagedFunctionBuilderUtil.runStaticMethod(FunctionNameParameterFunction.class, "method",
				(type) -> type.setReturnType(String.class), null));
		assertEquals("Incorrect function name", "method", result.getReturnValue());
	}

	public static class FunctionNameParameterFunction {

		public static String method(String name) {
			return name;
		}
	}

	/**
	 * Ensure can enrich with parameter with annotation.
	 */
	public void testParameterAnnotation() throws Exception {
		final String annotation = "ANNOTATION";
		MockParameterManufacturer.run((context) -> {
			context.addDefaultDependencyAnnotation(annotation);
			return null; // only enrich annotation
		}, () -> MethodManagedFunctionBuilderUtil.runStaticMethod(ParameterAnnotationFunction.class, "method",
				(type) -> {
					ManagedFunctionObjectTypeBuilder<?> objectType = type.addObject(String.class);
					objectType.setLabel(String.class.getName());
					objectType.addAnnotation(annotation); // should include added annotation
				}, (context) -> context.setObject(0, "DEPENDENCY")));
	}

	public static class ParameterAnnotationFunction {
		public static void method(String dependency) {
			// no operation
		}
	}

	/**
	 * Ensure can register exception.
	 */
	public void testRegisterException() throws Exception {
		RuntimeException failure = new RuntimeException("TEST");
		MethodResult result = MockParameterManufacturer.run((context) -> {
			context.addEscalation(RuntimeException.class);
			return (mc) -> failure;
		}, () -> MethodManagedFunctionBuilderUtil.runMethod(new RegisterExceptionFunction(), "method",
				(context) -> context.addEscalation(RuntimeException.class), null));
		assertSame("Should throw exception", failure, result.getFailure());
	}

	public static class RegisterExceptionFunction {

		public void method(RuntimeException exception) {
			throw exception;
		}
	}

	/**
	 * Ensure not register duplicate {@link Exception} types.
	 */
	public void testDuplicateException() throws Exception {
		Exception failure = new Exception("TEST");
		MethodResult result = MockParameterManufacturer.run((context) -> {
			context.addEscalation(Exception.class);
			return (mc) -> failure;
		}, () -> MethodManagedFunctionBuilderUtil.runMethod(new DuplicateExceptionFunction(), "method",
				(type) -> type.addEscalation(Exception.class), null));
		assertSame("Should throw exception", failure, result.getFailure());
	}

	public static class DuplicateExceptionFunction {

		public void method(Exception exception) throws Exception {
			throw exception;
		}
	}

	/**
	 * Ensure can translate dependency.
	 */
	public void testTranslateObject() throws Exception {
		Closure<String> closure = new Closure<>("TEST");
		MethodResult result = MockParameterManufacturer.run((context) -> {

			// Ensure correct method
			assertEquals("Incorrect function name", "method", context.getFunctionName());
			assertEquals("Incorrect method", TranslateObjectFunction.class.getMethod("method", String.class),
					context.getMethod());
			assertEquals("Incorrect parameter index", 0, context.getParameterIndex());

			// Add the object and provide translation
			int objectIndex = context.addObject(Closure.class, null);
			return (mc) -> {
				@SuppressWarnings("unchecked")
				Closure<String> object = (Closure<String>) mc.getObject(objectIndex);
				return object.value;
			};
		}, () -> MethodManagedFunctionBuilderUtil.runMethod(new TranslateObjectFunction(), "method", (type) -> {
			type.addObject(Closure.class);
			type.setReturnType(String.class);
		}, (context) -> {
			context.setObject(0, closure);
		}));
		assertEquals("Incorrect translated argument", closure.value, result.getReturnValue());
	}

	public static class TranslateObjectFunction {

		public String method(String value) {
			return value;
		}
	}

	/**
	 * Ensure can translate return value.
	 */
	public void testTranslateReturn() throws Exception {
		Closure<Class<?>> closure = new Closure<>();
		MethodResult result = MockReturnManufacturer.run(Closure.class, String.class, (context) -> {

			// Ensure correct method
			assertEquals("Incorrect function name", "method", context.getFunctionName());
			assertEquals("Incorrect method", TranslateReturnFunction.class.getMethod("method"), context.getMethod());

			// Provide translation of class
			closure.value = context.getReturnClass();
			context.setTranslatedReturnClass(String.class);
			return (translateContext) -> {
				assertNotNull("Should have " + ManagedFunctionContext.class.getSimpleName(),
						translateContext.getManagedFunctionContext());
				translateContext.setTranslatedReturnValue((String) translateContext.getReturnValue().value);
			};
		}, () -> MethodManagedFunctionBuilderUtil.runMethod(new TranslateReturnFunction(), "method", (type) -> {
			type.setReturnType(String.class);
		}, null));
		assertEquals("Incorrect return class", Closure.class, closure.value);
		assertEquals("Incorrect translated return value", TranslateReturnFunction.VALUE, result.getReturnValue());
	}

	public static class TranslateReturnFunction {

		public static final String VALUE = "VALUE";

		public Closure<String> method() {
			return new Closure<>(VALUE);
		}
	}

	/**
	 * Ensure can translate to <code>null</code> return.
	 */
	public void testTranslateReturnNull() throws Throwable {
		Closure<Class<?>> closure = new Closure<>();
		MethodResult result = MockReturnManufacturer.run(Closure.class, null, (context) -> {

			// Provide translation to null
			closure.value = context.getReturnClass();
			context.setTranslatedReturnClass(null);
			return (translateContext) -> translateContext.setTranslatedReturnValue(null);

		}, () -> MethodManagedFunctionBuilderUtil.runMethod(new TranslateReturnFunction(), "method", null, null));
		assertEquals("Incorrect return class", Closure.class, closure.value);
		assertNull("Should translate to null", result.getReturnValue());
	}

	/**
	 * Ensure can asynchronously translate return value.
	 */
	public void testAsynchronousTranslateReturn() throws Throwable {
		final String ASYNC_TRANSLATED_RESULT = "ASYNC";
		Closure<Thread> thread = new Closure<>();
		MethodResult result = MockReturnManufacturer.run(Closure.class, String.class, (context) -> {

			// Provide asynchronous translate of class
			context.setTranslatedReturnClass(String.class);
			return (translateContext) -> {
				AsynchronousFlow flow = translateContext.getManagedFunctionContext().createAsynchronousFlow();
				thread.value = new Thread(() -> {
					flow.complete(() -> translateContext.setTranslatedReturnValue(ASYNC_TRANSLATED_RESULT));
				});
			};
		}, () -> MethodManagedFunctionBuilderUtil.runMethod(new TranslateReturnFunction(), "method", (type) -> {
			type.setReturnType(String.class);
		}, null));

		// Should not yet have result
		assertNull("Should not have result", result.getReturnValue());

		// Trigger asynchronous translation and wait for its completion
		thread.value.start();
		result.getAsynchronousFlows()[0].waitOnCompletion().run();

		// Should now have result
		assertEquals("Incorrect asynchronous translated result", ASYNC_TRANSLATED_RESULT, result.getReturnValue());
	}

	/**
	 * Ensure can throw {@link Exception} on translating return value.
	 */
	public void testTranslateReturnFailure() throws Throwable {
		MethodResult result = MockReturnManufacturer.run(Closure.class, String.class, (context) -> {

			// Provide translation that fails
			context.addEscalation(Exception.class);
			context.setTranslatedReturnClass(String.class);
			return (translateContext) -> {
				assertNotNull("Should have " + ManagedFunctionContext.class.getSimpleName(),
						translateContext.getManagedFunctionContext());
				throw (Exception) translateContext.getReturnValue().value;
			};
		}, () -> MethodManagedFunctionBuilderUtil.runMethod(new TranslateReturnFailureFunction(), "method", (type) -> {
			type.addEscalation(Exception.class);
			type.setReturnType(String.class);
		}, null));
		assertEquals("Incorrect translation failure", TranslateReturnFailureFunction.FAILURE, result.getFailure());
	}

	public static class TranslateReturnFailureFunction {

		public static final Exception FAILURE = new Exception("TEST");

		public Closure<Throwable> method() {
			return new Closure<>(FAILURE);
		}
	}

	/**
	 * Ensure can throw {@link Exception} on asynchronous translating return value.
	 */
	public void testAsynchronousTranslateReturnFailure() throws Throwable {
		Closure<Thread> thread = new Closure<>();
		MethodResult result = MockReturnManufacturer.run(Closure.class, String.class, (context) -> {

			// Provide translation that fails
			context.addEscalation(Exception.class);
			context.setTranslatedReturnClass(String.class);
			return (translateContext) -> {
				AsynchronousFlow flow = translateContext.getManagedFunctionContext().createAsynchronousFlow();
				thread.value = new Thread(() -> {
					flow.complete(() -> {
						throw (Exception) translateContext.getReturnValue().value;
					});
				});
			};
		}, () -> MethodManagedFunctionBuilderUtil.runMethod(new TranslateReturnFailureFunction(), "method", (type) -> {
			type.addEscalation(Exception.class);
			type.setReturnType(String.class);
		}, null));

		// Should not yet have result nor failure
		assertNull("Should not have result", result.getReturnValue());
		assertNull("Should not have failure", result.getFailure());

		// Trigger asynchronous translation and wait for its completion
		thread.value.start();
		AsynchronousFlowCompletion completion = result.getAsynchronousFlows()[0].waitOnCompletion();
		try {
			completion.run();
			fail("Should not be successful");
		} catch (Exception ex) {
			// Should now have result
			assertEquals("Incorrect asynchronous translation failure", TranslateReturnFailureFunction.FAILURE, ex);
		}
	}

	/**
	 * Translate function on return.
	 */
	public void testFunctionNameReturn() throws Exception {
		MethodResult result = MockReturnManufacturer.run(String.class, String.class, (context) -> {
			return (translateContext) -> translateContext.setTranslatedReturnValue(context.getFunctionName());
		}, () -> MethodManagedFunctionBuilderUtil.runMethod(new FunctionNameReturnFunction(), "method", (type) -> {
			type.setReturnType(String.class);
		}, null));
		assertEquals("Incorrect returned function name", "method", result.getReturnValue());
	}

	public static class FunctionNameReturnFunction {
		public String method() {
			return "TRANSLATED";
		}
	}

	/**
	 * Translate function to return annotation value.
	 */
	public void testReturnAnnotation() throws Exception {
		Next annotation = ReturnAnnotationFunction.class.getMethod("method").getAnnotation(Next.class);
		MethodResult result = MockReturnManufacturer.run(String.class, Next.class, (context) -> {
			return (translateContext) -> translateContext
					.setTranslatedReturnValue((Next) context.getMethodAnnotations()[0]);
		}, () -> MethodManagedFunctionBuilderUtil.runMethod(new ReturnAnnotationFunction(), "method", (type) -> {
			type.addAnnotation(annotation);
			type.setReturnType(Next.class);
		}, null));
		assertSame("Incorrect annotation", annotation, result.getReturnValue());
	}

	public static class ReturnAnnotationFunction {

		@Next("TEST")
		public String method() {
			return "TRANSLATED";
		}
	}

}
