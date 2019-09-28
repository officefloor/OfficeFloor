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
	public void testQualifiedVariable() {
		MockVar<String> variable = new MockVar<>();
		MethodManagedFunctionBuilderUtil.runMethod(new QualifiedVariableFunction(), "method", (context) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> object = context.addObject(Var.class);
			object.setTypeQualifier("qualified-" + Integer.class.getName());
			object.setLabel("VAR-qualified-" + Integer.class.getName());
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
	public void testValue() {
		MockVar<String> variable = new MockVar<>("VAL");
		ValueFunction instance = new ValueFunction();
		MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (context) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> object = context.addObject(Var.class);
			object.setTypeQualifier(String.class.getName());
			object.setLabel("VAR-" + String.class.getName());
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
	public void testQualifiedValue() {
		MockVar<Integer> variable = new MockVar<>(Integer.valueOf(1));
		QualifiedValueFunction instance = new QualifiedValueFunction();
		MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (context) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> object = context.addObject(Var.class);
			object.setTypeQualifier(ValueQualifier.class.getName() + "-" + Integer.class.getName());
			object.setLabel("VAR-" + ValueQualifier.class.getName() + "-" + Integer.class.getName());
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
	public void testQualifiedIn() {
		MockVar<Integer> variable = new MockVar<>(Integer.valueOf(1));
		QualifiedInFunction instance = new QualifiedInFunction();
		MethodManagedFunctionBuilderUtil.runMethod(instance, "method", (context) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> object = context.addObject(Var.class);
			object.setTypeQualifier(ValueQualifier.class.getName() + "-" + Integer.class.getName());
			object.setLabel("VAR-" + ValueQualifier.class.getName() + "-" + Integer.class.getName());
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
	public void testOutVariable() {
		MockVar<String> variable = new MockVar<>();
		MethodManagedFunctionBuilderUtil.runMethod(new QualifiedOutFunction(), "method", (context) -> {
			ManagedFunctionObjectTypeBuilder<Indexed> object = context.addObject(Var.class);
			object.setTypeQualifier("qualified-" + Integer.class.getName());
			object.setLabel("VAR-qualified-" + Integer.class.getName());
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