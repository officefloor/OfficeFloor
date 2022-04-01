package net.officefloor.cabinet.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.DocumentBundle;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.common.adapt.InternalRange;
import net.officefloor.cabinet.common.manage.ManagedDocument;
import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;
import net.officefloor.cabinet.common.metadata.AbstractSectionMetaData;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Query.QueryField;
import net.officefloor.cabinet.spi.Range;
import net.officefloor.cabinet.spi.Range.Direction;

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
	 * Name of the {@link QueryField}.
	 */
	private static String QUERY_FIELD_NAME = "value";

	/**
	 * Test {@link Query}.
	 */
	private static Query QUERY = new Query(new QueryField(QUERY_FIELD_NAME, 1));

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
			Iterator<MockDocument> iterator = cabinet.retrieveByQuery(QUERY, null);
			assertTrue(iterator.hasNext(), "Should find document");
			MockDocument document = iterator.next();
			assertEquals(KEY, document.getKey(), "Incorrect key");
			assertEquals(1, document.getValue(), "Incorrect value");
			assertFalse(iterator.hasNext(), "Should be no further documents");
		}
	}

	/**
	 * Ensure able to retrieve {@link DocumentBundle}.
	 */
	@Test
	public void retrieveFirstBundle() throws Exception {
		try (MockOfficeCabinet<MockDocument> cabinet = this.mockOfficeCabinet(MockDocument.class)) {
			cabinet.retrieved = this.createInternalDocuments(1, 10);

			// Retrieve the document bundles
			DocumentBundle<MockDocument> bundle = cabinet.retrieveByQuery(QUERY, range(1));
			assertTrue(bundle.hasNext(), "Should find document");
			MockDocument document = bundle.next();
			assertEquals(1, document.getValue(), "Incorrect value");
			assertFalse(bundle.hasNext(), "Should be no further documents");
		}
	}

	/**
	 * Ensure able to retrieve next {@link DocumentBundle} instances of different
	 * sizes, count and repetitions.
	 */
	@ParameterizedTest(name = "Bundle size {0}, Bundle count {1}, Repeated {2}")
	@CsvSource({ "1,1,0", "1,10,0", "10,1,0", "10,10,0", "1,1,10", "1,10,10", "10,1,10", "10,10,10" })
	public void retrieveNextBundles(int bundleSize, int bundleCount, int repeated) throws Exception {
		this.retrieveBundles(
				new RetrieveMockDocuments().bundleSize(bundleSize).bundleSize(bundleCount).repeatCount(repeated));
	}

	/**
	 * Ensure able to retrieve next {@link DocumentBundle} instances by next
	 * document token of different sizes, count and repetitions.
	 */
	@ParameterizedTest(name = "Bundle size {0}, Bundle count {1}, Repeated {2}")
	@CsvSource({ "1,1,0", "1,10,0", "10,1,0", "10,10,0", "1,1,10", "1,10,10", "10,1,10", "10,10,10" })
	public void retrieveNextBundlesByNextDocumentToken(int bundleSize, int bundleCount, int repeated) throws Exception {
		this.retrieveBundles(new RetrieveMockDocuments().getNextBundle((bundle, cabinet) -> {
			String token = bundle.getNextDocumentBundleToken();
			return token == null ? null : cabinet.retrieveByQuery(QUERY, range(bundleSize, token));
		}).bundleSize(bundleSize).bundleCount(bundleCount).repeatCount(repeated));
	}

	protected static class RetrieveMockDocuments extends RetrieveBundle<MockDocument, RetrieveMockDocuments> {
		public RetrieveMockDocuments() {
			this.getFirstBundle((cabinet) -> cabinet.retrieveByQuery(QUERY, range(this.bundleSize)));
			this.getDocumentIndex((document) -> document.getValue());
			this.getNextBundle((bundle, cabinet) -> bundle.nextDocumentBundle());
		}
	}

	private <D> void retrieveBundles(RetrieveMockDocuments retrieveBundle) {
		try (MockOfficeCabinet<MockDocument> cabinet = this.mockOfficeCabinet(MockDocument.class)) {
			cabinet.retrieved = this.createInternalDocuments(1, retrieveBundle.bundleSize * retrieveBundle.bundleCount);
			this.retrieveBundles(cabinet, retrieveBundle);
		} catch (Exception ex) {
			fail(ex);
		}
	}

	protected static class RetrieveBundle<D, R extends RetrieveBundle<? extends D, R>> {
		protected int bundleSize = 1;
		protected int bundleCount = 1;
		protected Function<MockOfficeCabinet<D>, DocumentBundle<D>> getFirstBundle;
		protected int repeatCount = 0;
		protected Function<D, Integer> getDocumentIndex;
		protected BiFunction<DocumentBundle<D>, MockOfficeCabinet<D>, DocumentBundle<D>> getNextBundle;

		@SuppressWarnings("unchecked")
		public R bundleSize(int expectedBundleSize) {
			this.bundleSize = expectedBundleSize;
			return (R) this;
		}

		@SuppressWarnings("unchecked")
		public R bundleCount(int expectedBundleCount) {
			this.bundleCount = expectedBundleCount;
			return (R) this;
		}

		@SuppressWarnings("unchecked")
		public R getFirstBundle(Function<MockOfficeCabinet<D>, DocumentBundle<D>> getFirstBundle) {
			this.getFirstBundle = getFirstBundle;
			return (R) this;
		}

		@SuppressWarnings("unchecked")
		public R repeatCount(int repeatCount) {
			this.repeatCount = repeatCount;
			return (R) this;
		}

		@SuppressWarnings("unchecked")
		public R getDocumentIndex(Function<D, Integer> getDocumentIndex) {
			this.getDocumentIndex = getDocumentIndex;
			return (R) this;
		}

		@SuppressWarnings("unchecked")
		public R getNextBundle(BiFunction<DocumentBundle<D>, MockOfficeCabinet<D>, DocumentBundle<D>> getNextBundle) {
			this.getNextBundle = getNextBundle;
			return (R) this;
		}
	}

	private <D> void retrieveBundles(MockOfficeCabinet<D> cabinet,
			RetrieveBundle<D, ? extends RetrieveBundle<? extends D, ?>> retrieveBundle) {
		int documentIndex = 1;
		int bundleIndex = 0;
		DocumentBundle<D> bundle = retrieveBundle.getFirstBundle.apply(cabinet);
		do {
			int startingDocumentIndex = documentIndex;

			// Ensure correct number of documents
			int bundleDocumentCount = 0;
			while (bundle.hasNext()) {
				D document = bundle.next();
				bundleDocumentCount++;
				assertEquals(Integer.valueOf(documentIndex++), retrieveBundle.getDocumentIndex.apply(document),
						"Incorrect document in bundle " + bundleIndex);
			}
			assertEquals(retrieveBundle.bundleSize, bundleDocumentCount,
					"Incorrect number of documents for bundle " + bundleIndex);

			// Ensure able to repeat obtaining the documents from bundle
			for (int repeat = 0; repeat < retrieveBundle.repeatCount; repeat++) {
				Iterator<D> iterator = bundle.iterator();
				int repeatDocumentIndex = startingDocumentIndex;
				bundleDocumentCount = 0;
				while (iterator.hasNext()) {
					D document = iterator.next();
					bundleDocumentCount++;
					assertEquals(Integer.valueOf(repeatDocumentIndex++),
							retrieveBundle.getDocumentIndex.apply(document),
							"Incorrect document in bundle " + bundleIndex + " for repeat " + repeat);
				}
				assertEquals(retrieveBundle.bundleSize, bundleDocumentCount,
						"Incorrect number of documents for bundle " + bundleIndex + " for repeat " + repeat);
			}

			bundleIndex++;
		} while ((bundle = retrieveBundle.getNextBundle.apply(bundle, cabinet)) != null);
		assertEquals(retrieveBundle.bundleCount, bundleIndex, "Incorrect number of bundles");
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
			Iterator<MockDocument> iterator = cabinet.retrieveByQuery(QUERY, null);
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
		Iterator<MockDocument> iterator = cabinet.retrieveByQuery(QUERY, null);
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
	 * Creates an {@link InternalDocument} for testing.
	 * 
	 * @param index Index of the {@link InternalDocument}.
	 * @return {@link InternalDocument}.
	 */
	private Map<String, Object> createInternalDocument(int index) {
		Map<String, Object> document = new HashMap<>();
		document.put("key", String.valueOf(index));
		document.put("value", index);
		document.put("section", Map.of("value", "TEST_" + index));
		return document;
	}

	/**
	 * Creates a listing of {@link InternalDocument} instances for testing.
	 * 
	 * @param startingIndex Starting {@link InternalDocument} index.
	 * @param endingIndex   Ending {@link InternalDocument} index.
	 * @return Listing of {@link InternalDocument} instances,
	 */
	private List<Map<String, Object>> createInternalDocuments(int startingIndex, int endingIndex) {
		List<Map<String, Object>> documents = new ArrayList<>(endingIndex - startingIndex);
		for (int i = startingIndex; i <= endingIndex; i++) {
			documents.add(this.createInternalDocument(i));
		}
		return documents;
	}

	/**
	 * Creates a {@link Range}.
	 * 
	 * @param limit Limit.
	 * @return {@link Range}.
	 */
	private static Range range(int limit) {
		return new Range("value", Direction.Ascending, limit);
	}

	/**
	 * Creates a {@link Range}.
	 * 
	 * @param limit                   Limit.
	 * @param nextDocumentBundleToken Next {@link DocumentBundle} token.
	 * @return {@link Range}.
	 */
	private static Range range(int limit, String nextDocumentBundleToken) {
		return new Range("value", Direction.Ascending, limit, nextDocumentBundleToken);
	}

	/**
	 * Mock {@link AbstractOfficeCabinet}.
	 */
	private static class MockOfficeCabinet<D>
			extends AbstractOfficeCabinet<Map<String, Object>, Map<String, Object>, D, MockDocumentMetaData<D>> {

		private List<Map<String, Object>> retrieved;

		private InternalDocument<Map<String, Object>> stored = null;

		private MockOfficeCabinet(MockDocumentMetaData<D> metaData) {
			super(metaData, false);

			// Provide retrieved entry
			Map<String, Object> document = new HashMap<>();
			document.put("key", KEY);
			document.put("value", 1);
			document.put("section", Map.of("value", "TEST"));
			this.retrieved = Arrays.asList(document);
		}

		/*
		 * ===================== AbstractOfficeCabinet =======================
		 */

		@Override
		protected Map<String, Object> retrieveInternalDocument(String key) {
			for (Map<String, Object> entry : this.retrieved) {
				String checkKey = (String) entry.get("key");
				if (key.equals(checkKey)) {
					return entry;
				}
			}
			return null;
		}

		@Override
		protected InternalDocumentBundle<Map<String, Object>> retrieveInternalDocuments(Query index,
				InternalRange range) {
			return MockInternalDocumentBundle.createMockInternalDocumentBundle(this.retrieved, range);
		}

		@Override
		protected void storeInternalDocument(InternalDocument<Map<String, Object>> internalDocument) {
			this.stored = internalDocument;
		}
	}

	/**
	 * Mock {@link InternalDocumentBundle} for testing.
	 */
	private static class MockInternalDocumentBundle implements InternalDocumentBundle<Map<String, Object>> {

		private static MockInternalDocumentBundle createMockInternalDocumentBundle(List<Map<String, Object>> documents,
				InternalRange range) {

			// Determine if token
			if (range != null) {
				String token = range.getNextDocumentBundleToken();
				if (token != null) {
					// Obtain the index
					int lastIndex = (int) range.getTokenFieldValue(QUERY_FIELD_NAME);

					// Slice limit
					Iterator<Map<String, Object>> search = documents.iterator();
					while (search.hasNext() && (((int) search.next().get(QUERY_FIELD_NAME)) <= lastIndex)) {
						search.remove();
					}
				}
			}

			// Slice to to limit
			int limit = (range != null) ? range.getLimit() : -1;
			Iterator<Map<String, Object>> bundleDocuments;
			List<Map<String, Object>> remainingDocuments;
			if ((limit > 0) && (limit <= documents.size())) {
				bundleDocuments = documents.subList(0, limit).iterator();
				remainingDocuments = documents.size() > limit ? documents.subList(limit, documents.size()) : null;
			} else {
				bundleDocuments = documents.iterator();
				remainingDocuments = null;
			}

			// Return the bundle documents
			return bundleDocuments.hasNext()
					? new MockInternalDocumentBundle(range, bundleDocuments, remainingDocuments)
					: null;
		}

		private final Iterator<Map<String, Object>> bundleDocuments;

		private final InternalRange range;

		private final List<Map<String, Object>> remainingDocuments;

		private MockInternalDocumentBundle(InternalRange range, Iterator<Map<String, Object>> bundleDocuments,
				List<Map<String, Object>> remainingDocuments) {
			this.range = range;
			this.bundleDocuments = bundleDocuments;
			this.remainingDocuments = remainingDocuments;
		}

		/*
		 * ===================== InternalDocumentBundle =====================
		 */

		@Override
		public boolean hasNext() {
			return this.bundleDocuments.hasNext();
		}

		@Override
		public Map<String, Object> next() {
			return this.bundleDocuments.next();
		}

		@Override
		public InternalDocumentBundle<Map<String, Object>> nextDocumentBundle(NextDocumentBundleContext context) {
			return (this.remainingDocuments != null)
					? createMockInternalDocumentBundle(this.remainingDocuments, this.range)
					: null;
		}

		@Override
		public String getNextDocumentBundleToken(NextDocumentBundleTokenContext<Map<String, Object>> context) {
			return context.getLastInternalDocumentToken();
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

	private static void initialise(
			AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>, ?>.Initialise init) {
		init.setInternalDocumentFactory(() -> new HashMap<>());
		init.setKeyGetter((map, keyName) -> (String) map.get(keyName));
		init.setKeySetter((map, keyName, keyValue) -> map.put(keyName, keyValue));
		AbstractSectionAdapter.defaultInitialiseMap(init);
	}

}