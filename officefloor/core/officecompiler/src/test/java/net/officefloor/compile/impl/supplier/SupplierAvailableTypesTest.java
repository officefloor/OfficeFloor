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

package net.officefloor.compile.impl.supplier;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.supplier.source.AvailableType;
import net.officefloor.compile.spi.supplier.source.SupplierSource;
import net.officefloor.compile.spi.supplier.source.SupplierSourceContext;
import net.officefloor.compile.spi.supplier.source.impl.AbstractSupplierSource;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.test.OfficeFrameTestCase;
import net.officefloor.plugin.managedobject.singleton.Singleton;

/**
 * Ensure able to list {@link AvailableType} instances.
 * 
 * @author Daniel Sagenschneider
 */
public class SupplierAvailableTypesTest extends OfficeFrameTestCase {

	/**
	 * Ensure can list {@link AvailableType} instances.
	 */
	public void testAvailableTypes() throws Exception {

		// Suppliers to capture the available types
		AvailableTypesSupplierSource officeFloorSupplier = new AvailableTypesSupplierSource();
		AvailableTypesSupplierSource officeSupplier = new AvailableTypesSupplierSource();

		// Available types only on compile
		CompileOfficeFloor compiler = new CompileOfficeFloor();
		compiler.officeFloor((context) -> {
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
			deployer.addSupplier("SUPPLIER", officeFloorSupplier);

			// Provide OfficeFloor dependencies
			DeployedOffice deployedOffice = context.getDeployedOffice();
			Singleton.load(deployer, new MockUnqualifiedOfficeFloorObject(), deployedOffice);
			Singleton.load(deployer, new MockQualifiedObject(), deployedOffice).addTypeQualification("OFFICEFLOOR",
					MockQualifiedObject.class.getName());
		});
		compiler.office((context) -> {
			OfficeArchitect office = context.getOfficeArchitect();
			office.addSupplier("SUPPLIER", officeSupplier);

			// Provide Office dependencies
			Singleton.load(office, new MockUnqualifiedOfficeObject());
			Singleton.load(office, new MockQualifiedObject()).addTypeQualification("OFFICE",
					MockQualifiedObject.class.getName());
		});
		compiler.compileOfficeFloor();

		// Validate the OfficeFloor available types
		assertAvailableTypes("OfficeFloor: ", officeFloorSupplier.availableTypes, "OFFICEFLOOR",
				MockQualifiedObject.class, null, MockUnqualifiedOfficeFloorObject.class);
		assertAvailableTypes("Office: ", officeSupplier.availableTypes, "OFFICEFLOOR", MockQualifiedObject.class, null,
				MockUnqualifiedOfficeFloorObject.class, "OFFICE", MockQualifiedObject.class, null,
				MockUnqualifiedOfficeObject.class);
	}

	/**
	 * Asserts the {@link AvailableType} instances are as expected.
	 * 
	 * @param prefix            Message prefix.
	 * @param availableTypes    {@link AvailableType} instances.
	 * @param qualiferTypePairs Expected Qualifier/Type pairs.
	 */
	private static void assertAvailableTypes(String prefix, AvailableType[] availableTypes,
			Object... qualiferTypePairs) {
		assertEquals(prefix + "incorrect number of available types", qualiferTypePairs.length / 2,
				availableTypes.length);
		for (int i = 0; i < qualiferTypePairs.length; i += 2) {
			String expectedQualifier = (String) qualiferTypePairs[i];
			Class<?> expectedType = (Class<?>) qualiferTypePairs[i + 1];
			int index = i / 2;
			AvailableType availableType = availableTypes[index];
			assertEquals(prefix + "incorrect type (index=" + index + ")", expectedType, availableType.getType());
			assertEquals(
					prefix + "incorrect qualifier (index=" + index + ", type=" + expectedType.getSimpleName() + ")",
					expectedQualifier, availableType.getQualifier());
		}
	}

	/**
	 * {@link SupplierSource} to capture the {@link AvailableType} instances.
	 */
	private static class AvailableTypesSupplierSource extends AbstractSupplierSource {

		/**
		 * Captured {@link AvailableType} instances.
		 */
		private AvailableType[] availableTypes;

		/*
		 * ===================== SupplierSource ======================
		 */

		@Override
		protected void loadSpecification(SpecificationContext context) {
			// No specification
		}

		@Override
		public void supply(SupplierSourceContext context) throws Exception {
			context.addCompileCompletion((completion) -> {
				this.availableTypes = completion.getAvailableTypes();
			});
		}

		@Override
		public void terminate() {
			// Nothing to terminate
		}
	}

	public static class MockUnqualifiedOfficeFloorObject {
	}

	public static class MockUnqualifiedOfficeObject {
	}

	public static class MockQualifiedObject {
	}
}
