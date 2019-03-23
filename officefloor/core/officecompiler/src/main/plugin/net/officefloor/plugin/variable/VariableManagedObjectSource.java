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
public class VariableManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * Obtains {@link Out} wrapper on {@link Var}.
	 * 
	 * @param variable {@link Var}.
	 * @return {@link Out} wrapper on {@link Var}.
	 */
	public static <T> Out<T> out(Var<T> variable) {
		return (value) -> variable.set(value);
	}

	/**
	 * Obtains {@link In} wrapper on {@link Var}.
	 * 
	 * @param variable {@link Var}.
	 * @return {@link In} wrapper on {@link Var}
	 */
	public static <T> In<T> in(Var<T> variable) {
		return () -> variable.get();
	}

	/**
	 * Obtains the value of the variable.
	 * 
	 * @param variable {@link Var}.
	 * @return Value of the {@link Var}.
	 */
	public static <T> T val(Var<T> variable) {
		return variable.get();
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
		return new VariableManagedObject<>();
	}

	/**
	 * Variable {@link ManagedObject}.
	 */
	private class VariableManagedObject<T> implements ProcessAwareManagedObject, Var<T> {

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