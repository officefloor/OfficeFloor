package net.officefloor.spring.extension;

import net.officefloor.spring.SpringSupplierSource;

/**
 * Extension to {@link SpringSupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SpringSupplierExtension {

	/**
	 * <p>
	 * Invoked before Spring is loaded.
	 * <p>
	 * This allows initial setup to be undertaken. It also allows capturing
	 * information on the current {@link Thread} as Spring loads.
	 * 
	 * @param context {@link SpringSupplierExtensionContext}.
	 * @throws Exception If fails to setup.
	 */
	default void beforeSpringLoad(SpringSupplierExtensionContext context) throws Exception {
		// does nothing by default
	}

	/**
	 * <p>
	 * Invoked after Spring is loaded.
	 * <p>
	 * Allows processing captured information.
	 * 
	 * @param context {@link SpringSupplierExtensionContext}.
	 * @throws Exception If fails to complete extension configuration.
	 */
	default void afterSpringLoad(SpringSupplierExtensionContext context) throws Exception {
		// does nothing by default
	}

	/**
	 * Invoked for each registered Spring bean to further decorate integration.
	 * 
	 * @param context {@link SpringBeanDecoratorContext}.
	 * @throws Exception If fails to decorate the Spring Bean.
	 */
	default void decorateSpringBean(SpringBeanDecoratorContext context) throws Exception {
		// does nothing by default
	}

}