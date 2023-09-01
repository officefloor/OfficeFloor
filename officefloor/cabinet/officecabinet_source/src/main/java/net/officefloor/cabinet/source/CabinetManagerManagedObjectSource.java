package net.officefloor.cabinet.source;

import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for the {@link CabinetManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class CabinetManagerManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * {@link OfficeStore}.
	 */
	private OfficeStore officeStore;

	/*
	 * =================== ManagedObjectSource ==================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {
		context.setObjectClass(CabinetManager.class);

		// Load the office store
		this.officeStore = context.getManagedObjectSourceContext().loadService(OfficeStoreServiceFactory.class, null);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {

		// Create the cabinet manager
		CabinetManager cabinetManager = this.officeStore.createCabinetManager();

		// Return the managed object
		return new CabinetManagerManagedObject(cabinetManager);
	}

	/**
	 * {@link ManagedObject} for the {@link CabinetManager}.
	 */
	private static class CabinetManagerManagedObject implements ManagedObject {

		/**
		 * {@link CabinetManager}.
		 */
		private final CabinetManager cabinetManager;

		/**
		 * Instantiate.
		 * 
		 * @param cabinetManager {@link CabinetManager}.
		 */
		private CabinetManagerManagedObject(CabinetManager cabinetManager) {
			this.cabinetManager = cabinetManager;
		}

		/*
		 * =============== ManagedObject ================
		 */

		@Override
		public Object getObject() throws Throwable {
			return this.cabinetManager;
		}
	}

}
