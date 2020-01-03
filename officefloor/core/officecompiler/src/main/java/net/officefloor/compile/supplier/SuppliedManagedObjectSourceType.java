package net.officefloor.compile.supplier;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.plugin.section.clazz.ManagedObject;

/**
 * <code>Type definition</code> of a potentially supplied {@link ManagedObject}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SuppliedManagedObjectSourceType {

	/**
	 * Obtains the type of {@link Object} provided by the supplied
	 * {@link ManagedObject}.
	 * 
	 * @return Type of {@link Object} provided by the supplied
	 *         {@link ManagedObject}.
	 */
	Class<?> getObjectType();

	/**
	 * Obtains the possible qualifier for the supplied {@link ManagedObject}.
	 * 
	 * @return Qualifier for the supplied {@link ManagedObject}. May be
	 *         <code>null</code>.
	 */
	String getQualifier();

	/**
	 * Obtains the {@link ManagedObjectSource}.
	 * 
	 * @return {@link ManagedObjectSource}.
	 */
	ManagedObjectSource<?, ?> getManagedObjectSource();

	/**
	 * Obtains the {@link PropertyList} to configure the
	 * {@link ManagedObjectSource}.
	 * 
	 * @return {@link PropertyList} to configure the {@link ManagedObjectSource}.
	 */
	PropertyList getPropertyList();

}