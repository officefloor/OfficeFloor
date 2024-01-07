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
import net.officefloor.frame.api.managedobject.source.ManagedObjectSourceContext;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for the {@link CabinetManager}.
 * 
 * @author Daniel Sagenschneider
 */
public class CabinetManagerManagedObjectSource extends AbstractManagedObjectSource<None, None> {

	/**
	 * {@link ThreadLocal} for overridng the {@link OfficeStore}.
	 */
	private static final ThreadLocal<OfficeStore> OFFICE_STORE_OVERRIDE = new ThreadLocal<>();

	/**
	 * Functionality to have the {@link OfficeStore} overridden.
	 */
	public @FunctionalInterface interface OverrideOfficeStore {

		/**
		 * Functionality for overridden {@link OfficeStore}.
		 * 
		 * @throws Exception If fails.
		 */
		void overridenExecution() throws Exception;
	}

	/**
	 * Runs functionality for overridden {@link OfficeStore}.
	 * 
	 * @param officeStore {@link OfficeStore}.
	 * @param override    {@link OverrideOfficeStore}.
	 * @throws Exception Failure.
	 */
	public static void overrideOfficeStore(OfficeStore officeStore, OverrideOfficeStore override) throws Exception {
		try {

			// Override the office store
			OFFICE_STORE_OVERRIDE.set(officeStore);

			// Undertake functionality
			override.overridenExecution();

		} finally {
			// Clear override
			OFFICE_STORE_OVERRIDE.remove();
		}
	}

	/**
	 * {@link OfficeStore}.
	 */
	private OfficeStore officeStore;

	/**
	 * Obtains the {@link OfficeStore}.
	 * 
	 * @return {@link OfficeStore}.
	 */
	protected OfficeStore getOfficeStore() {
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
		ManagedObjectSourceContext<None> mosContext = context.getManagedObjectSourceContext();

		context.setObjectClass(CabinetManager.class);

		// Provide store on process completion
		mosContext.getRecycleFunction(new RecycleFunction()).linkParameter(0, RecycleManagedObjectParameter.class);

		// Determine if override office store
		this.officeStore = OFFICE_STORE_OVERRIDE.get();
		if (this.officeStore == null) {

			// Load the office store
			this.officeStore = mosContext.loadService(OfficeStoreServiceFactory.class, null);
		}
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
