package net.officefloor.cabinet.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import lombok.Data;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;

/**
 * Tests the {@link AbstractOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeCabinetTest {

	@Test
	public void ensureDirty() throws Exception {

		// Create the cabinet
		MockOfficeCabinetMetaData<MockDocument> metaData = new MockOfficeCabinetMetaData<>(MockDocument.class);
		try (MockOfficeCabinet<MockDocument> cabinet = new MockOfficeCabinet<>(metaData)) {

			// Create entity
			MockDocument document = cabinet.createManagedDocument();
			assertTrue(document instanceof ManagedDocument, "Should be instance of managed document");
			ManagedDocument managed = (ManagedDocument) document;
			assertFalse(managed.$$OfficeFloor$$_getManagedDocumentState().isDirty, "Should not be dirty");

			// Make change and ensure dirty
			document.setValue(1);
			assertTrue(managed.$$OfficeFloor$$_getManagedDocumentState().isDirty, "Should now be dirty");
		}
	}

	/**
	 * Mock {@link Document}.
	 */
	@Data
	@Document
	public static class MockDocument {

		@Key
		private String key;

		private int value;
	}

	/**
	 * Mock {@link AbstractOfficeCabinet}.
	 */
	private static class MockOfficeCabinet<D> extends AbstractOfficeCabinet<D, MockOfficeCabinetMetaData<D>> {

		private D retrieved = null;

		private String storedKey = "TEST_KEY";

		private D stored = null;

		private MockOfficeCabinet(MockOfficeCabinetMetaData<D> metaData) {
			super(metaData);
		}

		/*
		 * ===================== AbstractOfficeCabinet =======================
		 */

		@Override
		public D createManagedDocument() {
			return super.createManagedDocument();
		}

		@Override
		protected D _retrieveByKey(String key) {
			return this.retrieved;
		}

		@Override
		protected String _store(D document) {
			this.stored = document;
			return this.storedKey;
		}
	}

	/**
	 * Mock {@link AbstractOfficeCabinetMetaData} for testing.
	 */
	private static class MockOfficeCabinetMetaData<D> extends AbstractOfficeCabinetMetaData<D> {

		private MockOfficeCabinetMetaData(Class<D> documentType) throws Exception {
			super(documentType);
		}
	}

}