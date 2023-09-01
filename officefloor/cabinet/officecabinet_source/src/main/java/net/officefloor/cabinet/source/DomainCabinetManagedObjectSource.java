package net.officefloor.cabinet.source;

import net.officefloor.cabinet.Cabinet;
import net.officefloor.cabinet.domain.DomainCabinetFactory;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.frame.api.build.None;
import net.officefloor.frame.api.managedobject.CoordinatingManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.ObjectRegistry;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.managedobject.source.impl.AbstractManagedObjectSource;

/**
 * {@link ManagedObjectSource} for the {@link DomainCabinetFactory} to provide
 * {@link Cabinet} implementations.
 */
public class DomainCabinetManagedObjectSource
		extends AbstractManagedObjectSource<DomainCabinetManagedObjectSource.DependencyKeys, None> {

	/**
	 * Dependency keys.
	 */
	public static enum DependencyKeys {
		CABINET_MANAGER
	}

	/**
	 * Type created by {@link DomainCabinetFactory}.
	 */
	private final Class<?> domainCabinetType;

	/**
	 * {@link DomainCabinetFactory}.
	 */
	private final DomainCabinetFactory<?> domainCabinetFactory;

	/**
	 * Instantiate.
	 * 
	 * @param domainCabinetType    Type created by {@link DomainCabinetFactory}.
	 * @param domainCabinetFactory {@link DomainCabinetFactory}.
	 */
	public DomainCabinetManagedObjectSource(Class<?> domainCabinetType, DomainCabinetFactory<?> domainCabinetFactory) {
		this.domainCabinetType = domainCabinetType;
		this.domainCabinetFactory = domainCabinetFactory;
	}

	/*
	 * ================= ManagedObjectSource ==================
	 */

	@Override
	protected void loadSpecification(SpecificationContext context) {
		// No specification
	}

	@Override
	protected void loadMetaData(MetaDataContext<DependencyKeys, None> context) throws Exception {

		// Provide the meta-data
		context.setObjectClass(this.domainCabinetType);
		context.setManagedObjectClass(DomainCabinetManagedObject.class);
		context.addDependency(DependencyKeys.CABINET_MANAGER, CabinetManager.class);
	}

	@Override
	protected ManagedObject getManagedObject() throws Throwable {
		return new DomainCabinetManagedObject();
	}

	/**
	 * {@link ManagedObject} for the domain {@link Cabinet}.
	 */
	private class DomainCabinetManagedObject implements CoordinatingManagedObject<DependencyKeys> {

		/**
		 * {@link Cabinet}.
		 */
		private Object domainCabinet;

		/*
		 * =================== CoordinatingManagedObject =================
		 */

		@Override
		public void loadObjects(ObjectRegistry<DependencyKeys> registry) throws Throwable {

			// Obtain the cabinet manager
			CabinetManager cabinetManager = (CabinetManager) registry.getObject(DependencyKeys.CABINET_MANAGER);

			// Create the cabinet
			this.domainCabinet = DomainCabinetManagedObjectSource.this.domainCabinetFactory
					.createDomainSpecificCabinet(cabinetManager);
		}

		@Override
		public Object getObject() throws Throwable {
			return this.domainCabinet;
		}
	}

}