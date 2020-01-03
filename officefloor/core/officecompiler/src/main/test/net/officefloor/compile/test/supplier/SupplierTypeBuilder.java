package net.officefloor.compile.test.supplier;

import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.supplier.SuppliedManagedObjectSourceType;
import net.officefloor.compile.supplier.SupplierThreadLocalType;
import net.officefloor.compile.supplier.SupplierType;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.thread.ThreadSynchroniserFactory;

/**
 * Builder for the {@link SupplierType}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierTypeBuilder {

	/**
	 * Adds a {@link SupplierThreadLocalType}.
	 * 
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Object type for the {@link SupplierThreadLocalType}.
	 */
	void addSupplierThreadLocal(String qualifier, Class<?> objectType);

	/**
	 * Adds a {@link ThreadSynchroniserFactory}.
	 */
	void addThreadSynchroniser();

	/**
	 * Adds a {@link SuppliedManagedObjectSourceType}.
	 * 
	 * @param                     <O> Dependency keys type.
	 * @param                     <F> Flow keys type.
	 * @param                     <MS> {@link ManagedObjectSource} type.
	 * @param qualifier           Qualifier. May be <code>null</code>.
	 * @param objectType          Object type for the
	 *                            {@link SuppliedManagedObjectSourceType}.
	 * @param managedObjectSource Expected {@link ManagedObjectSource}.
	 * @return {@link PropertyList} to load the expected {@link Property} instances.
	 */
	<O extends Enum<O>, F extends Enum<F>, MS extends ManagedObjectSource<O, F>> PropertyList addSuppliedManagedObjectSource(
			String qualifier, Class<?> objectType, MS managedObjectSource);

}