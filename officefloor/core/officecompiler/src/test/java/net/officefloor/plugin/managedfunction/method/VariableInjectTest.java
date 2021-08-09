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

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import net.officefloor.compile.spi.managedfunction.source.ManagedFunctionObjectTypeBuilder;
import net.officefloor.compile.test.managedfunction.clazz.MethodManagedFunctionBuilderUtil;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.model.test.variable.MockVar;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.clazz.Qualifier;
import net.officefloor.plugin.variable.In;
import net.officefloor.plugin.variable.Out;
import net.officefloor.plugin.variable.Val;
import net.officefloor.plugin.variable.Var;
import net.officefloor.plugin.variable.VariableAnnotation;

/**
 * Ensure can inject variables.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableInjectTest extends OfficeFrameTestCase {

	/**
	 * Ensure can invoke {@link Method} with {@link Var}.
	 */
	public void testVariable() {
		MockVar<String> variable = new MockVar<>();
		MethodManagedFunctionBuilderUtil.runMethod(new VariableFunction(), "method", (context) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> object = context.addObject(Var.class);
			object.setTypeQualifier(String.class.getName());
			object.setLabel(String.class.getName() + "-" + Var.class.getName());
			object.addAnnotation(new VariableAnnotation(String.class.getName(), String.class.getName()));
		}, (type) -> {
			type.setObject(0, variable);
		});
		assertEquals("Incorrect value", VariableFunction.VALUE, variable.get());
	}

	public static class VariableFunction {

		public static final String VALUE = "VALUE";

		public void method(Var<String> variable) {
			variable.set(VALUE);
		}
	}

	/**
	 * Ensure can invoke {@link Method} with qualified {@link Var}.
	 */
	public void testQualifiedVariable() throws Exception {
		Qualified annotation = (Qualified) QualifiedVariableFunction.class.getMethod("method", Var.class)
				.getParameterAnnotations()[0][0];
		MockVar<String> variable = new MockVar<>();
		MethodManagedFunctionBuilderUtil.runMethod(new QualifiedVariableFunction(), "method", (context) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> object = context.addObject(Var.class);
			object.setTypeQualifier("qualified-" + Integer.class.getName());
			object.setLabel("qualified-" + Integer.class.getName() + "-" + Var.class.getName());
			object.addAnnotation(annotation);
			object.addAnnotation(
					new VariableAnnotation("qualified-" + Integer.class.getName(), Integer.class.getName()));
		}, (type) -> {
			type.setObject(0, variable);
		});
		assertEquals("Incorrect value", QualifiedVariableFunction.VALUE, variable.get());
	}

	public static class QualifiedVariableFunction {

		public static final Integer VALUE = Integer.valueOf(1);

		public void method(@Qualified("qualified") Var<Integer> variable) {
			variable.set(VALUE);
		}
	}

	/**
	 * Ensure can invoke {@link Method} with {@link Val}.
	 */
	public void testValue() throws Exception {
		MockVar<String> variable = new MockVar<>("VAL");
		ValueFunction instance = new ValueFunction();
		MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (context) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> object = context.addObject(Var.class);
			object.setTypeQualifier(String.class.getName());
			object.setLabel(String.class.getName() + "-" + Var.class.getName());
			object.addAnnotation(Val.class);
			object.addAnnotation(new VariableAnnotation(String.class.getName(), String.class.getName()));
		}, (type) -> {
			type.setObject(0, variable);
		});
		assertEquals("Incorrect value", variable.get(), instance.value);
	}

	public static class ValueFunction {

		public String value;

		public void method(@Val String value) {
			this.value = value;
		}
	}

	/**
	 * Ensure can invoke {@link Method} with qualified {@link Val}.
	 */
	public void testQualifiedValue() throws Exception {
		Annotation[] annotations = QualifiedValueFunction.class.getMethod("method", Integer.class)
				.getParameterAnnotations()[0];
		MockVar<Integer> variable = new MockVar<>(Integer.valueOf(1));
		QualifiedValueFunction instance = new QualifiedValueFunction();
		MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (context) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> object = context.addObject(Var.class);
			object.setTypeQualifier(ValueQualifier.class.getName() + "-" + Integer.class.getName());
			object.setLabel(ValueQualifier.class.getName() + "-" + Integer.class.getName() + "-" + Var.class.getName());
			for (Annotation annotation : annotations) {
				object.addAnnotation(annotation);
			}
			object.addAnnotation(new VariableAnnotation(ValueQualifier.class.getName() + "-" + Integer.class.getName(),
					Integer.class.getName()));
		}, (type) -> {
			type.setObject(0, variable);
		});
		assertEquals("Incorrect value", variable.get(), instance.value);
	}

	@Qualifier
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface ValueQualifier {
	}

	public static class QualifiedValueFunction {

		public Integer value;

		public void method(@ValueQualifier @Val Integer value) {
			this.value = value;
		}
	}

	/**
	 * Ensure can invoke {@link Method} with {@link In}.
	 */
	public void testIn() {
		MockVar<String> variable = new MockVar<>("VAL");
		InFunction instance = new InFunction();
		MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (context) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> object = context.addObject(Var.class);
			object.setTypeQualifier(String.class.getName());
			object.setLabel(String.class.getName() + "-" + Var.class.getName());
			object.addAnnotation(new VariableAnnotation(String.class.getName(), String.class.getName()));
		}, (type) -> {
			type.setObject(0, variable);
		});
		assertEquals("Incorrect value", variable.get(), instance.value);
	}

	public static class InFunction {

		public String value;

		public void method(In<String> value) {
			this.value = value.get();
		}
	}

	/**
	 * Ensure can invoke {@link Method} with qualified {@link In}.
	 */
	public void testQualifiedIn() throws Exception {
		ValueQualifier annotation = (ValueQualifier) QualifiedInFunction.class.getMethod("method", In.class)
				.getParameterAnnotations()[0][0];
		MockVar<Integer> variable = new MockVar<>(Integer.valueOf(1));
		QualifiedInFunction instance = new QualifiedInFunction();
		MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (context) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> object = context.addObject(Var.class);
			object.setTypeQualifier(ValueQualifier.class.getName() + "-" + Integer.class.getName());
			object.setLabel(ValueQualifier.class.getName() + "-" + Integer.class.getName() + "-" + Var.class.getName());
			object.addAnnotation(annotation);
			object.addAnnotation(new VariableAnnotation(ValueQualifier.class.getName() + "-" + Integer.class.getName(),
					Integer.class.getName()));
		}, (type) -> {
			type.setObject(0, variable);
		});
		assertEquals("Incorrect value", variable.get(), instance.value);
	}

	public static class QualifiedInFunction {

		public Integer value;

		public void method(@ValueQualifier In<Integer> value) {
			this.value = value.get();
		}
	}

	/**
	 * Ensure can invoke {@link Method} with {@link Out}.
	 */
	public void testOut() {
		MockVar<String> variable = new MockVar<>();
		MethodManagedFunctionBuilderUtil.runMethod(new OutFunction(), "method", (context) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> object = context.addObject(Var.class);
			object.setTypeQualifier(String.class.getName());
			object.setLabel(String.class.getName() + "-" + Var.class.getName());
			object.addAnnotation(new VariableAnnotation(String.class.getName(), String.class.getName()));
		}, (type) -> {
			type.setObject(0, variable);
		});
		assertEquals("Incorrect value", OutFunction.VALUE, variable.get());
	}

	public static class OutFunction {

		public static final String VALUE = "VALUE";

		public void method(Out<String> variable) {
			variable.set(VALUE);
		}
	}

	/**
	 * Ensure can invoke {@link Method} with qualified {@link Out}.
	 */
	public void testOutVariable() throws Exception {
		Qualified annotation = (Qualified) QualifiedOutFunction.class.getMethod("method", Out.class)
				.getParameterAnnotations()[0][0];
		MockVar<String> variable = new MockVar<>();
		MethodManagedFunctionBuilderUtil.runMethod(new QualifiedOutFunction(), "method", (context) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> object = context.addObject(Var.class);
			object.setTypeQualifier("qualified-" + Integer.class.getName());
			object.setLabel("qualified-" + Integer.class.getName() + "-" + Var.class.getName());
			object.addAnnotation(annotation);
			object.addAnnotation(new VariableAnnotation("qualified-" + Integer.class, Integer.class.getName()));
		}, (type) -> {
			type.setObject(0, variable);
		});
		assertEquals("Incorrect value", QualifiedOutFunction.VALUE, variable.get());
	}

	public static class QualifiedOutFunction {

		public static final Integer VALUE = Integer.valueOf(1);

		public void method(@Qualified("qualified") Out<Integer> variable) {
			variable.set(VALUE);
		}
	}

}
