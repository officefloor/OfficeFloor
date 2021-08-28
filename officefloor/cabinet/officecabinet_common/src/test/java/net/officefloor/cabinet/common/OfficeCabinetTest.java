package net.officefloor.cabinet.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.common.adapt.AbstractOfficeCabinetAdapter;
import net.officefloor.cabinet.common.manage.ManagedDocument;
import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;
import net.officefloor.cabinet.common.metadata.InternalDocument;

/**
 * Tests the {@link AbstractOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeCabinetTest {

	/**
	 * Test key.
	 */
	private static String KEY = "TEST_KEY";

	/**
	 * {@link MockOfficeCabinetAdapter}.
	 */
	private final MockOfficeCabinetAdapter ADAPTER = new MockOfficeCabinetAdapter();

	/**
	 * Ensure able to retrieve {@link Document}.
	 */
	@Test
	public void retrieve() throws Exception {
		try (MockOfficeCabinet<MockDocument> cabinet = this.mockOfficeCabinet(MockDocument.class)) {

			// Load the data
			cabinet.retrieved.put("value", 1);

			// Retrieve the document
			MockDocument document = cabinet.retrieveByKey(KEY).get();
			assertEquals(KEY, document.getKey(), "Incorrect key");
			assertEquals(1, document.getValue(), "Incorrect value");
		}
	}

	/**
	 * Ensure able to store {@link Document}.
	 */
	@Test
	public void store() throws Exception {
		try (MockOfficeCabinet<MockDocument> cabinet = this.mockOfficeCabinet(MockDocument.class)) {

			// Store document
			cabinet.store(new MockDocument(KEY, 1));

			// Ensure stored
			assertEquals(KEY, cabinet.stored.getInternalDocument().get("key"), "Incorrect key");
			assertEquals(1, cabinet.stored.getInternalDocument().get("value"), "Incorrect value");
		}
	}

	/**
	 * Ensure able to detect dirty {@link Field} change.
	 */
	@Test
	public void ensureDirty() throws Exception {
		try (MockOfficeCabinet<MockDocument> cabinet = this.mockOfficeCabinet(MockDocument.class)) {

			// Retrieve the document
			MockDocument document = cabinet.retrieveByKey(KEY).get();
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
	@AllArgsConstructor
	@NoArgsConstructor
	@Document
	public static class MockDocument {

		@Key
		private String key;

		private int value;
	}

	/**
	 * Creates the {@link MockOfficeCabinet}.
	 * 
	 * @param <D>          {@link Document} type.
	 * @param documentType {@link Document} type.
	 * @return {@link MockOfficeCabinet}.
	 */
	private <D> MockOfficeCabinet<D> mockOfficeCabinet(Class<D> documentType) {
		try {
			MockOfficeCabinetMetaData<D> metaData = new MockOfficeCabinetMetaData<>(ADAPTER, documentType);
			return new MockOfficeCabinet<>(metaData);
		} catch (Exception ex) {
			return fail("Failed to create " + MockOfficeCabinet.class.getSimpleName(), ex);
		}
	}

	/**
	 * Mock {@link AbstractOfficeCabinet}.
	 */
	private static class MockOfficeCabinet<D>
			extends AbstractOfficeCabinet<Map<String, Object>, Map<String, Object>, D, MockOfficeCabinetMetaData<D>> {

		private Map<String, Object> retrieved;

		private InternalDocument<Map<String, Object>> stored = null;

		private MockOfficeCabinet(MockOfficeCabinetMetaData<D> metaData) {
			super(metaData);

			// Provide retrieved entry
			this.retrieved = new HashMap<>();
			this.retrieved.put("key", KEY);
		}

		/*
		 * ===================== AbstractOfficeCabinet =======================
		 */

		@Override
		protected Map<String, Object> retrieveInternalDocument(String key) {
			return this.retrieved;
		}

		@Override
		protected void storeInternalDocument(InternalDocument<Map<String, Object>> internalDocument) {
			this.stored = internalDocument;
		}
	}

	/**
	 * Mock {@link AbstractDocumentMetaData} for testing.
	 */
	private static class MockOfficeCabinetMetaData<D>
			extends AbstractDocumentMetaData<Map<String, Object>, Map<String, Object>, D> {

		private MockOfficeCabinetMetaData(MockOfficeCabinetAdapter adapter, Class<D> documentType) throws Exception {
			super(adapter, documentType);
		}
	}

	/**
	 * Mock {@link AbstractOfficeCabinetAdapter} for testing.
	 */
	private static class MockOfficeCabinetAdapter
			extends AbstractOfficeCabinetAdapter<Map<String, Object>, Map<String, Object>> {

		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		protected void initialise(Initialise init) throws Exception {
			init.setInternalDocumentFactory(() -> new HashMap<>());
			init.setKeyGetter((map, keyName) -> (String) map.get(keyName));
			init.setKeySetter((map, keyName, keyValue) -> map.put(keyName, keyValue));
			for (Class<?> clazz : Arrays.asList(boolean.class, Boolean.class, byte.class, Byte.class, short.class,
					Short.class, char.class, Character.class, int.class, Integer.class, long.class, Long.class,
					float.class, Float.class, double.class, Double.class, String.class)) {
				init.addFieldType((Class) clazz, (map, fieldName) -> map.get(fieldName),
						(map, fieldName, value) -> map.put(fieldName, value));
			}
		}
	}

}