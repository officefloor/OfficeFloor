package net.officefloor.cabinet.common;

import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import net.officefloor.cabinet.AttributeTypesDocument;
import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.HierarchicalDocument;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.common.adapt.DocumentMetaDataFactory;
import net.officefloor.cabinet.common.adapt.FieldValueDeserialiser;
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
import net.officefloor.cabinet.spi.Range.Direction;

/**
 * Mock {@link OfficeCabinetArchive}.
 * 
 * @author Daniel Sagenschneider
 *
 */
public class MockOfficeCabinetArchive<D> implements OfficeCabinetArchive<D> {

	private static final Class<?>[] FIELD_TYPES = new Class[] { boolean.class, Boolean.class, byte.class, Byte.class,
			short.class, char.class, Character.class, Short.class, int.class, Integer.class, long.class, Long.class,
			float.class, Float.class, double.class, Double.class, String.class, Map.class };

	private final Class<D> documentType;

	private final Function<D, String> getKey;

	private final Map<String, D> documents = new HashMap<>();

	private final MockDocumentMetaData metaData;

	/**
	 * Instantiate.
	 * 
	 * @param documentType {@link Document} type.
	 * @param getKey       {@link Function} to obtain the key from the
	 *                     {@link Document}.
	 * @throws Exception If fails to create {@link MockOfficeCabinetArchive}.
	 */
	public MockOfficeCabinetArchive(Class<D> documentType, Function<D, String> getKey) throws Exception {
		this.documentType = documentType;
		this.getKey = getKey;

		// Create the meta data
		MockDocumentAdapter adapter = new MockDocumentAdapter();
		this.metaData = new MockDocumentMetaData(adapter, this.documentType);
	}

	/*
	 * ======================= OfficeCabinetArchive ===========================
	 */

	@Override
	public OfficeCabinet<D> createOfficeCabinet() {
		try {
			return new MockOfficeCabinet(this.metaData);
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

		private MockOfficeCabinet(MockDocumentMetaData metaData) {
			super(metaData, false);
		}

		/*
		 * ===================== AbstractOfficeCabinet =======================
		 */

		@Override
		protected D retrieveInternalDocument(String key) {
			D document = MockOfficeCabinetArchive.this.documents.get(key);
			return document;
		}

		@Override
		protected InternalDocumentBundle<D> retrieveInternalDocuments(Query query, InternalRange range) {
			@SuppressWarnings("resource")
			MockOfficeCabinetArchive<D> archive = MockOfficeCabinetArchive.this;
			return archive.createMockInternalDocumentBundle(archive.documents, query, range);
		}

		@Override
		protected void storeInternalDocument(InternalDocument<D> internalDocument) {
			@SuppressWarnings("resource")
			MockOfficeCabinetArchive<D> archive = MockOfficeCabinetArchive.this;
			D document = internalDocument.getInternalDocument();
			String key = archive.getKey.apply(document);
			archive.documents.put(key, document);
		}
	}

	private MockInternalDocumentBundle createMockInternalDocumentBundle(Map<String, D> documents, Query query,
			InternalRange range) {

		// Obtain entries into a list
		List<D> sortedDocuments = new LinkedList<>(documents.values());

		// Remaining documents (if limiting)
		Map<String, D> remainingDocuments = new HashMap<>();

		// Apply possible range
		if (range != null) {

			// Provide appropriate sorting
			String sortFieldName = range.getFieldName();
			sortedDocuments.sort((a, b) -> {

				// Obtain the values
				Object aSortValue = getValue(a, sortFieldName);
				Object bSortValue = getValue(b, sortFieldName);

				// Provide sorting based on type
				Class<?> fieldType = getField(a, sortFieldName).getType();
				if (Number.class.isAssignableFrom(fieldType) || fieldType.isPrimitive()) {
					// Sort numerically
					double difference = ((Number) aSortValue).doubleValue() - ((Number) bSortValue).doubleValue();
					return (difference == 0) ? 0 : (difference < 0) ? -1 : 1;

				} else {
					// Sort on string value
					return String.CASE_INSENSITIVE_ORDER.compare(aSortValue.toString(), bSortValue.toString());

				}
			});

			// Determine if sort descending
			if (Direction.Descending.equals(range.getDirection())) {
				Collections.reverse(sortedDocuments);
			}

			// Determine if next token
			String token = range.getNextDocumentBundleToken();
			if (token != null) {

				// Skip values up to key
				String key = range.getTokenKeyValue();
				Iterator<D> sortedDocumentIterator = sortedDocuments.iterator();
				FOUND_KEY: while (sortedDocumentIterator.hasNext()) {

					// Remove the document (as key is for last bundle)
					D document = sortedDocumentIterator.next();
					sortedDocumentIterator.remove();

					// Determine if found key
					String documentKey = MockOfficeCabinetArchive.this.getKey.apply(document);
					if (key.equals(documentKey)) {
						break FOUND_KEY;
					}
				}
			}

			// Determine if limit results
			int limit = range.getLimit();
			if (limit > 0) {

				// Load up the remaining documents
				int sortedDocumentsSize = sortedDocuments.size();
				for (int i = limit; i < sortedDocumentsSize; i++) {
					D document = sortedDocuments.get(i);
					String key = MockOfficeCabinetArchive.this.getKey.apply(document);
					remainingDocuments.put(key, document);
				}

				// Obtain the limited number of documents
				sortedDocuments = sortedDocuments.subList(0, limit < sortedDocumentsSize ? limit : sortedDocumentsSize);
			}
		}

		// Return the document bundle
		return sortedDocuments.size() > 0
				? new MockInternalDocumentBundle(query, range, sortedDocuments.iterator(), remainingDocuments)
				: null;
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
			return ((this.remainingDocuments != null) && (this.remainingDocuments.size() > 0))
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

		private Map<Class<?>, Function<String, ?>> fieldTypeDeserialisers;

		private <T> void addFieldTypeDeserialiser(Class<T> clazz, Function<String, T> deserialiser) {
			if (this.fieldTypeDeserialisers == null) {
				this.fieldTypeDeserialisers = new HashMap<>();
			}
			this.fieldTypeDeserialisers.put(clazz, deserialiser);
		}

		private <T> void addFieldTypeDeserialiser(Class<T> primitive, Class<T> boxed,
				Function<String, T> deserialiser) {
			this.addFieldTypeDeserialiser(primitive, deserialiser);
			this.addFieldTypeDeserialiser(boxed, deserialiser);
		}

		@SuppressWarnings("unchecked")
		public <V> FieldValueDeserialiser<V> fieldTypeSerialiseable(Class<?> clazz) {
			Function<String, ?> deserialiser = this.fieldTypeDeserialisers.get(clazz);
			if (deserialiser == null) {
				return notDeserialiseable(clazz);
			}

			// Handle deserialise
			return (fieldName, serialisedValue) -> {
				if (serialisedValue == null) {
					return null;
				}
				return (V) deserialiser.apply(serialisedValue);
			};
		}

		public MockDocumentAdapter() {
			super(new MockSectionAdapter());
		}

		@Override
		@SuppressWarnings({ "rawtypes", "unchecked" })
		protected void initialise(Initialise init) throws Exception {

			// Configure the field type deserialisers
			this.addFieldTypeDeserialiser(int.class, Integer.class, Integer::parseInt);
			this.addFieldTypeDeserialiser(String.class, (value) -> value);

			// Configure
			DocumentMetaDataFactory documentMetaDataFactory = (docType, indexes,
					adapter) -> new MockDocumentMetaData((MockDocumentAdapter) adapter, docType);
			init.setDocumentMetaDataFactory(documentMetaDataFactory);
			init.setInternalDocumentFactory(() -> newDocument(MockOfficeCabinetArchive.this.documentType));
			init.setKeyGetter((document, keyName) -> getValue(document, keyName));
			init.setKeySetter((document, keyName, keyValue) -> setValue(document, keyName, keyValue));
			for (Class<?> type : FIELD_TYPES) {
				init.addFieldType(type, getFieldValue(), getFieldTranslator(), getFieldSetter(), serialiser(),
						this.fieldTypeSerialiseable(type));
			}
			init.addFieldType(Map.class, getMapFieldValue(), getFieldTranslator(), getMapFieldSetter(), serialiser(),
					notDeserialiseable(Map.class));
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

	@SuppressWarnings("rawtypes")
	private static <D> ScalarFieldValueGetter<D, Map> getMapFieldValue() {
		return new ScalarFieldValueGetter<D, Map>() {
			@Override
			public Map getValue(D internalDocument, String fieldName) {

				// Handle hierarchical document child
				if (internalDocument instanceof HierarchicalDocument) {

					// Obtain the child
					Object child = MockOfficeCabinetArchive.getValue(internalDocument, fieldName);
					if (child == null) {
						return null; // no child
					}

					// Obtain mapping of fields
					Map<String, Object> data = new HashMap<>();
					Class<?> childType = child.getClass();
					do {
						// Load field values of the object
						for (Field childField : childType.getDeclaredFields()) {
							String childFieldName = childField.getName();
							Object childFieldValue = MockOfficeCabinetArchive.getValue(child, childField);

							// Handle character
							Class<?> childFieldType = childField.getType();
							if (Character.class.isAssignableFrom(childFieldType)
									|| char.class.isAssignableFrom(childFieldType)) {
								Character childFieldCharacterValue = (Character) childFieldValue;
								childFieldValue = childFieldValue != null ? String.valueOf(childFieldCharacterValue)
										: null;
							}

							// Load the data
							data.put(childFieldName, childFieldValue);
						}

						childType = childType.getSuperclass();
					} while (childType != null);

					// Return the data for child object
					return data;

				} else {
					// Obtain raw child object
					return MockOfficeCabinetArchive.getValue(internalDocument, fieldName);
				}
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

	@SuppressWarnings("rawtypes")
	private static <D> FieldValueSetter<D, Map> getMapFieldSetter() {
		return new FieldValueSetter<D, Map>() {

			@Override
			@SuppressWarnings("unchecked")
			public void setValue(D internalDocument, String fieldName, Map value) {

				// Handle hierarchical document child
				if ((value != null) && (internalDocument instanceof HierarchicalDocument)) {
					// Provide child
					AttributeTypesDocument child = new AttributeTypesDocument();
					Map<String, Object> data = (Map<String, Object>) value;
					for (String childFieldName : data.keySet()) {
						Object childFieldValue = data.get(childFieldName);

						// Handle character
						Field childField = MockOfficeCabinetArchive.getField(child, childFieldName);
						Class<?> childFieldType = childField.getType();
						if (Character.class.isAssignableFrom(childFieldType)
								|| char.class.isAssignableFrom(childFieldType)) {
							String childFieldStringValue = (String) childFieldValue;
							childFieldValue = childFieldValue != null ? childFieldStringValue.charAt(0) : null;
						}

						// Load child value
						MockOfficeCabinetArchive.setValue(child, childFieldName, childFieldValue);
					}
					MockOfficeCabinetArchive.setValue(internalDocument, fieldName, child);

				} else {
					// Map on document
					getFieldSetter().setValue(internalDocument, fieldName, value);
				}
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
		@SuppressWarnings({ "rawtypes", "unchecked" })
		protected void initialise(Initialise init) throws Exception {
			DocumentMetaDataFactory documentMetaDataFactory = (docType, indexes,
					adapter) -> new MockSectionMetaData((MockSectionAdapter) adapter, docType);
			init.setDocumentMetaDataFactory(documentMetaDataFactory);
			init.setKeyGetter((document, keyName) -> getValue(document, keyName));
			init.setKeySetter((document, keyName, keyValue) -> setValue(document, keyName, keyValue));
		}
	}

	private static <D> D newDocument(Class<D> documentType) {
		try {
			return documentType.getConstructor().newInstance();
		} catch (Exception e) {
			return fail("Unable to create document of type " + documentType.getName());
		}
	}

	private static <D, V> V getValue(D document, String fieldName) {
		Field field = getField(document, fieldName);
		return getValue(document, field);
	}

	@SuppressWarnings("unchecked")
	private static <D, V> V getValue(D document, Field field) {
		Object fieldValue;
		try {
			field.setAccessible(true);
			fieldValue = field.get(document);
		} catch (Exception ex) {
			return fail(
					"Unable to get field " + field.getName() + " from document type " + document.getClass().getName(),
					ex);
		}
		return (V) fieldValue;
	}

	private static <D, V> void setValue(D document, String fieldName, V value) {
		Field field = getField(document, fieldName);
		try {
			field.setAccessible(true);
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