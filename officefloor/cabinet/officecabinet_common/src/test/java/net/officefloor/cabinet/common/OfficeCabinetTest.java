package net.officefloor.cabinet.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.jupiter.api.Test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.common.adapt.Index;
import net.officefloor.cabinet.common.manage.ManagedDocument;
import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;
import net.officefloor.cabinet.common.metadata.AbstractSectionMetaData;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Query.QueryField;

/**
 * Tests the {@link AbstractOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeCabinetTest {

	/**
	 * Test {@link Key}.
	 */
	private static String KEY = "TEST_KEY";

	/**
	 * Test {@link Query}.
	 */
	private static Query INDEX = new Query(new QueryField("value", 1));

	/**
	 * {@link MockDocumentAdapter}.
	 */
	private final MockDocumentAdapter ADAPTER = new MockDocumentAdapter();

	/**
	 * Ensure able to retrieve {@link Document} by {@link Key}.
	 */
	@Test
	public void retrieveByKey() throws Exception {
		try (MockOfficeCabinet<MockDocument> cabinet = this.mockOfficeCabinet(MockDocument.class)) {

			// Retrieve the document
			MockDocument document = cabinet.retrieveByKey(KEY).get();
			assertEquals(KEY, document.getKey(), "Incorrect key");
			assertEquals(1, document.getValue(), "Incorrect value");
			MockSection section = document.getSection();
			assertNotNull(section, "Should have section");
			assertEquals("TEST", section.getValue(), "Incorrect section value");
		}
	}

	/**
	 * Ensure able to retrieve {@link Document} instances by {@link Query}.
	 */
	@Test
	public void retrieveByIndex() throws Exception {
		try (MockOfficeCabinet<MockDocument> cabinet = this.mockOfficeCabinet(MockDocument.class)) {

			// Retrieve the documents
			Iterator<MockDocument> iterator = cabinet.retrieveByIndex(INDEX);
			assertTrue(iterator.hasNext(), "Should find document");
			MockDocument document = iterator.next();
			assertEquals(KEY, document.getKey(), "Incorrect key");
			assertEquals(1, document.getValue(), "Incorrect value");
			assertFalse(iterator.hasNext(), "Should be no further documents");
		}
	}

	/**
	 * Ensure keeps in session.
	 */
	@Test
	public void sessionByKey() throws Exception {
		try (MockOfficeCabinet<MockDocument> cabinet = this.mockOfficeCabinet(MockDocument.class)) {

			// Retrieve the document
			MockDocument document = cabinet.retrieveByKey(KEY).get();

			// Ensure appropriate session
			this.assertSession(document, cabinet);
		}
	}

	/**
	 * Ensure keeps in session.
	 */
	@Test
	public void sessionByIndex() throws Exception {
		try (MockOfficeCabinet<MockDocument> cabinet = this.mockOfficeCabinet(MockDocument.class)) {

			// Retrieve the documents
			Iterator<MockDocument> iterator = cabinet.retrieveByIndex(INDEX);
			assertTrue(iterator.hasNext(), "Should find document");
			MockDocument document = iterator.next();

			// Ensure appropriate session
			this.assertSession(document, cabinet);
		}
	}

	/**
	 * Ensure appropriate session of {@link Document} instances.
	 * 
	 * @param document {@link MockDocument} already retrieved.
	 * @param cabinet  {@link MockOfficeCabinet} managing the session.
	 */
	private void assertSession(MockDocument document, MockOfficeCabinet<MockDocument> cabinet) {

		// Ensure same instance when retrieved again
		MockDocument retrievedAgain = cabinet.retrieveByKey(KEY).get();
		assertSame(document, retrievedAgain, "Should be same instance in session by key");

		// And again via list
		Iterator<MockDocument> iterator = cabinet.retrieveByIndex(INDEX);
		assertTrue(iterator.hasNext(), "Should again find document");
		MockDocument retrievedIndexAgain = iterator.next();
		assertSame(document, retrievedIndexAgain, "SHould be same instance in session by index");
	}

	/**
	 * Ensure able to store {@link Document}.
	 */
	@Test
	@SuppressWarnings("unchecked")
	public void store() throws Exception {
		try (MockOfficeCabinet<MockDocument> cabinet = this.mockOfficeCabinet(MockDocument.class)) {

			// Store document
			assertNull(cabinet.stored, "INVALID TEST: should not have stored document");
			cabinet.store(new MockDocument(KEY, 1, new MockSection("TEST")));

			// Ensure stored
			assertEquals(KEY, cabinet.stored.getInternalDocument().get("key"), "Incorrect key");
			assertEquals(1, cabinet.stored.getInternalDocument().get("value"), "Incorrect value");
			Map<String, Object> section = (Map<String, Object>) cabinet.stored.getInternalDocument().get("section");
			assertNotNull(section, "Should store section");
			assertEquals("TEST", section.get("value"), "Incorrect section value");
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
			assertFalse(managed.get$$OfficeFloor$$_managedDocumentState().isDirty, "Should not be dirty");

			// Make change and ensure dirty
			document.setValue(1);
			assertTrue(managed.get$$OfficeFloor$$_managedDocumentState().isDirty, "Should now be dirty");
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

		private MockSection section;
	}

	/**
	 * Mock section of {@link Document}.
	 */
	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class MockSection {

		private String value;
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
			MockDocumentMetaData<D> metaData = new MockDocumentMetaData<>(ADAPTER, documentType);
			return new MockOfficeCabinet<>(metaData);
		} catch (Exception ex) {
			return fail("Failed to create " + MockOfficeCabinet.class.getSimpleName(), ex);
		}
	}

	/**
	 * Mock {@link AbstractOfficeCabinet}.
	 */
	private static class MockOfficeCabinet<D>
			extends AbstractOfficeCabinet<Map<String, Object>, Map<String, Object>, D, MockDocumentMetaData<D>> {

		private Map<String, Object> retrieved;

		private InternalDocument<Map<String, Object>> stored = null;

		private MockOfficeCabinet(MockDocumentMetaData<D> metaData) {
			super(metaData);

			// Provide retrieved entry
			this.retrieved = new HashMap<>();
			this.retrieved.put("key", KEY);
			this.retrieved.put("value", 1);
			this.retrieved.put("section", Map.of("value", "TEST"));
		}

		/*
		 * ===================== AbstractOfficeCabinet =======================
		 */

		@Override
		protected Map<String, Object> retrieveInternalDocument(String key) {
			return this.retrieved;
		}

		@Override
		protected Iterator<Map<String, Object>> retrieveInternalDocuments(Query index) {
			return Arrays.asList(this.retrieved).iterator();
		}

		@Override
		protected void storeInternalDocument(InternalDocument<Map<String, Object>> internalDocument) {
			this.stored = internalDocument;
		}
	}

	/**
	 * Mock {@link AbstractDocumentMetaData} for testing.
	 */
	private static class MockDocumentMetaData<D>
			extends AbstractDocumentMetaData<Map<String, Object>, Map<String, Object>, MockDocumentAdapter, D> {

		private MockDocumentMetaData(MockDocumentAdapter adapter, Class<D> documentType) throws Exception {
			super(adapter, documentType);
		}
	}

	private static <D> MockDocumentMetaData<D> createMockDocumentMetaData(Class<D> documentType, Index[] indexes,
			MockDocumentAdapter adapter) throws Exception {
		return new MockDocumentMetaData<>(adapter, documentType);
	}

	/**
	 * Mock {@link AbstractDocumentMetaData} for testing.
	 */
	private static class MockSectionMetaData<D> extends AbstractSectionMetaData<MockSectionAdapter, D> {

		private MockSectionMetaData(MockSectionAdapter adapter, Class<D> documentType) throws Exception {
			super(adapter, documentType);
		}
	}

	private static <D> MockSectionMetaData<D> createMockSectionMetaData(Class<D> documentType, Index[] indexes,
			MockSectionAdapter adapter) throws Exception {
		return new MockSectionMetaData<>(adapter, documentType);
	}

	/**
	 * Mock {@link AbstractDocumentAdapter} for testing.
	 */
	private static class MockDocumentAdapter
			extends AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>, MockDocumentAdapter> {

		public MockDocumentAdapter() {
			super(new MockSectionAdapter());
		}

		@Override
		protected void initialise(Initialise init) throws Exception {
			OfficeCabinetTest.initialise(init);
			init.setDocumentMetaDataFactory(OfficeCabinetTest::createMockDocumentMetaData);
		}
	}

	private static class MockSectionAdapter extends AbstractSectionAdapter<MockSectionAdapter> {

		@Override
		protected void initialise(
				AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>, MockSectionAdapter>.Initialise init)
				throws Exception {
			OfficeCabinetTest.initialise(init);
			init.setDocumentMetaDataFactory(OfficeCabinetTest::createMockSectionMetaData);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void initialise(
			AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>, ?>.Initialise init) {
		init.setInternalDocumentFactory(() -> new HashMap<>());
		init.setKeyGetter((map, keyName) -> (String) map.get(keyName));
		init.setKeySetter((map, keyName, keyValue) -> map.put(keyName, keyValue));
		for (Class<?> clazz : Arrays.asList(boolean.class, Boolean.class, byte.class, Byte.class, short.class,
				Short.class, char.class, Character.class, int.class, Integer.class, long.class, Long.class, float.class,
				Float.class, double.class, Double.class, String.class, Map.class)) {
			init.addFieldType((Class) clazz, (map, fieldName) -> map.get(fieldName),
					(map, fieldName, value) -> map.put(fieldName, value));
		}
	}

}