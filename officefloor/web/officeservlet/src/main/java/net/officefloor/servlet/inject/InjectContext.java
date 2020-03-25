package net.officefloor.servlet.inject;

import net.officefloor.compile.spi.supplier.source.SupplierThreadLocal;

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
	 */
	public static Object getActiveDependency(int dependencyIndex) {
		return activeContext.get().getDependency(dependencyIndex);
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