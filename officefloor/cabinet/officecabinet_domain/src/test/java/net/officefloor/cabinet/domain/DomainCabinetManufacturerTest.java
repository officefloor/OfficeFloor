package net.officefloor.cabinet.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;
import net.officefloor.cabinet.domain.impl.CabinetSessionImpl;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;
import net.officefloor.cabinet.spi.Query;
import net.officefloor.cabinet.spi.Query.QueryField;

/**
 * Tests the {@link DomainCabinetManufacturer}.
 * 
 * @author Daniel Sagenschneider
 */
public class DomainCabinetManufacturerTest {

	/**
	 * {@link DomainCabinetManufacturer} to test.
	 */
	private final DomainCabinetManufacturer manufacturer = new DomainCabinetManufacturerImpl(
			DomainCabinetManufacturerTest.class.getClassLoader());

	/**
	 * Ensure can retrieve by Id.
	 */
	@Test
	public void retrieveById() throws Exception {
		final String ID = "ID";
		MockDocument document = new MockDocument();
		DomainCabinetFactory<RetrieveById> factory = manufacturer.createDomainCabinetFactory(RetrieveById.class);
		assertMetaData(factory, e(document));
		Optional<MockDocument> optionalDocument = retrieve(factory, (cabinet) -> cabinet.retrieveById(ID),
				c(document, ID));
		assertSame(document, optionalDocument.get(), "Incorrect document retrieved");
	}

	public static interface RetrieveById {
		Optional<MockDocument> retrieveById(String id);
	}

	public static @Document class MockDocument {
		private @Key String id;
		String one;
		int two;
	}

	/**
	 * Ensure can retrieve by {@link Query}.
	 */
	@Test
	public void retrieveByQuery() throws Exception {
		MockDocument document = new MockDocument();
		DomainCabinetFactory<RetrieveByQuery> factory = manufacturer.createDomainCabinetFactory(RetrieveByQuery.class);
		assertMetaData(factory, e(document, Index.of("one", "two")));
		Iterator<MockDocument> documents = retrieve(factory, (cabinet) -> cabinet.retrieveByOneTwo("ONE", 2),
				c(document, q("one", "ONE"), q("two", 2)));
		assertTrue(documents.hasNext(), "Should have document");
		assertSame(document, documents.next(), "Incorrect document");
		assertFalse(documents.hasNext(), "Should be no further documents");
	}

	public static interface RetrieveByQuery {
		Iterator<MockDocument> retrieveByOneTwo(String one, int two);
	}

	/*
	 * ============================ Helpers ============================
	 */

	private static <D> ExpectedMetaData e(D expectedDocument, Index... expectedIndexes) {
		return e(expectedDocument.getClass(), expectedIndexes);
	}

	private static ExpectedMetaData e(Class<?> expectedDocumentType, Index... expectedIndexes) {
		return new ExpectedMetaData(expectedDocumentType, expectedIndexes);
	}

	private static class ExpectedMetaData {
		private final Class<?> expectedDocumentType;
		private final Index[] expectedIndexes;

		private ExpectedMetaData(Class<?> expectedDocumentType, Index... expectedIndexes) {
			this.expectedDocumentType = expectedDocumentType;
			this.expectedIndexes = expectedIndexes;
		}
	}

	private static void assertMetaData(DomainCabinetFactory<?> factory, ExpectedMetaData... expectedMetaDatas) {
		DomainCabinetDocumentMetaData[] metaDatas = factory.getMetaData();
		assertEquals(expectedMetaDatas.length, metaDatas.length, "Incorrect number of meta-data");

		// Compare the meta-data
		Arrays.sort(metaDatas, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.getDocumentType().getName(),
				b.getDocumentType().getName()));
		Arrays.sort(expectedMetaDatas, (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(a.expectedDocumentType.getName(),
				b.expectedDocumentType.getName()));
		for (int m = 0; m < expectedMetaDatas.length; m++) {
			DomainCabinetDocumentMetaData metaData = metaDatas[m];
			ExpectedMetaData expectedMetaData = expectedMetaDatas[m];
			String metaDataSuffix = " for meta-data " + m + " (document: " + metaData.getDocumentType().getName() + ")";
			assertEquals(expectedMetaData.expectedDocumentType, metaData.getDocumentType(),
					"Incorrect document type" + metaDataSuffix);

			// Verify the indexes
			Index[] expectedIndexes = expectedMetaData.expectedIndexes;
			Index[] actualIndexes = metaData.getIndexes();
			assertEquals(expectedIndexes.length, actualIndexes.length, "Incorrect number of indexes" + metaDataSuffix);

			// Compare the indexes
			Function<Index, String> toComparison = (index) -> String.join(",", Arrays.stream(index.getFields())
					.map((indexField) -> indexField.fieldName).collect(Collectors.toList()));
			Comparator<Index> indexComparator = (a, b) -> String.CASE_INSENSITIVE_ORDER.compare(toComparison.apply(a),
					toComparison.apply(b));
			Arrays.sort(expectedIndexes, indexComparator);
			Arrays.sort(actualIndexes, indexComparator);
			for (int i = 0; i < expectedIndexes.length; i++) {
				Index expectedIndex = expectedIndexes[i];
				Index actIndex = actualIndexes[i];
				assertEquals(toComparison.apply(expectedIndex), toComparison.apply(actIndex),
						"Incorrect index " + i + metaDataSuffix);
			}
		}
	}

	private static <C, R> R retrieve(DomainCabinetFactory<C> factory, Function<C, R> retrieve,
			MockOfficeCabinet<?>... cabinets) {
		Map<Class<?>, MockOfficeCabinet<?>> cabinetMap = new HashMap<>();
		for (MockOfficeCabinet<?> cabinet : cabinets) {
			cabinetMap.put(cabinet.documentType, cabinet);
		}
		CabinetSession session = new CabinetSessionImpl(cabinetMap);
		C cabinet = factory.createDomainSpecificCabinet(session);
		return retrieve.apply(cabinet);
	}

	@SuppressWarnings("unchecked")
	private static <D> MockOfficeCabinet<D> c(D document, String expectedKey) {
		return new MockOfficeCabinet<>((Class<D>) document.getClass(), document, expectedKey, null);
	}

	@SuppressWarnings("unchecked")
	public static <D> MockOfficeCabinet<D> c(D document, QueryField... queryFields) {
		return new MockOfficeCabinet<D>((Class<D>) document.getClass(), document, null, new Query(queryFields));
	}

	private static QueryField q(String name, Object value) {
		return new QueryField(name, value);
	}

	private static class MockOfficeCabinet<D> implements OfficeCabinetArchive<D>, OfficeCabinet<D> {

		private final Class<D> documentType;

		private final D document;

		private final String expectedKey;

		private final Query expectedQuery;

		private final List<D> storedDocuments = new LinkedList<>();

		private MockOfficeCabinet(Class<D> documentType, D document, String expectedKey, Query expectedQuery) {
			this.documentType = documentType;
			this.document = document;
			this.expectedKey = expectedKey;
			this.expectedQuery = expectedQuery;
		}

		/*
		 * =================== OfficeCabinetArchive =================
		 */

		@Override
		public OfficeCabinet<D> createOfficeCabinet() {
			return this;
		}

		@Override
		public void close() throws Exception {
			// Do nothing
		}

		/*
		 * ====================== OfficeCabinet ======================
		 */

		@Override
		public Optional<D> retrieveByKey(String key) {
			assertNotNull(this.expectedKey, "Not expecting retrieve by key");
			assertEquals(this.expectedKey, key, "Incorrect retrieve by key");
			return Optional.ofNullable(this.document);
		}

		@Override
		public Iterator<D> retrieveByQuery(Query query) {
			assertNotNull(this.expectedQuery, "No expecting retrieve by query");
			QueryField[] expectedFields = this.expectedQuery.getFields();
			QueryField[] fields = query.getFields();
			assertEquals(expectedFields.length, fields.length, "Incorrect number of query fields");
			for (int f = 0; f < expectedFields.length; f++) {
				QueryField expectedField = expectedFields[f];
				QueryField field = fields[f];
				assertEquals(expectedField.fieldName, field.fieldName, "Incorrect name for field " + f);
				assertEquals(expectedField.fieldValue, field.fieldValue,
						"Incorrect value for field " + f + " (" + expectedField.fieldName + ")");
			}
			return new Iterator<D>() {

				private int nextCount = 0;

				@Override
				public boolean hasNext() {
					return (this.nextCount++ < 1);
				}

				@Override
				public D next() {
					if (this.nextCount < 2) {
						return MockOfficeCabinet.this.document;
					} else {
						throw new NoSuchElementException();
					}
				}
			};
		}

		@Override
		public void store(D document) {
			this.storedDocuments.add(document);
		}
	}

}
