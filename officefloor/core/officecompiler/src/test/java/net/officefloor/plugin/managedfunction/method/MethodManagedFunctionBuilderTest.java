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

package net.officefloor.plugin.managedfunction.method;

import java.lang.reflect.Method;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil.MethodResult;
import net.officefloor.frame.api.administration.AdministrationContext;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.function.AsynchronousFlowCompletion;
import net.officefloor.frame.api.function.ManagedFunction;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.test.Closure;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.clazz.dependency.ClassDependencyFactory;
import net.officefloor.plugin.clazz.method.MethodManagedFunctionBuilder;
import net.officefloor.plugin.section.clazz.Next;

/**
 * Tests the {@link MethodManagedFunctionBuilder}.
 * 
 * @author Daniel Sagenschneider
 */
public class MethodManagedFunctionBuilderTest extends OfficeFrameTestCase {

	@FunctionalInterface
	private static interface MethodClassDependencyFactory extends ClassDependencyFactory {

		@Override
		default Object createDependency(ManagedObject managedObject, ManagedObjectContext context,
				ObjectRegistry<Indexed> registry) throws Throwable {
			fail("Should not be invoked");
			return null;
		}

		@Override
		default Object createDependency(AdministrationContext<Object, Indexed, Indexed> context) throws Throwable {
			fail("Should not be invoked");
			return null;
		}
	}

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
		MethodResult result = MockClassDependencyManufacturer.run((context) -> {
			return (MethodClassDependencyFactory) (mc) -> context.getName();
		}, () -> MethodManagedFunctionBuilderUtil.runStaticMethod(FunctionNameParameterFunction.class, "method",
				(type) -> type.setReturnType(String.class), null));
		assertEquals("Incorrect function name", "Compiler", result.getReturnValue());
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
		MockClassDependencyManufacturer.run((context) -> {
			context.addAnnotation(annotation);
			return null;
		}, () -> MethodManagedFunctionBuilderUtil.runStaticMethod(ParameterAnnotationFunction.class, "method",
				(type) -> {
					type.addAnnotation(annotation);
					type.addObject(String.class).setLabel(String.class.getName());
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
		MethodResult result = MockClassDependencyManufacturer.run((context) -> {
			context.addEscalation(RuntimeException.class);
			return (MethodClassDependencyFactory) (mc) -> failure;
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
		MethodResult result = MockClassDependencyManufacturer.run((context) -> {
			context.addEscalation(Exception.class);
			return (MethodClassDependencyFactory) (mc) -> failure;
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
		MethodResult result = MockClassDependencyManufacturer.run((context) -> {

			// Ensure correct method
			assertEquals("Incorrect function name", "Compiler", context.getName());
			assertEquals("Incorrect method", TranslateObjectFunction.class.getMethod("method", String.class),
					context.getExecutable());
			assertEquals("Incorrect parameter index", 0, context.getExecutableParameterIndex());

			// Add the object and provide translation
			int objectIndex = context.newDependency(Closure.class).build().getIndex();
			return (MethodClassDependencyFactory) (mc) -> {
				@SuppressWarnings("unchecked")
				Closure<String> object = (Closure<String>) mc.getObject(objectIndex);
				return object.value;
			};
		}, () -> MethodManagedFunctionBuilderUtil.runMethod(new TranslateObjectFunction(), "method", (type) -> {
			type.addObject(Closure.class).setLabel(Closure.class.getName());
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
