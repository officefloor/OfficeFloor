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
package net.officefloor.plugin.variable;

import java.util.function.Consumer;

import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ProcessAwareContext;
import net.officefloor.frame.api.managedobject.ProcessAwareManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * Variable {@link ManagedObjectSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class VariableManagedObjectSource<T> extends AbstractManagedObjectSource<None, None> {

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
	 * Obtains the variable name.
	 * 
	 * @param qualifier Qualifier for variable.
	 * @param type      Variable type.
	 * @return Name for the variable.
	 */
	public static String name(String qualifier, String type) {
		return (qualifier == null ? "" : qualifier + "-") + type;
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
	private class VariableManagedObject implements ProcessAwareManagedObject, Var<T> {

		/**
		 * {@link ProcessAwareContext}.
		 */
		private ProcessAwareContext context;

		/**
		 * Value for the {@link Var}.
		 */
		private T value;

		/*
		 * ============= ProcessAwareManagedObject ================
		 */

		@Override
		public void setProcessAwareContext(ProcessAwareContext context) {
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