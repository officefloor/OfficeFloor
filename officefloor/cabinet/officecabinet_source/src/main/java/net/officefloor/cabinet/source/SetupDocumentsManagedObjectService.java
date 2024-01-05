package net.officefloor.cabinet.source;

import java.util.Map;
import java.util.Set;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectExecuteContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectService;
import net.officefloor.frame.api.managedobject.source.ManagedObjectServiceContext;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} to setup the {@link Document} types within the
 * {@link OfficeStore}.
 */
public class SetupDocumentsManagedObjectService extends AbstractManagedObjectSource<None, None>
		implements ManagedObjectService<None> {

	/**
	 * Meta-data of {@link Document} type to it's {@link Index} instances.
	 */
	private final Map<Class<?>, Set<Index>> documentMetaData;

	/**
	 * {@link CabinetManagerManagedObjectSource}.
	 */
	private final CabinetManagerManagedObjectSource cabinetManagerManagedObjectSource;

	/**
	 * Instantiate.
	 * 
	 * @param documentMetaData                  Meta-data of {@link Document} type
	 *                                          to it's {@link Index} instances.
	 * @param cabinetManagerManagedObjectSource {@link CabinetManagerManagedObjectSource}.
	 */
	public SetupDocumentsManagedObjectService(Map<Class<?>, Set<Index>> documentMetaData,
			CabinetManagerManagedObjectSource cabinetManagerManagedObjectSource) {
		this.documentMetaData = documentMetaData;
		this.cabinetManagerManagedObjectSource = cabinetManagerManagedObjectSource;
	}

	/*
	 * =================== ManagedObjectSource ====================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification required
	}

	@Override
	protected void loadMetaData(MetaDataContext<None, None> context) throws Exception {

		// Not to actually be used
		context.setObjectClass(this.getClass());
	}

	@Override
	public void start(ManagedObjectExecuteContext<None> context) throws Exception {

		// Setup documents on start up
		context.addService(this);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		throw new IllegalStateException("Should never depend on " + this.getClass().getSimpleName());
	}

	/*
	 * ================== ManagedObjectService ===================
	 */

	@Override
	public void startServicing(ManagedObjectServiceContext<None> serviceContext) throws Exception {

		// Obtain the OfficeStore (should be loaded)
		OfficeStore officeStore = this.cabinetManagerManagedObjectSource.getOfficeStore();

		// Load the document meta-data
		for (Class<?> documentType : this.documentMetaData.keySet()) {

			// Obtain the indexes
			Index[] indexes = this.documentMetaData.get(documentType).stream().toArray(Index[]::new);

			// Setup the document
			officeStore.setupOfficeCabinet(documentType, indexes);
		}
	}

	@Override
	public void stopServicing() {
		// Nothing to stop
	}

}
