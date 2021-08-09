/*-
 * #%L
 * Servlet
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
