package net.officefloor.jaxrs;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;

/**
 * {@link OfficeFloorIntoHk2Bridge} implementation.
 * 
 * @author Daniel Sagenschneider
 */
@Singleton
public class OfficeFloorIntoHk2BridgeImpl implements OfficeFloorIntoHk2Bridge {

	/**
	 * {@link ServiceLocator}.
	 */
	private @Inject ServiceLocator serviceLocator;

	/*
	 * ================= OfficeFloorIntoHk2Bridge ===================
	 */

	@Override
	public void bridgeOfficeFloor(OfficeFloorDependencies dependencies) {
		OfficeFloorJustInTimeInjectionResolver justInTimeResolver = new OfficeFloorJustInTimeInjectionResolver(
				dependencies, this.serviceLocator);
		ServiceLocatorUtilities.addOneConstant(this.serviceLocator, justInTimeResolver);
	}

}