package net.officefloor.compile.supplier;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceProperty;
import net.officefloor.compile.spi.supplier.source.SupplierSourceSpecification;

/**
 * Loads the {@link SupplierType} from the {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface SupplierLoader {

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link SupplierSourceSpecification} for the {@link SupplierSource}.
	 * 
	 * @param <S>                 {@link SupplierSource} type.
	 * @param supplierSourceClass {@link SupplierSource} class.
	 * @return {@link PropertyList} of the {@link SupplierSourceProperty} instances
	 *         of the {@link SupplierSourceSpecification} or <code>null</code> if
	 *         issue, which is reported to the {@link CompilerIssues}.
	 */
	<S extends SupplierSource> PropertyList loadSpecification(Class<S> supplierSourceClass);

	/**
	 * Loads and returns the {@link PropertyList} from the
	 * {@link SupplierSourceSpecification} for the {@link SupplierSource}.
	 * 
	 * @param supplierSource {@link SupplierSource} instance.
	 * @return {@link PropertyList} of the {@link SupplierSourceProperty} instances
	 *         of the {@link SupplierSourceSpecification} or <code>null</code> if
	 *         issue, which is reported to the {@link CompilerIssues}.
	 */
	PropertyList loadSpecification(SupplierSource supplierSource);

	/**
	 * Loads and returns {@link SupplierType} for the {@link SupplierSource}.
	 * 
	 * @param <S>                 {@link SupplierSource} type.
	 * @param supplierSourceClass Class of the {@link SupplierSource}.
	 * @param propertyList        {@link PropertyList} containing the properties to
	 *                            source the {@link SupplierType}.
	 * @return {@link SupplierType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<S extends SupplierSource> SupplierType loadSupplierType(Class<S> supplierSourceClass, PropertyList propertyList);

	/**
	 * Loads and returns {@link SupplierType} for the {@link SupplierSource}.
	 * 
	 * @param supplierSource {@link SupplierSource} instance.
	 * @param propertyList   {@link PropertyList} containing the properties to
	 *                       source the {@link SupplierType}.
	 * @return {@link SupplierType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	SupplierType loadSupplierType(SupplierSource supplierSource, PropertyList propertyList);

}