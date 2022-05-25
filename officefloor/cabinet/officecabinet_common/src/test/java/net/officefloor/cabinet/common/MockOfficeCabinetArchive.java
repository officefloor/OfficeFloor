package net.officefloor.cabinet.common;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Function;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.common.adapt.DocumentMetaDataFactory;
import net.officefloor.cabinet.common.adapt.FieldValueSetter;
import net.officefloor.cabinet.common.adapt.FieldValueTranslator;
import net.officefloor.cabinet.common.adapt.InternalRange;
import net.officefloor.cabinet.common.adapt.ScalarFieldValueGetter;
import net.officefloor.cabinet.common.metadata.AbstractDocumentMetaData;
import net.officefloor.cabinet.common.metadata.AbstractSectionMetaData;
import net.officefloor.cabinet.common.metadata.InternalDocument;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;
import net.officefloor.cabinet.spi.Query;

/**
 * Mock {@link OfficeCabinetArchive}.
 * 
 * @author Daniel Sagenschneider
 *
 */
public class MockOfficeCabinetArchive<D> implements OfficeCabinetArchive<D> {

	private final Class<D> documentType;

	private final Function<D, String> getKey;

	/**
	 * Instantiate.
	 * 
	 * @param documentType {@link Document} type.
	 * @param getKey       {@link Function} to obtain the key from the
	 *                     {@link Document}.
	 */
	public MockOfficeCabinetArchive(Class<D> documentType, Function<D, String> getKey) {
		this.documentType = documentType;
		this.getKey = getKey;
	}

	/*
	 * ======================= OfficeCabinetArchive ===========================
	 */

	@Override
	public OfficeCabinet<D> createOfficeCabinet() {
		try {
			MockDocumentAdapter adapter = new MockDocumentAdapter();
			MockDocumentMetaData metaData = new MockDocumentMetaData(adapter, this.documentType);
			return new MockOfficeCabinet(metaData);
		} catch (Exception ex) {
			return fail("Failed to create " + MockOfficeCabinet.class.getName(), ex);
		}
	}

	@Override
	public void close() throws Exception {
		// Do nothing
	}

	/**
	 * Mock {@link AbstractOfficeCabinet}.
	 */
	private class MockOfficeCabinet extends AbstractOfficeCabinet<D, D, D, MockDocumentMetaData> {

		private final Map<String, D> documents = new HashMap<>();

		private MockOfficeCabinet(MockDocumentMetaData metaData) {
			super(metaData, false);
		}

		/*
		 * ===================== AbstractOfficeCabinet =======================
		 */

		@Override
		protected D retrieveInternalDocument(String key) {
			D document = this.documents.get(key);
			return document;
		}

		@Override
		protected InternalDocumentBundle<D> retrieveInternalDocuments(Query query, InternalRange range) {
			return MockOfficeCabinetArchive.this.createMockInternalDocumentBundle(this.documents, query, range);
		}

		@Override
		protected void storeInternalDocument(InternalDocument<D> internalDocument) {
			D document = internalDocument.getInternalDocument();
			String key = MockOfficeCabinetArchive.this.getKey.apply(document);
			this.documents.put(key, document);
		}
	}

	private MockInternalDocumentBundle createMockInternalDocumentBundle(Map<String, D> documents, Query query,
			InternalRange range) {

//		// Determine if token
//		if (range != null) {
//			String token = range.getNextDocumentBundleToken();
//			if (token != null) {
//				// Obtain the index
//				int lastIndex = (int) range.getTokenFieldValue(QUERY_FIELD_NAME);
//
//				// Slice limit
//				Iterator<D> search = documents.iterator();
//				while (search.hasNext() && (((int) search.next().get(QUERY_FIELD_NAME)) <= lastIndex)) {
//					search.remove();
//				}
//			}
//		}
//
//		// Slice to to limit
//		int limit = (range != null) ? range.getLimit() : -1;
//		Iterator<Map<String, Object>> bundleDocuments;
//		List<Map<String, Object>> remainingDocuments;
//		if ((limit > 0) && (limit <= documents.size())) {
//			bundleDocuments = documents.subList(0, limit).iterator();
//			remainingDocuments = documents.size() > limit ? documents.subList(limit, documents.size()) : null;
//		} else {
//			bundleDocuments = documents.iterator();
//			remainingDocuments = null;
//		}
//
//		// Return the bundle documents
//		return bundleDocuments.hasNext() ? new MockInternalDocumentBundle(range, bundleDocuments, remainingDocuments)
//				: null;

		return null;
	}

	/**
	 * Mock {@link InternalDocumentBundle} for testing.
	 */
	private class MockInternalDocumentBundle implements InternalDocumentBundle<D> {

		private final Iterator<D> bundleDocuments;

		private final Query query;

		private final InternalRange range;

		private final Map<String, D> remainingDocuments;

		private MockInternalDocumentBundle(Query query, InternalRange range, Iterator<D> bundleDocuments,
				Map<String, D> remainingDocuments) {
			this.query = query;
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
		public D next() {
			return this.bundleDocuments.next();
		}

		@Override
		public InternalDocumentBundle<D> nextDocumentBundle(NextDocumentBundleContext context) {
			return (this.remainingDocuments != null)
					? createMockInternalDocumentBundle(this.remainingDocuments, this.query, this.range)
					: null;
		}

		@Override
		public String getNextDocumentBundleToken(NextDocumentBundleTokenContext<D> context) {
			return context.getLastInternalDocumentToken();
		}
	}

	/**
	 * Mock {@link AbstractDocumentMetaData} for testing.
	 */
	private class MockDocumentMetaData extends AbstractDocumentMetaData<D, D, MockDocumentAdapter, D> {

		private MockDocumentMetaData(MockDocumentAdapter adapter, Class<D> documentType) throws Exception {
			super(adapter, documentType);
		}
	}

	/**
	 * Mock {@link AbstractDocumentAdapter} for testing.
	 */
	private class MockDocumentAdapter extends AbstractDocumentAdapter<D, D, MockDocumentAdapter> {

		public MockDocumentAdapter() {
			super(new MockSectionAdapter());
		}

		@Override
		protected void initialise(Initialise init) throws Exception {
			DocumentMetaDataFactory documentMetaDataFactory = (docType, indexes,
					adapter) -> new MockDocumentMetaData((MockDocumentAdapter) adapter, docType);
			init.setDocumentMetaDataFactory(documentMetaDataFactory);
			init.setInternalDocumentFactory(() -> newDocument(MockOfficeCabinetArchive.this.documentType));
			init.setKeyGetter((document, keyName) -> getValue(document, keyName));
			init.setKeySetter((document, keyName, keyValue) -> setValue(document, keyName, keyValue));
			for (Class<?> type : new Class[] { boolean.class, Boolean.class, byte.class, Byte.class, short.class,
					char.class, Character.class, Short.class, int.class, Integer.class, long.class, Long.class,
					float.class, Float.class, double.class, Double.class, String.class, Map.class }) {
				init.addFieldType(type, getFieldValue(), getFieldTranslator(), getFieldSetter(), serialiser(),
						notDeserialiseable());
			}
		}
	}

	private static <D, V> ScalarFieldValueGetter<D, V> getFieldValue() {
		return new ScalarFieldValueGetter<D, V>() {
			@Override
			public V getValue(D internalDocument, String fieldName) {
				return MockOfficeCabinetArchive.getValue(internalDocument, fieldName);
			}
		};
	}

	private static <V> FieldValueTranslator<V, V> getFieldTranslator() {
		return new FieldValueTranslator<V, V>() {
			@Override
			public V translate(String fieldName, V fieldValue) {
				return fieldValue;
			}
		};
	}

	private static <D, V> FieldValueSetter<D, V> getFieldSetter() {
		return new FieldValueSetter<D, V>() {
			@Override
			public void setValue(D internalDocument, String fieldName, V value) {
				MockOfficeCabinetArchive.setValue(internalDocument, fieldName, value);
			}
		};
	}

	/**
	 * Mock {@link AbstractDocumentMetaData} for testing.
	 */
	private class MockSectionMetaData extends AbstractSectionMetaData<MockSectionAdapter, D> {

		private MockSectionMetaData(MockSectionAdapter adapter, Class<D> documentType) throws Exception {
			super(adapter, documentType);
		}
	}

	private class MockSectionAdapter extends AbstractSectionAdapter<MockSectionAdapter> {

		@Override
		protected void initialise(Initialise init) throws Exception {
			DocumentMetaDataFactory documentMetaDataFactory = (docType, indexes,
					adapter) -> new MockSectionMetaData((MockSectionAdapter) adapter, docType);
			init.setDocumentMetaDataFactory(documentMetaDataFactory);
			init.setKeyGetter((document, keyName) -> getValue(document, keyName));
			init.setKeySetter((document, keyName, keyValue) -> setValue(document, keyName, keyValue));
			for (Class<?> type : new Class[] { boolean.class }) {
				init.addFieldType(type, null, null, null, null, null);
			}
		}
	}

	private static <D> D newDocument(Class<D> documentType) {
		try {
			return documentType.getConstructor().newInstance();
		} catch (Exception e) {
			return fail("Unable to create document of type " + documentType.getName());
		}
	}

	@SuppressWarnings("unchecked")
	private static <D, V> V getValue(D document, String fieldName) {
		Field field = getField(document, fieldName);
		Object fieldValue;
		try {
			fieldValue = field.get(document);
		} catch (Exception ex) {
			return fail("Unable to get field " + fieldName + " from document type " + document.getClass().getName(),
					ex);
		}
		return (V) fieldValue;
	}

	private static <D, V> void setValue(D document, String fieldName, V value) {
		Field field = getField(document, fieldName);
		try {
			field.set(document, value);
		} catch (Exception ex) {
			fail("Unable to set " + fieldName + " on document type " + document.getClass().getName(), ex);
		}
	}

	private static <D> Field getField(D document, String fieldName) {

		// Search for field
		Class<?> clazz = document.getClass();
		while (clazz != null) {
			for (Field field : clazz.getDeclaredFields()) {
				if (fieldName.equals(field.getName())) {
					field.setAccessible(true);
					return field;
				}
			}
			clazz = clazz.getSuperclass();
		}

		// Should always find field
		return fail("No field " + fieldName + " on document type " + document.getClass().getName());
	}

}