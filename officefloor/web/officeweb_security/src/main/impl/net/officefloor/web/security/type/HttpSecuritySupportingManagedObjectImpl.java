package net.officefloor.web.security.type;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import net.officefloor.compile.managedobject.ManagedObjectType;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.web.spi.security.HttpSecuritySupportingManagedObject;

/**
 * {@link HttpSecuritySupportingManagedObject} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class HttpSecuritySupportingManagedObjectImpl
		implements HttpSecuritySupportingManagedObject, HttpSecuritySupportingManagedObjectType {

	/**
	 * Name of the {@link HttpSecuritySupportingManagedObject}.
	 */
	private final String name;

	/**
	 * {@link ManagedObjectSource} for the
	 * {@link HttpSecuritySupportingManagedObject}.
	 */
	private final ManagedObjectSource<?, ?> managedObjectSource;

	/**
	 * {@link PropertyList} to configure the {@link ManagedObjectSource}.
	 */
	private final PropertyList propertyList;

	/**
	 * Object type.
	 */
	private Class<?> objectType = null;

	/**
	 * Instantiate.
	 * 
	 * @param name                Name of the
	 *                            {@link HttpSecuritySupportingManagedObject}.
	 * @param managedObjectSource {@link ManagedObjectSource} for the
	 *                            {@link HttpSecuritySupportingManagedObject}.
	 * @param propertyListFactory Factory to create a {@link PropertyList}.
	 */
	public HttpSecuritySupportingManagedObjectImpl(String name, ManagedObjectSource<?, ?> managedObjectSource,
			Supplier<PropertyList> propertyListFactory) {
		this.name = name;
		this.managedObjectSource = managedObjectSource;
		this.propertyList = propertyListFactory.get();
	}

	/**
	 * Loads the {@link HttpSecuritySupportingManagedObjectType}.
	 * 
	 * @param managedObjectTypeLoader Loader to load the {@link ManagedObjectType}.
	 * @return {@link HttpSecuritySupportingManagedObjectType}.
	 */
	public HttpSecuritySupportingManagedObjectType loadHttpSecuritySupportingManagedObjectType(
			BiFunction<ManagedObjectSource<?, ?>, PropertyList, ManagedObjectType<?>> managedObjectTypeLoader) {

		// Load the managed object type
		ManagedObjectType<?> managedObjectType = managedObjectTypeLoader.apply(this.managedObjectSource,
				this.propertyList);
		if (managedObjectType == null) {
			return null; // failed to load type
		}

		// Load the object type
		this.objectType = managedObjectType.getObjectType();

		// Return the supporting managed object type
		return this;
	}

	/*
	 * =============== HttpSecuritySupportingManagedObject ==============
	 */

	@Override
	public void addProperty(String name, String value) {
		this.propertyList.addProperty(name).setValue(value);
	}

	/*
	 * ============= HttpSecuritySupportingManagedObjectType ============
	 */

	@Override
	public String getSupportingManagedObjectName() {
		return this.name;
	}

	@Override
	public ManagedObjectSource<?, ?> getManagedObjectSource() {
		return this.managedObjectSource;
	}

	@Override
	public PropertyList getProperties() {
		return this.propertyList;
	}

	@Override
	public Class<?> getObjectType() {
		return this.objectType;
	}

}