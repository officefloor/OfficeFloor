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

package net.officefloor.plugin.variable;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Consumer;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Variable {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableManagedObjectSource<T> extends AbstractManagedObjectSource<None, None> {

	/**
	 * Prefix for variable name.
	 */
	public static final String VARIABLE_NAME_PREFIX = "VARIABLE_";

	/**
	 * Extracts the type from the variable.
	 * 
	 * @param variableGenericType Variable {@link Type}.
	 * @return Variable type.
	 */
	public static String extractVariableType(Type variableGenericType) {
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
	 * Obtains {@link Var} from dependency object.
	 * 
	 * @param object Dependency object.
	 * @return {@link Var} wrapper on dependency object.
	 * @throws IllegalStateException If fails to convert.
	 */
	@SuppressWarnings("unchecked")
	public static <T> Var<T> var(Object object) throws IllegalStateException {
		if (!(object instanceof Var)) {
			throw new IllegalStateException("Object is not a variable");
		}
		return (Var<T>) object;
	}

	/**
	 * Obtains {@link Out} from dependency object.
	 * 
	 * @param object Dependency object.
	 * @return {@link Out} wrapper on dependency object.
	 */
	public static <T> Out<T> out(Object object) {
		Var<T> variable = var(object);
		return (value) -> variable.set(value);
	}

	/**
	 * Obtains {@link In} from dependency object.
	 * 
	 * @param object Dependency object.
	 * @return {@link In} wrapper on dependency object.
	 */
	public static <T> In<T> in(Object object) {
		Var<T> variable = var(object);
		return () -> variable.get();
	}

	/**
	 * Obtains the value from dependency object.
	 * 
	 * @param object Dependency object.
	 * @return Value extracted from dependency object.
	 */
	public static <T> T val(Object object) {
		Var<T> variable = var(object);
		return variable.get();
	}

	/**
	 * Obtains the variable type.
	 * 
	 * @param type Raw type name.
	 * @return Variable type.
	 */
	public static String type(String type) {

		// Handle primitives and their arrays
		switch (type) {
		case "boolean":
			type = Boolean.class.getName();
			break;
		case "byte":
			type = Byte.class.getName();
			break;
		case "short":
			type = Short.class.getName();
			break;
		case "char":
			type = Character.class.getName();
			break;
		case "int":
			type = Integer.class.getName();
			break;
		case "long":
			type = Long.class.getName();
			break;
		case "float":
			type = Float.class.getName();
			break;
		case "double":
			type = Double.class.getName();
			break;
		case "[B":
			type = "byte[]";
			break;
		case "[S":
			type = "short[]";
			break;
		case "[C":
			type = "char[]";
			break;
		case "[I":
			type = "int[]";
			break;
		case "[J":
			type = "long[]";
			break;
		case "[F":
			type = "float[]";
			break;
		case "[D":
			type = "double[]";
			break;
		default:
			// Use type as is
			break;
		}

		// Handle object array
		final String START = "[L";
		final String END = ";";
		if (type.startsWith(START) && type.endsWith(END)) {

			// Array, so obtain component name
			String componentName = type.substring(START.length(), type.length() - END.length());
			type = componentName + "[]";
		}

		// Return the type
		return type;
	}

	/**
	 * Obtains the variable name.
	 * 
	 * @param qualifier Qualifier for variable.
	 * @param type      Variable type.
	 * @return Name for the variable.
	 */
	public static String name(String qualifier, String type) {
		return (qualifier == null ? "" : qualifier + "-") + type(type);
	}

	/**
	 * Decorator of new {@link Var}.
	 */
	private final Consumer<Var<T>> decorator;

	/**
	 * Default constructor.
	 */
	public VariableManagedObjectSource() {
		this.decorator = null;
	}

	/**
	 * Instantiate.
	 * 
	 * @param decorator Decorator of new {@link Var}.
	 */
	public VariableManagedObjectSource(Consumer<Var<T>> decorator) {
		this.decorator = decorator;
	}

	/*
	 * =================== ManagedObjectSource ===================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {

		// Provide meta-data
		context.setObjectClass(Var.class);
		context.setManagedObjectClass(VariableManagedObject.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new VariableManagedObject();
	}

	/**
	 * Variable {@link ManagedObject}.
	 */
	private class VariableManagedObject implements ContextAwareManagedObject, Var<T> {

		/**
		 * {@link ManagedObjectContext}.
		 */
		private ManagedObjectContext context;

		/**
		 * Value for the {@link Var}.
		 */
		private T value;

		/*
		 * ============= ContextAwareManagedObject ================
		 */

		@Override
		public void setManagedObjectContext(ManagedObjectContext context) {
			this.context = context;
		}

		@Override
		public Object getObject() throws Throwable {

			// Easy access to source
			VariableManagedObjectSource<T> mos = VariableManagedObjectSource.this;

			// Enable decoration of the variable
			// (after getting process aware context)
			if (mos.decorator != null) {
				mos.decorator.accept(this);
			}

			// Return this var
			return this;
		}

		/*
		 * ======================= Var =============================
		 */

		@Override
		public void set(T value) {
			this.context.run(() -> this.value = value);
		}

		@Override
		public T get() {
			return this.context.run(() -> this.value);
		}
	}

}
