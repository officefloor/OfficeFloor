package net.officefloor.cabinet.firestore;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.FieldValueGetter;
import net.officefloor.cabinet.common.adapt.Index;
import net.officefloor.cabinet.common.adapt.ScalarFieldValueGetter;

/**
 * {@link Firestore} {@link AbstractDocumentAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreDocumentAdapter
		extends AbstractDocumentAdapter<DocumentSnapshot, Map<String, Object>, FirestoreDocumentAdapter> {

	/**
	 * Transforms the {@link Field} value for the {@link Map} to store.
	 */
	@FunctionalInterface
	static interface TransformToMapValue<V, M> {
		M toMap(V value);
	}

	/**
	 * Convenience to add a field type.
	 * 
	 * @param <V>       Type of {@link Field}.
	 * @param init      {@link Initialise}.
	 * @param fieldType {@link Field} type.
	 * @param getter    {@link FieldValueGetter}.
	 */
	private static <V> void addFieldType(Initialise init, Class<V> fieldType,
			ScalarFieldValueGetter<DocumentSnapshot, V> getter) {
		init.addFieldType(fieldType, getter, (map, fieldName, fieldValue) -> map.put(fieldName, fieldValue));
	}

	/**
	 * Convenience to add a field type.
	 * 
	 * @param <V>            Type of {@link Field}.
	 * @param init           {@link Initialise}.
	 * @param fieldType      {@link Field} type.
	 * @param boxedFieldType Auto-boxed {@link Field} type.
	 * @param getter         {@link FieldValueGetter}.
	 */
	private static <V> void addFieldType(Initialise init, Class<V> fieldType, Class<V> boxedFieldType,
			ScalarFieldValueGetter<DocumentSnapshot, V> getter) {
		addFieldType(init, fieldType, getter);
		addFieldType(init, boxedFieldType, getter);
	}

	/**
	 * Convenience to add a field type.
	 * 
	 * @param <V>       Type of {@link Field}.
	 * @param init      {@link Initialise}.
	 * @param fieldType {@link Field} type.
	 * @param getter    {@link FieldValueGetter}.
	 * @param toMap     {@link TransformToMapValue}.
	 */
	private static <V, M> void addFieldType(Initialise init, Class<V> fieldType,
			ScalarFieldValueGetter<DocumentSnapshot, V> getter, TransformToMapValue<V, M> toMap) {
		init.addFieldType(fieldType, getter, (map, fieldName, fieldValue) -> {
			if (fieldValue != null) {
				map.put(fieldName, toMap.toMap(fieldValue));
			}
		});
	}

	/**
	 * Convenience to add a field type.
	 * 
	 * @param <V>            Type of {@link Field}.
	 * @param init           {@link Initialise}.
	 * @param fieldType      {@link Field} type.
	 * @param boxedFieldType Auto-boxed {@link Field} type.
	 * @param getter         {@link FieldValueGetter}.
	 * @param toMap          {@link TransformToMapValue}.
	 */
	private static <V, M> void addFieldType(Initialise init, Class<V> fieldType, Class<V> boxedFieldType,
			ScalarFieldValueGetter<DocumentSnapshot, V> getter, TransformToMapValue<V, M> toMap) {
		addFieldType(init, fieldType, getter, toMap);
		addFieldType(init, boxedFieldType, getter, toMap);
	}

	/**
	 * {@link Firestore}.
	 */
	private final Firestore firestore;

	/**
	 * Instantiate.
	 * 
	 * @param firestore {@link Firestore}.
	 */
	public FirestoreDocumentAdapter(Firestore firestore) {
		super(new FirestoreSectionAdapter());
		this.firestore = firestore;
	}

	/**
	 * Creates the {@link FirestoreDocumentMetaData}.
	 * 
	 * @param <D>          Type of {@link Document}.
	 * @param documentType {@link Document} type.
	 * @param indexes      {@link Index} instances for the {@link Document}.
	 * @param adapter      {@link FirestoreDocumentAdapter}.
	 * @return {@link FirestoreDocumentMetaData}.
	 * @throws Exception If fails to create {@link FirestoreDocumentMetaData}.
	 */
	private <D> FirestoreDocumentMetaData<D> createDocumentMetaData(Class<D> documentType, Index[] indexes,
			FirestoreDocumentAdapter adapter) throws Exception {
		return new FirestoreDocumentMetaData<>(adapter, documentType, indexes, this.firestore);
	}

	/*
	 * =================== AbstractOfficeCabinetAdapter ===================
	 */

	@Override
	@SuppressWarnings("unchecked")
	protected void initialise(Initialise init) throws Exception {

		// Internal document
		init.setInternalDocumentFactory(() -> new HashMap<>());

		// Keys
		init.setKeyGetter((snapshot, keyName) -> snapshot.getId());
		init.setKeySetter((map, keyName, keyValue) -> {
			// Key not set into map
		});

		// Document meta-data
		init.setDocumentMetaDataFactory(this::createDocumentMetaData);

		// Primitives
		addFieldType(init, boolean.class, Boolean.class, DocumentSnapshot::getBoolean);
		addFieldType(init, byte.class, Byte.class, (snapshot, fieldName) -> {
			Long value = snapshot.getLong(fieldName);
			return value != null ? value.byteValue() : null;
		}, Byte::intValue);
		addFieldType(init, short.class, Short.class, (snapshot, fieldName) -> {
			Long value = snapshot.getLong(fieldName);
			return value != null ? value.shortValue() : null;
		}, Short::intValue);
		addFieldType(init, int.class, Integer.class, (snapshot, fieldName) -> {
			Long value = snapshot.getLong(fieldName);
			return value != null ? value.intValue() : null;
		});
		addFieldType(init, long.class, Long.class, DocumentSnapshot::getLong);
		addFieldType(init, float.class, Float.class, (snapshot, fieldName) -> {
			Double value = snapshot.getDouble(fieldName);
			return value != null ? value.floatValue() : null;
		});
		addFieldType(init, double.class, Double.class, DocumentSnapshot::getDouble);
		addFieldType(init, char.class, Character.class, (snapshot, fieldName) -> {
			String value = snapshot.getString(fieldName);
			return value != null ? value.charAt(0) : null;
		}, (value) -> String.valueOf(value));

		// Open types
		addFieldType(init, String.class, DocumentSnapshot::getString);

		// Section types
		addFieldType(init, Map.class, (snapshot, fieldName) -> (Map<String, Object>) snapshot.get(fieldName));
	}

}
