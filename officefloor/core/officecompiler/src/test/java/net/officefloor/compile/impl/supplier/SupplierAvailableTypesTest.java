/*-
 * #%L
 * OfficeCompiler
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
