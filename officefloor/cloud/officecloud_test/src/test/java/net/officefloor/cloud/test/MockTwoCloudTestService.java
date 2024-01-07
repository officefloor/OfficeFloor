package net.officefloor.cloud.test;

import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * {@link CloudTestService} for testing.
 */
public class MockTwoCloudTestService extends AbstractMockCloudTestService {

	/**
	 * Instantiate.
	 */
	public MockTwoCloudTestService() {
	}

	/**
	 * Instantiate.
	 * 
	 * @param cabinetManager {@link CabinetManager}.
	 */
	private MockTwoCloudTestService(OfficeStore officeStore, CabinetManager cabinetManager) {
		super(officeStore, cabinetManager);
	}

	/*
	 * ================= AbstractMockCloudTestService ===============
	 */

	@Override
	public String getCloudServiceName() {
		return "MockTwo";
	}

	@Override
	protected AbstractMockCloudTestService createMockCloudTestService(OfficeStore officeStore,
			CabinetManager cabinetManager) {
		return new MockTwoCloudTestService(officeStore, cabinetManager);
	}

}