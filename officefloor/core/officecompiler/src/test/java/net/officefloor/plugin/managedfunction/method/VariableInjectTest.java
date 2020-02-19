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
			object.setLabel("VAR-" + String.class.getName());
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
			object.setLabel("VAR-qualified-" + Integer.class.getName());
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
		Val annotation = (Val) ValueFunction.class.getMethod("method", String.class).getParameterAnnotations()[0][0];
		MockVar<String> variable = new MockVar<>("VAL");
		ValueFunction instance = new ValueFunction();
		MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (context) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> object = context.addObject(Var.class);
			object.setTypeQualifier(String.class.getName());
			object.setLabel("VAR-" + String.class.getName());
			object.addAnnotation(annotation);
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
			object.setLabel("VAR-" + ValueQualifier.class.getName() + "-" + Integer.class.getName());
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
			object.setLabel("VAR-" + String.class.getName());
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
			object.setLabel("VAR-" + ValueQualifier.class.getName() + "-" + Integer.class.getName());
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
			object.setLabel("VAR-" + String.class.getName());
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
			object.setLabel("VAR-qualified-" + Integer.class.getName());
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
