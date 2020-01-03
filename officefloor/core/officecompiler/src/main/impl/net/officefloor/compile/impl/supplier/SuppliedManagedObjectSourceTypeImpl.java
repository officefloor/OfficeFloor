package net.officefloor.compile.impl.supplier;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.supplier.source.SuppliedManagedObjectSource;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;

/**
 * {@link SuppliedManagedObjectSourceType} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class SuppliedManagedObjectSourceTypeImpl
		implements SuppliedManagedObjectSource, SuppliedManagedObjectSourceType {

	/**
	 * Object type.
	 */
	private final Class<?> objectType;

	/**
	 * Qualifier. May be <code>null</code>.
	 */
	private final String qualifier;

	/**
	 * {@link ManagedObjectSource}.
	 */
	private final ManagedObjectSource<?, ?> managedObjectSource;

	/**
	 * {@link PropertyList}.
	 */
	private final PropertyList properties;

	/**
	 * Instantiate.
	 * 
	 * @param objectType          Object type.
	 * @param qualifier           Qualifier. May be <code>null</code>.
	 * @param managedObjectSource {@link ManagedObjectSource}.
	 * @param properties          {@link PropertyList}.
	 */
	public SuppliedManagedObjectSourceTypeImpl(Class<?> objectType, String qualifier,
			ManagedObjectSource<?, ?> managedObjectSource, PropertyList properties) {
		this.objectType = objectType;
		this.qualifier = qualifier;
		this.managedObjectSource = managedObjectSource;
		this.properties = properties;
	}

	/*
	 * ===================== SuppliedManagedObjectSource ================
	 */

	@Override
	public void addProperty(String name, String value) {
		this.properties.addProperty(name).setValue(value);
	}

	/*
	 * ======================== SuppliedManagedObjectType ======================
	 */

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

	@Override
	public String getQualifier() {
		return this.qualifier;
	}

	@Override
	public ManagedObjectSource<?, ?> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public PropertyList getPropertyList() {
		return this.properties;
	}

}