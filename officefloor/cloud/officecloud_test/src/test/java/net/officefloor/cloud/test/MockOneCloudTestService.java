package net.officefloor.cloud.test;

import net.officefloor.frame.api.source.ServiceContext;

/**
 * {@link CloudTestService} for testing.
 */
public class MockOneCloudTestService implements CloudTestService, CloudTestServiceFactory {

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
	public String getCloudServiceName() {
		return "MockOne";
	}

}