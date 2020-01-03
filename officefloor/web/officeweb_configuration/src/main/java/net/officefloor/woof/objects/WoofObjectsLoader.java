package net.officefloor.woof.objects;

import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.woof.model.objects.WoofObjectsModel;

/**
 * Loads the {@link WoofObjectsModel} and configures the {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WoofObjectsLoader {

	/**
	 * Loads the {@link WoofObjectsModel} configuration and configures the
	 * {@link SupplierSource}.
	 * 
	 * @param context
	 *            {@link WoofObjectsLoaderContext}.
	 * @throws Exception
	 *             If fails to load the configuration.
	 */
	void loadWoofObjectsConfiguration(WoofObjectsLoaderContext context) throws Exception;

}