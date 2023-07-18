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

package net.officefloor.compile.integrate.officefloor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.office.source.impl.AbstractOfficeSource;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.DeployedOfficeInput;
import net.officefloor.compile.spi.officefloor.ExternalServiceInput;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.test.officefloor.CompileOfficeFloor;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.managedobject.ContextAwareManagedObject;
import net.officefloor.frame.api.managedobject.ManagedObjectContext;
import net.officefloor.frame.api.source.TestSource;
import net.officefloor.plugin.clazz.Qualified;
import net.officefloor.plugin.section.clazz.ClassSectionSource;

/**
 * Ensure can configure qualified service handling for a {@link DeployedOffice}.
 * 
 * @author Daniel Sagenschneider
 */
public class ServicingQualifiedInputTest {

	/**
	 * {@link OfficeFloor}.
	 */
	private OfficeFloor officeFloor;

	/**
	 * {@link ExternalServiceInput} qualifier one.
	 */
	private static final String QUALIFIER_ONE = "ONE";

	/**
	 * First qualified {@link ExternalServiceInput} obtained from
	 * {@link DeployedOfficeInput}.
	 */
	private ExternalServiceInput<ServiceInputObject, ServiceInputObject> externalServiceInputOne;

	/**
	 * {@link ExternalServiceInput} qualifier two.
	 */
	private static final String QUALIFIER_TWO = "TWO";

	/**
	 * Second qualified {@link ExternalServiceInput} obtained from
	 * {@link DeployedOfficeInput}.
	 */
	private ExternalServiceInput<ServiceInputObject, ServiceInputObject> externalServiceInputTwo;

	/**
	 * {@link ExternalServiceInput} wrong qualifier.
	 */
	private static final String QUALIFIER_WRONG_QUALIFIER = "WRONG";

	/**
	 * Wrong qualifier {@link ExternalServiceInput} obtained from
	 * {@link DeployedOfficeInput}.
	 */
	private ExternalServiceInput<ServiceInputObject, ServiceInputObject> externalServiceInputWrongQualifier;

	/**
	 * Not qualified {@link ExternalServiceInput} obtained from
	 * {@link DeployedOfficeInput}.
	 */
	private ExternalServiceInput<ServiceInputObject, ServiceInputObject> externalServiceInputNotQualified;

	/**
	 * Value.
	 */
	private static String value = null;

	@BeforeEach
	protected void setUp() throws Exception {

		final String OFFICE_NAME = "OFFICE";
		final MockOfficeSource officeSource = new MockOfficeSource();

		// Compile the OfficeFloor with extension
		CompileOfficeFloor compile = new CompileOfficeFloor();

		// Add the Office
		compile.officeFloor((context) -> {
			// Add the deployed office (configuring the section)
			context.getOfficeFloorDeployer().addDeployedOffice(OFFICE_NAME, officeSource, null);
		});

		// Extend the existing Office
		compile.officeFloor((context) -> {

			// Obtain the existing office (added above)
			OfficeFloorDeployer deployer = context.getOfficeFloorDeployer();
			DeployedOffice office = deployer.getDeployedOffice(OFFICE_NAME);
			assertNotNull(office, "Should have existing Office");

			// External service input one
			this.externalServiceInputOne = office.getDeployedOfficeInput("SECTION", "externalServiceInputOne")
					.addExternalServiceInput(ServiceInputObject.class, QUALIFIER_ONE, ServiceInputObject.class,
							(inputManagedObject, escalations) -> fail("Should have no escalations"));

			// External service input two
			this.externalServiceInputTwo = office.getDeployedOfficeInput("SECTION", "externalServiceInputTwo")
					.addExternalServiceInput(ServiceInputObject.class, QUALIFIER_TWO, ServiceInputObject.class,
							(inputManagedObject, escalations) -> fail("Should have no escalations"));

			// External service input wrong qualifier (as should not inject input)
			this.externalServiceInputWrongQualifier = office
					.getDeployedOfficeInput("SECTION", "externalServiceInputWrongQualifier").addExternalServiceInput(
							ServiceInputObject.class, QUALIFIER_WRONG_QUALIFIER, ServiceInputObject.class,
							(inputManagedObject, escalations) -> fail("Should have no escalations"));

			// External service input non-qualified
			this.externalServiceInputNotQualified = office
					.getDeployedOfficeInput("SECTION", "externalServiceInputNotQualified")
					.addExternalServiceInput(ServiceInputObject.class, ServiceInputObject.class,
							(inputManagedObject, escalations) -> fail("Should have no escalations"));
		});

		// Open the OfficeFloor
		this.officeFloor = compile.compileAndOpenOfficeFloor();

		// Reset value
		value = null;
	}

	@AfterEach
	protected void tearDown() throws Exception {
		if (this.officeFloor != null) {
			this.officeFloor.closeOfficeFloor();
		}
	}

	/**
	 * Ensure can invoke via first qualified {@link ExternalServiceInput}.
	 */
	@Test
	public void externalServiceInputOne() throws Exception {
		ServiceInputObject serviceObject = new ServiceInputObject(1);
		this.externalServiceInputOne.service(serviceObject, null);
		assertEquals("ONE=1:ExternalServiceInput_" + ServiceInputObject.class.getName() + "_ONE", serviceObject.value,
				"Incorrect qualification");
	}

	/**
	 * Ensure can invoke via second qualified {@link ExternalServiceInput}.
	 */
	@Test
	public void externalServiceInputTwo() throws Exception {
		ServiceInputObject serviceObject = new ServiceInputObject(2);
		this.externalServiceInputTwo.service(serviceObject, null);
		assertEquals("TWO=2:ExternalServiceInput_" + ServiceInputObject.class.getName() + "_TWO", serviceObject.value,
				"Incorrect service input servicing");
	}

	/**
	 * Ensure can invoke and wrong qualifier {@link ExternalServiceInput}.
	 */
	@Test
	public void externalServiceInputWrongQualifier() throws Exception {
		ServiceInputObject serviceObject = new ServiceInputObject(3);
		this.externalServiceInputWrongQualifier.service(serviceObject, null);
		assertEquals(value, "NULL", "Should be null input, as wrong qualifier");
	}

	/**
	 * Ensure can invoke via not qualified {@link ExternalServiceInput}.
	 */
	@Test
	public void externalServiceInputNotQualified() throws Exception {
		ServiceInputObject serviceObject = new ServiceInputObject(0);
		this.externalServiceInputNotQualified.service(serviceObject, null);
		String inputDetails = "0:ExternalServiceInput_" + ServiceInputObject.class.getName();
		assertEquals("NOT=" + inputDetails + " FALLBACK=" + inputDetails, serviceObject.value,
				"Incorrect service input servicing");
	}

	@TestSource
	public static class MockOfficeSource extends AbstractOfficeSource {

		@Override
		protected void loadSpecification(SpecificationContext context) {
		}

		@Override
		public void sourceOffice(OfficeArchitect officeArchitect, OfficeSourceContext context) throws Exception {

			// Auto-wire objects for functions
			officeArchitect.enableAutoWireObjects();

			// Add the section with functions
			officeArchitect.addOfficeSection("SECTION", ClassSectionSource.class.getName(),
					MockSection.class.getName());
		}
	}

	public static class MockSection {

		public void externalServiceInputOne(@Qualified(QUALIFIER_ONE) ServiceInputObject input) {
			input.value = "ONE=" + input.id + ":" + input.name;
		}

		public void externalServiceInputTwo(@Qualified(QUALIFIER_TWO) ServiceInputObject input) {
			input.value = "TWO=" + input.id + ":" + input.name;
		}

		public void externalServiceInputWrongQualifier(@Qualified(QUALIFIER_ONE) ServiceInputObject input) {
			ServicingQualifiedInputTest.value = (input == null) ? "NULL" : String.valueOf(input.id);
		}

		public void externalServiceInputNotQualified(ServiceInputObject input,
				@Qualified(QUALIFIER_ONE) ServiceInputObject fallback) {
			input.value = "NOT=" + input.id + ":" + input.name + " FALLBACK=" + input.id + ":" + input.name;
		}
	}

	private static class ServiceInputObject implements ContextAwareManagedObject {

		private int id;

		private String name;

		private String value = null;

		private ServiceInputObject(int id) {
			this.id = id;
		}

		@Override
		public void setManagedObjectContext(ManagedObjectContext context) {
			this.name = context.getBoundName();
		}

		@Override
		public Object getObject() throws Throwable {
			return this;
		}
	}

}
