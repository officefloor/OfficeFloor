package net.officefloor.cabinet.source;

import java.util.HashSet;
import java.util.Set;

import net.officefloor.cabinet.Cabinet;
import net.officefloor.cabinet.domain.DomainCabinetFactory;
import net.officefloor.cabinet.domain.DomainCabinetManufacturer;
import net.officefloor.cabinet.domain.DomainCabinetManufacturerImpl;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.compile.managedfunction.ManagedFunctionObjectType;
import net.officefloor.compile.managedfunction.ManagedFunctionType;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.extension.OfficeExtensionContext;
import net.officefloor.compile.spi.office.extension.OfficeExtensionService;
import net.officefloor.compile.spi.office.extension.OfficeExtensionServiceFactory;
import net.officefloor.frame.api.source.ServiceContext;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * {@link OfficeExtensionService} to configure the {@link OfficeCabinet}
 * instances.
 * 
 * @author Daniel Sagenschneider
 */
public class CabinetOfficeExtensionService implements OfficeExtensionService, OfficeExtensionServiceFactory {

	/*
	 * =============== OfficeExtensionServiceFactory ==============
	 */

	@Override
	public OfficeExtensionService createService(ServiceContext context) throws Throwable {
		return this;
	}

	/*
	 * =================== OfficeExtensionService =================
	 */

	@Override
	public void extendOffice(OfficeArchitect officeArchitect, OfficeExtensionContext context) throws Exception {

		// Register the cabinet manager
		officeArchitect
				.addOfficeManagedObjectSource(CabinetManager.class.getSimpleName(),
						CabinetManagerManagedObjectSource.class.getName())
				.addOfficeManagedObject(CabinetManager.class.getSimpleName(), ManagedObjectScope.THREAD);

		// Create the domain cabinet manufacturer
		DomainCabinetManufacturer domainCabinetManufacturer = new DomainCabinetManufacturerImpl(
				context.getClassLoader());

		// Only register managed object for domain cabinet once
		Set<Class<?>> registeredDomainCabinetTypes = new HashSet<>();

		// Configure loading the cabinets
		officeArchitect.addManagedFunctionAugmentor((managedFunctionContext) -> {

			// Interrogate dependencies for cabinets
			ManagedFunctionType<?, ?> type = managedFunctionContext.getManagedFunctionType();
			for (ManagedFunctionObjectType<?> objectType : type.getObjectTypes()) {

				// Determine if cabinet
				Class<?> dependencyType = objectType.getObjectType();
				if (dependencyType.isAnnotationPresent(Cabinet.class)) {

					// Determine if already added
					if (!(registeredDomainCabinetTypes.contains(dependencyType))) {

						// Only attempt to add once
						registeredDomainCabinetTypes.add(dependencyType);

						try {
							// Create the domain cabinet factory
							DomainCabinetFactory<?> domainCabinetFactory = domainCabinetManufacturer
									.createDomainCabinetFactory(dependencyType);

							// Add the domain cabinet
							String managedObjectName = "DomainCabinet_" + dependencyType.getName();
							officeArchitect
									.addOfficeManagedObjectSource(managedObjectName,
											new DomainCabinetManagedObjectSource(dependencyType, domainCabinetFactory))
									.addOfficeManagedObject(managedObjectName, ManagedObjectScope.THREAD);

						} catch (Exception ex) {
							managedFunctionContext.addIssue("Failed creating " + Cabinet.class.getSimpleName() + " for "
									+ dependencyType.getName(), ex);
						}
					}
				}
			}
		});
	}

}