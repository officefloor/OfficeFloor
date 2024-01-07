package net.officefloor.cabinet.validate;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.Test;

import net.officefloor.cabinet.AbstractOfficeCabinetTestCase;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.MCabinet;
import net.officefloor.cabinet.MStore;

/**
 * Tests to ensure validation of the {@link Document}.
 */
public abstract class AbstractOfficeCabinetValidateTest {

	/**
	 * Obtains the test case.
	 * 
	 * @return {@link AbstractOfficeCabinetTestCase}.
	 */
	protected abstract AbstractOfficeCabinetTestCase testcase();

	/**
	 * Ensure pass with valid {@link Document}.
	 */
	@Test
	@MStore(cabinets = @MCabinet(ValidDocument.class))
	public void validDocument() {
		// as run, ensure valid document
	}

	/**
	 * Validate to have default {@link Constructor}.
	 */
	@Test
	@MStore(cabinets = @MCabinet(ValidDocument.class))
	public void failOnNoDefaultConstructor() throws Exception {
		try {
			this.testcase().officeStore.setupOfficeCabinet(NoDefaultConstructorDocument.class);
		} catch (IllegalStateException ex) {
			assertEquals("Must have default constructor for " + Document.class.getSimpleName() + " type "
					+ NoDefaultConstructorDocument.class.getName(), ex.getMessage());
		}
	}

	public static class NoDefaultConstructorDocument {
		public NoDefaultConstructorDocument(String noDefaultConstructor) {
			// ignore
		}
	}

	/**
	 * Validate to ensure have {@link Key}.
	 */
	@Test
	@MStore(cabinets = @MCabinet(ValidDocument.class))
	public void failOnNoKey() throws Exception {
		try {
			this.testcase().officeStore.setupOfficeCabinet(NoKeyDocument.class);
		} catch (IllegalStateException ex) {
			assertEquals("Must annotate one field with Key for Document type " + NoKeyDocument.class.getName(),
					ex.getMessage());
		}
	}

	public static class NoKeyDocument {
	}

}