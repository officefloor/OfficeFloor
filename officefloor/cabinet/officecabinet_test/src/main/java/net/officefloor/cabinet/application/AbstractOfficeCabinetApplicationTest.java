package net.officefloor.cabinet.application;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

import net.officefloor.cabinet.AbstractOfficeCabinetTestCase;
import net.officefloor.cabinet.MCabinet;
import net.officefloor.cabinet.MStore;
import net.officefloor.cabinet.attributes.AttributeTypesDocument;

/**
 * Undertakes testing within an application.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractOfficeCabinetApplicationTest {

	/**
	 * Obtains the test case.
	 * 
	 * @return {@link AbstractOfficeCabinetTestCase}.
	 */
	protected abstract AbstractOfficeCabinetTestCase testcase();

	/**
	 * Ensure can store and retrieve values.
	 */
	@Test
	@MStore(cabinets = @MCabinet(AttributeTypesDocument.class))
	public void application() {

		fail("TODO implement application");
	}

}
