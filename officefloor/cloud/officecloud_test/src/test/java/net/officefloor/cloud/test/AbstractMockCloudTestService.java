package net.officefloor.cloud.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import net.officefloor.cabinet.inmemory.InMemoryOfficeStore;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeStore;
import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link CloudTestService} for testing.
 */
public abstract class AbstractMockCloudTestService
		implements CloudTestService, CloudTestServiceFactory, CloudTestCabinet, OfficeStore, CabinetManager {

	/**
	 * {@link OfficeStore}.
	 */
	private final OfficeStore officeStore;

	/**
	 * {@link CabinetManager}.
	 */
	private final CabinetManager cabinetManager;

	/**
	 * Indicates if the data store has been started.
	 */
	private boolean isDataStoreStarted = false;

	/**
	 * Instantiate.
	 */
	public AbstractMockCloudTestService() {
		this.officeStore = null;
		this.cabinetManager = null;
	}

	/**
	 * Instantiate.
	 * 
	 * @param officeStore    {@link OfficeStore}.
	 * @param cabinetManager {@link CabinetManager}.
	 */
	protected AbstractMockCloudTestService(OfficeStore officeStore, CabinetManager cabinetManager) {
		this.officeStore = officeStore;
		this.cabinetManager = cabinetManager;
	}

	/**
	 * Creates the {@link AbstractMockCloudTestService}.
	 * 
	 * @param officeStore    {@link OfficeStore}.
	 * @param cabinetManager {@link CabinetManager}.
	 * @return {@link AbstractMockCloudTestService}.
	 */
	protected abstract AbstractMockCloudTestService createMockCloudTestService(OfficeStore officeStore,
			CabinetManager cabinetManager);

	/*
	 * =================== CloudTestServiceFactory =================
	 */

	@Override
	public CloudTestService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * ====================== CloudTestService =====================
	 */

	@Override
	public CloudTestCabinet getCloudTestCabinet() {
		return this;
	}

	/*
	 * ==================== CloudTestCabinet ================
	 */

	@Override
	public void startDataStore() {
		this.isDataStoreStarted = true;
	}

	@Override
	public OfficeStore getOfficeStore() {
		assertTrue(this.isDataStoreStarted, "Data store should be started");
		return this.createMockCloudTestService(new InMemoryOfficeStore(), null);
	}

	@Override
	public void stopDataStore() {
		assertTrue(this.isDataStoreStarted, "Data store should be started to stop");
	}

	/*
	 * ==================== OfficeStore ================
	 */

	@Override
	public <D> void setupOfficeCabinet(Class<D> documentType, Index... indexes) throws Exception {
		this.officeStore.setupOfficeCabinet(documentType, indexes);
	}

	@Override
	public CabinetManager createCabinetManager() {
		return this.createMockCloudTestService(this.officeStore, this.officeStore.createCabinetManager());
	}

	/*
	 * ==================== CabinetManager ================
	 */

	@Override
	public <D> OfficeCabinet<D> getOfficeCabinet(Class<D> documentType) {
		return this.cabinetManager.getOfficeCabinet(documentType);
	}

	@Override
	public void flush() throws Exception {
		this.cabinetManager.flush();
	}

}