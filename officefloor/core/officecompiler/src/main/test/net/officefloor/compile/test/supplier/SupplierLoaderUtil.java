/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2013 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.officefloor.compile.test.supplier;

import junit.framework.TestCase;
import net.officefloor.autowire.spi.supplier.source.SupplierSource;
import net.officefloor.autowire.spi.supplier.source.SupplierSourceSpecification;
import net.officefloor.autowire.supplier.SupplierType;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.compile.impl.properties.PropertyListImpl;
import net.officefloor.compile.properties.Property;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.test.issues.FailTestCompilerIssues;
import net.officefloor.compile.test.properties.PropertyListUtil;

/**
 * Utility class for testing a {@link SupplierSource}.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierLoaderUtil {

	// TODO determine how to provide this
	private static interface SupplierTypeBuilder {
	}

	/**
	 * Validates the {@link SupplierSourceSpecification} for the
	 * {@link SupplierSource}.
	 * 
	 * @param <S>
	 *            {@link SupplierSource} type.
	 * @param supplierSourceClass
	 *            {@link SupplierSource} class.
	 * @param propertyNameLabels
	 *            Listing of name/label pairs for the {@link Property}
	 *            instances.
	 * @return Loaded {@link PropertyList}.
	 */
	public static <S extends SupplierSource> PropertyList validateSpecification(
			Class<S> supplierSourceClass, String... propertyNameLabels) {

		// Load the specification
		PropertyList propertyList = getOfficeFloorCompiler(null)
				.getSupplierLoader().loadSpecification(supplierSourceClass);

		// Verify the properties
		PropertyListUtil.validatePropertyNameLabels(propertyList,
				propertyNameLabels);

		// Return the property list
		return propertyList;
	}

	/**
	 * Creates the {@link SupplierTypeBuilder} to create the expected
	 * {@link SupplierType}.
	 * 
	 * @return {@link SupplierTypeBuilder} to build the expected
	 *         {@link SupplierType}.
	 */
	public static SupplierTypeBuilder createSupplierSourceContext() {
		// TODO implement
		throw new UnsupportedOperationException("TODO implement");
	}

	/**
	 * Convenience method that validates the {@link SupplierType} loaded from
	 * the input {@link SupplierSource} against the expected
	 * {@link SupplierType} from the {@link SupplierTypeBuilder}.
	 * 
	 * @param <S>
	 *            {@link SupplierSource} type.
	 * @param expectedSupplierType
	 *            {@link SupplierTypeBuilder} that has had the expected
	 *            {@link SupplierType} built against it.
	 * @param supplierSourceClass
	 *            {@link SupplierSource} class.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link SupplierSource}.
	 * @return Loaded {@link SupplierType}.
	 */
	public static <S extends SupplierSource> SupplierType validateSupplierType(
			SupplierTypeBuilder expectedSupplierType,
			Class<S> supplierSourceClass, String... propertyNameValues) {
		return validateSupplierType(expectedSupplierType, supplierSourceClass,
				null, propertyNameValues);
	}

	/**
	 * Convenience method that validates the {@link SupplierType} loaded from
	 * the input {@link SupplierSource} against the expected
	 * {@link SupplierType} from the {@link SupplierTypeBuilder}.
	 * 
	 * @param <S>
	 *            {@link SupplierSource} type.
	 * @param expectedSupplierType
	 *            {@link SupplierTypeBuilder} that has had the expected
	 *            {@link SupplierType} built against it.
	 * @param supplierSourceClass
	 *            {@link SupplierSource} class.
	 * @param compiler
	 *            {@link OfficeFloorCompiler}. May be <code>null</code>.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link SupplierSource}.
	 * @return Loaded {@link SupplierType}.
	 */
	public static <S extends SupplierSource> SupplierType validateSupplierType(
			SupplierTypeBuilder expectedSupplierType,
			Class<S> supplierSourceClass, OfficeFloorCompiler compiler,
			String... propertyNameValues) {

		// Cast to obtain expected supplier type
		if (!(expectedSupplierType instanceof SupplierType)) {
			TestCase.fail("expectedSupplierType must be created from createSupplierTypeBuilder");
		}
		SupplierType expectedSupplier = (SupplierType) expectedSupplierType;

		// TODO implement
		throw new UnsupportedOperationException(
				"TODO implement validateSupplierType " + expectedSupplier);
	}

	/**
	 * Convenience method that loads the {@link SupplierType} by obtaining the
	 * {@link ClassLoader} from the {@link SupplierSource} class.
	 * 
	 * @param <S>
	 *            {@link SupplierSource} type.
	 * @param supplierSourceClass
	 *            {@link SupplierSource} class.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link SupplierSource}.
	 * @return Loaded {@link SupplierType}.
	 */
	public static <S extends SupplierSource> SupplierType loadSupplierType(
			Class<S> supplierSourceClass, String... propertyNameValues) {
		// Return the loaded supplier
		return loadSupplierType(supplierSourceClass, null, propertyNameValues);
	}

	/**
	 * Convenience method that loads the {@link SupplierType} with the provided
	 * {@link OfficeFloorCompiler}.
	 * 
	 * @param <S>
	 *            {@link SupplierSource} type.
	 * @param supplierSourceClass
	 *            {@link SupplierSource} class.
	 * @param compiler
	 *            {@link OfficeFloorCompiler}.
	 * @param propertyNameValues
	 *            Listing of name/value pairs that comprise the properties for
	 *            the {@link SupplierSource}.
	 * @return Loaded {@link SupplierType}.
	 */
	public static <S extends SupplierSource> SupplierType loadSupplierType(
			Class<S> supplierSourceClass, OfficeFloorCompiler compiler,
			String... propertyNameValues) {

		// Create the property list
		PropertyList propertyList = new PropertyListImpl();
		for (int i = 0; i < propertyNameValues.length; i += 2) {
			String name = propertyNameValues[i];
			String value = propertyNameValues[i + 1];
			propertyList.addProperty(name).setValue(value);
		}

		// Return the loaded supplier
		return getOfficeFloorCompiler(compiler).getSupplierLoader()
				.loadSupplierType(supplierSourceClass, propertyList);
	}

	/**
	 * Obtains the {@link OfficeFloorCompiler} setup for use.
	 * 
	 * @param compiler
	 *            {@link OfficeFloorCompiler}. May be <code>null</code>.
	 * @return {@link OfficeFloorCompiler}.
	 */
	private static OfficeFloorCompiler getOfficeFloorCompiler(
			OfficeFloorCompiler compiler) {
		if (compiler == null) {
			// Create the office floor compiler that fails on first issue
			compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
			compiler.setCompilerIssues(new FailTestCompilerIssues());
		}
		return compiler;
	}

	/**
	 * All access via static methods.
	 */
	private SupplierLoaderUtil() {
	}

}