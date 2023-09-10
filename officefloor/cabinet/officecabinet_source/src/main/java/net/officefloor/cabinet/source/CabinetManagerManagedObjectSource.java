package net.officefloor.cabinet.source;

import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.frame.api.build.Indexed;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.function.ManagedFunctionContext;
import net.officefloor.frame.api.function.StaticManagedFunction;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.recycle.CleanupEscalation;
import net.officefloor.frame.api.managedobject.recycle.RecycleManagedObjectParameter;
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

	/**
	 * Obtains the {@link OfficeStore}.
	 * 
	 * @return {@link OfficeStore}.
	 */
	public OfficeStore getOfficeStore() {
		return this.officeStore;
	}

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

		// Provide store on process completion
		context.getManagedObjectSourceContext().getRecycleFunction(new RecycleFunction()).linkParameter(0,
				RecycleManagedObjectParameter.class);

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

	/**
	 * Recycles the {@link CabinetManager}.
	 */
	private static class RecycleFunction extends StaticManagedFunction<Indexed, None> {

		@Override
		public void execute(ManagedFunctionContext<Indexed, None> context) throws Throwable {

			// Obtain the cabinet manager
			RecycleManagedObjectParameter<CabinetManagerManagedObject> recycle = RecycleManagedObjectParameter
					.getRecycleManagedObjectParameter(context);
			CabinetManager cabinetManager = recycle.getManagedObject().cabinetManager;

			// If no escalations, then flush changes
			CleanupEscalation[] escalations = recycle.getCleanupEscalations();
			if ((escalations == null) || (escalations.length == 0)) {
				// No escalations, so flush
				cabinetManager.flush();
			}

			// Reuse the connection
			recycle.reuseManagedObject();
		}
	}

}
