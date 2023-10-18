package net.officefloor.cabinet.serialise;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.officefloor.cabinet.AbstractOfficeCabinetTestCase;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.MCabinet;
import net.officefloor.cabinet.MStore;

/**
 * Tests to ensure correct serialising of {@link Document}.
 */
public abstract class AbstractOfficeCabinetSerialiseTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	/**
	 * Obtains the test case.
	 * 
	 * @return {@link AbstractOfficeCabinetTestCase}.
	 */
	protected abstract AbstractOfficeCabinetTestCase testcase();

	/**
	 * Ensure can serialise new {@link Document}.
	 */
	@Test
	@MStore(cabinets = @MCabinet(SerialisedDocument.class))
	public void serialiseNew() throws Exception {
		SerialisedDocument newDocument = new SerialisedDocument("1");
		assertEquals("{\"key\":\"1\"}", mapper.writeValueAsString(newDocument));
	}

	/**
	 * Ensure can serialise {@link Document} without meta-data.
	 */
	@Test
	@MStore(cabinets = @MCabinet(SerialisedDocument.class))
	public void serialiseRetrieved() throws Exception {
		String key = this.testcase().setupDocument(SerialisedDocument.class).getKey();
		SerialisedDocument retrievedDocument = this.testcase().officeStore.createCabinetManager()
				.getOfficeCabinet(SerialisedDocument.class).retrieveByKey(key).get();
		assertEquals("{\"key\":\"" + retrievedDocument.getKey() + "\"}", mapper.writeValueAsString(retrievedDocument));
	}

	public static class SerialisedDocument {
		private @Key String key;

		public SerialisedDocument() {
		}

		public SerialisedDocument(String key) {
			this.key = key;
		}

		public SerialisedDocument(int offset, String testName) {
			// ignore
		}

		public String getKey() {
			return this.key;
		}
	}
}
