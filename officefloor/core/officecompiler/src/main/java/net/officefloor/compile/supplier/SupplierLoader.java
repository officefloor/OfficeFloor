/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package net.officefloor.compile.supplier;

import net.officefloor.compile.issues.CompilerIssues;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceProperty;
import net.officefloor.compile.spi.supplier.source.SupplierSourceSpecification;

/**
 * Loads the {@link InitialSupplierType} from the {@link SupplierSource}.
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
	 * Loads and returns {@link InitialSupplierType} for the {@link SupplierSource}.
	 * 
	 * @param <S>                 {@link SupplierSource} type.
	 * @param supplierSourceClass Class of the {@link SupplierSource}.
	 * @param propertyList        {@link PropertyList} containing the properties to
	 *                            source the {@link InitialSupplierType}.
	 * @return {@link InitialSupplierType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	<S extends SupplierSource> InitialSupplierType loadInitialSupplierType(Class<S> supplierSourceClass,
			PropertyList propertyList);

	/**
	 * Loads and returns {@link InitialSupplierType} for the {@link SupplierSource}.
	 * 
	 * @param supplierSource {@link SupplierSource} instance.
	 * @param propertyList   {@link PropertyList} containing the properties to
	 *                       source the {@link InitialSupplierType}.
	 * @return {@link InitialSupplierType} or <code>null</code> if issues, which are
	 *         reported to the {@link CompilerIssues}.
	 */
	InitialSupplierType loadInitialSupplierType(SupplierSource supplierSource, PropertyList propertyList);

	/**
	 * Loads the completed {@link SupplierType}.
	 * 
	 * @param initialType    {@link InitialSupplierType}.
	 * @param availableTypes {@link AvailableType} instances.
	 * @return {@link SupplierType}.
	 */
	SupplierType loadSupplierType(InitialSupplierType initialType, AvailableType... availableTypes);

}
