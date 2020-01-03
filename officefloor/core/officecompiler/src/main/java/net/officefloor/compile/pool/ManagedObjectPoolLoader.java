package net.officefloor.compile.pool;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSource;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceProperty;
import net.officefloor.compile.spi.pool.source.ManagedObjectPoolSourceSpecification;

/**
 * Loads the {@link ManagedObjectPoolType} from the
 * {@link ManagedObjectPoolSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface ManagedObjectPoolLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link ManagedObjectPoolSourceSpecification} for the
	 * {@link ManagedObjectPoolSource}.
	 * 
	 * @param <PS>                         {@link ManagedObjectPoolSource} type.
	 * @param managedObjectPoolSourceClass Class of the
	 *                                     {@link ManagedObjectPoolSource}.
	 * @return {@link PropertyList} of the {@link ManagedObjectPoolSourceProperty}
	 *         instances of the {@link ManagedObjectPoolSourceSpecification} or
	 *         <code>null</code> if issues, which are reported to the
	 *         {@link CompilerIssues}.
	 */
	<PS extends ManagedObjectPoolSource> PropertyList loadSpecification(Class<PS> managedObjectPoolSourceClass);

	/**
	 * Loads and returns the {@link ManagedObjectPoolType} sourced from the
	 * {@link ManagedObjectPoolSource}.
	 * 
	 * @param <PS>                         {@link ManagedObjectPoolSource} type.
	 * @param managedObjectPoolSourceClass Class of the
	 *                                     {@link ManagedObjectPoolSource}.
	 * @param propertyList                 {@link PropertyList} containing the
	 *                                     properties to source the
	 *                                     {@link ManagedObjectPoolType}.
	 * @return {@link ManagedObjectPoolType} or <code>null</code> if issues, which
	 *         are reported to the {@link CompilerIssues}.
	 */
	<PS extends ManagedObjectPoolSource> ManagedObjectPoolType loadManagedObjectPoolType(
			Class<PS> managedObjectPoolSourceClass, PropertyList propertyList);

	/**
	 * Loads and returns the {@link ManagedObjectPoolType} sourced from the
	 * {@link ManagedObjectPoolSource}.
	 * 
	 * @param managedObjectPoolSource {@link ManagedObjectPoolSource} instance.
	 * @param propertyList            {@link PropertyList} containing the properties
	 *                                to source the {@link ManagedObjectPoolType}.
	 * @return {@link ManagedObjectPoolType} or <code>null</code> if issues, which
	 *         are reported to the {@link CompilerIssues}.
	 */
	ManagedObjectPoolType loadManagedObjectPoolType(ManagedObjectPoolSource managedObjectPoolSource,
			PropertyList propertyList);

}