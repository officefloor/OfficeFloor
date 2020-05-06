package net.officefloor.jaxrs;

import java.util.Set;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.jersey.inject.hk2.ImmediateHk2InjectionManager;
import org.glassfish.jersey.internal.inject.Bindings;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.server.spi.ComponentProvider;

import net.officefloor.frame.api.manage.OfficeFloor;

/**
 * {@link OfficeFloor} {@link ComponentProvider}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorComponentProvider implements ComponentProvider {

	/**
	 * {@link InjectionManager}.
	 */
	private InjectionManager injectionManager;

	/*
	 * ================== ComponentProvider ===================
	 */

	@Override
	public void initialize(InjectionManager injectionManager) {
		this.injectionManager = injectionManager;

		// Register OfficeFloor dependencies
		this.injectionManager.register(Bindings.injectionResolver(new DependencyInjectionResolver()));

		// Register to have OfficeFloor fulfill remaining dependencies
		ImmediateHk2InjectionManager immediateInjectionManager = (ImmediateHk2InjectionManager) injectionManager;
		ServiceLocator serviceLocator = immediateInjectionManager.getServiceLocator();
		if (serviceLocator.getBestDescriptor(
				BuilderHelper.createContractFilter(OfficeFloorIntoHk2Bridge.class.getName())) == null) {

			// Add the OfficeFloor bridge
			DynamicConfigurationService configurationService = serviceLocator
					.getService(DynamicConfigurationService.class);
			if (configurationService != null) {
				DynamicConfiguration configuration = configurationService.createDynamicConfiguration();
				configuration.addActiveDescriptor(OfficeFloorIntoHk2BridgeImpl.class);
				configuration.commit();
			}
		}
		OfficeFloorIntoHk2Bridge officeFloorBridge = injectionManager.getInstance(OfficeFloorIntoHk2Bridge.class);
		officeFloorBridge.bridgeOfficeFloor();
	}

	@Override
	public boolean bind(Class<?> component, Set<Class<?>> providerContracts) {

		// TODO determine if handle
		System.out.println("bind " + component.getName() + " (" + providerContracts + ")");
		return false;
	}

	@Override
	public void done() {
		// Nothing to complete

		// TODO REMOVE
		System.out.println("done");
	}

}