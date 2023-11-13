package net.officefloor.cloud.test;

import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * {@link CloudTestService} for testing.
 */
public class MockOneCloudTestService extends AbstractMockCloudTestService {

	/**
	 * Instantiate.
	 */
	public MockOneCloudTestService() {
	}

	/**
	 * Instantiate.
	 * 
	 * @param cabinetManager {@link CabinetManager}.
	 */
	private MockOneCloudTestService(OfficeStore officeStore, CabinetManager cabinetManager) {
		super(officeStore, cabinetManager);
	}

	/*
	 * ================= AbstractMockCloudTestService ===============
	 */

	@Override
	public String getCloudServiceName() {
		return "MockOne";
	}

	@Override
	protected AbstractMockCloudTestService createMockCloudTestService(OfficeStore officeStore,
			CabinetManager cabinetManager) {
		return new MockOneCloudTestService(officeStore, cabinetManager);
	}

}