/*-
 * #%L
 * Servlet
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

package net.officefloor.servlet.inject;

import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;
import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * Context for injection.
 * 
 * @author Daniel Sagenschneider
 */
public class InjectContext {

	/**
	 * Name of attribute storing the {@link InjectContext}.
	 */
	public static final String REQUEST_ATTRIBUTE_NAME = InjectContext.class.getSimpleName();

	/**
	 * Obtains the dependency.
	 * 
	 * @param dependencyIndex Index of the dependency.
	 * @return Dependency.
	 * @throws IllegalStateException If invoked from an unmanaged {@link Thread}.
	 */
	public static Object getActiveDependency(int dependencyIndex) throws IllegalStateException {
		InjectContext injectContext = activeContext.get();
		if (injectContext == null) {
			throw new IllegalStateException(
					"Attempting to use " + OfficeFloor.class.getSimpleName() + " dependency on unmanaged thread.");
		}
		return injectContext.getDependency(dependencyIndex);
	}

	/**
	 * Active {@link InjectContext} for the {@link Thread}.
	 */
	private static final ThreadLocal<InjectContext> activeContext = new ThreadLocal<>();

	/**
	 * Listing of {@link SupplierThreadLocal} instances with indexes corresponding
	 * to dependency index.
	 */
	private final SupplierThreadLocal<?>[] supplierThreadLocals;

	/**
	 * Loaded dependencies.
	 */
	private final Object[] dependencies;

	/**
	 * Instantiate.
	 * 
	 * @param supplierThreadLocals Listing of {@link SupplierThreadLocal} instances
	 *                             with indexes corresponding to dependency index.
	 */
	public InjectContext(SupplierThreadLocal<?>[] supplierThreadLocals) {
		this.supplierThreadLocals = supplierThreadLocals;
		this.dependencies = new Object[supplierThreadLocals.length];
	}

	/**
	 * Activates this {@link InjectContext}.
	 */
	public synchronized void activate() {
		activeContext.set(this);
	}

	/**
	 * Synchronises this {@link InjectContext} for another {@link Thread}.
	 */
	public synchronized void synchroniseForAnotherThread() {

		// Load all the dependencies (so available to another thread)
		for (int i = 0; i < this.dependencies.length; i++) {
			this.getDependency(i);
		}
	}

	/**
	 * Obtains the dependency for the index.
	 * 
	 * @param index Index of the dependency.
	 * @return Dependency.
	 */
	private Object getDependency(int index) {

		// Lazy load the dependency
		Object dependency = this.dependencies[index];
		if (dependency == null) {

			// No dependency so obtain and register
			dependency = this.supplierThreadLocals[index].get();
			this.dependencies[index] = dependency;
		}

		// Return the dependency
		return dependency;
	}

}
