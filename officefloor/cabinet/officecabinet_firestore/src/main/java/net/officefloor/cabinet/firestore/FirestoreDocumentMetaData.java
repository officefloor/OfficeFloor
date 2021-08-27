package net.officefloor.cabinet.firestore;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.AbstractDocumentMetaData;
import net.officefloor.cabinet.common.CabinetUtil;

/**
 * Meta-data for the {@link FirestoreOfficeCabinet} {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreDocumentMetaData<D> extends AbstractDocumentMetaData<DocumentSnapshot, Map<String, Object>, D> {

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
	private static <V> void addFieldType(Initialise<DocumentSnapshot, Map<String, Object>> init, Class<V> fieldType,
			FieldValueGetter<DocumentSnapshot, V> getter) {
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
	private static <V> void addFieldType(Initialise<DocumentSnapshot, Map<String, Object>> init, Class<V> fieldType,
			Class<V> boxedFieldType, FieldValueGetter<DocumentSnapshot, V> getter) {
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
	private static <V, M> void addFieldType(Initialise<DocumentSnapshot, Map<String, Object>> init, Class<V> fieldType,
			FieldValueGetter<DocumentSnapshot, V> getter, TransformToMapValue<V, M> toMap) {
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
	private static <V, M> void addFieldType(Initialise<DocumentSnapshot, Map<String, Object>> init, Class<V> fieldType,
			Class<V> boxedFieldType, FieldValueGetter<DocumentSnapshot, V> getter, TransformToMapValue<V, M> toMap) {
		addFieldType(init, fieldType, getter, toMap);
		addFieldType(init, boxedFieldType, getter, toMap);
	}

	/**
	 * {@link Firestore}.
	 */
	final Firestore firestore;

	/**
	 * Id of {@link CollectionReference}.
	 */
	final String collectionId;

	/**
	 * Instantiate.
	 * 
	 * @param documentType Type of document.
	 * @param firestore    {@link Firestore}.
	 * @throws Exception If fails to create {@link OfficeCabinet}.
	 */
	public FirestoreDocumentMetaData(Class<D> documentType, Firestore firestore) throws Exception {
		super(documentType);
		this.firestore = firestore;

		// Obtain the collection id
		this.collectionId = CabinetUtil.getDocumentName(documentType);
	}

	@Override
	protected void initialise(Initialise<DocumentSnapshot, Map<String, Object>> init) throws Exception {

		// Internal document
		init.setInternalDocumentFactory(() -> new HashMap<>());

		// Keys
		init.setKeyGetter((snapshot, keyName) -> snapshot.getId());
		init.setKeySetter((map, keyName, keyValue) -> {
			// Key not set into map
		});

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
	}

}