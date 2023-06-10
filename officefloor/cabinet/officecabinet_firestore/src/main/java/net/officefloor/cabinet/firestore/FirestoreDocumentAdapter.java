package net.officefloor.cabinet.firestore;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.common.AbstractOfficeStore;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.FieldValueDeserialiser;
import net.officefloor.cabinet.common.adapt.FieldValueGetter;
import net.officefloor.cabinet.common.adapt.FieldValueTranslator;
import net.officefloor.cabinet.common.adapt.ScalarFieldValueGetter;

/**
 * {@link Firestore} {@link AbstractDocumentAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreDocumentAdapter extends AbstractDocumentAdapter<DocumentSnapshot, Map<String, Object>> {

	/**
	 * Convenience to add a field type.
	 * 
	 * @param <V>          Type of {@link Field}.
	 * @param init         {@link Initialise}.
	 * @param fieldType    {@link Field} type.
	 * @param getter       {@link FieldValueGetter}.
	 * @param deserialiser {@link FieldValueDeserialiser}.
	 */
	private static <V> void addFieldType(Initialise init, Class<V> fieldType,
			ScalarFieldValueGetter<DocumentSnapshot, V> getter, FieldValueDeserialiser<V> deserialiser) {
		init.addFieldType(fieldType, getter, (fieldName, fieldValue) -> fieldValue, (map, fieldName, value, change) -> {
			if (value != null) {
				map.put(fieldName, value);
			}
		}, serialiser(), deserialiser);
	}

	/**
	 * Convenience to add a field type.
	 * 
	 * @param <V>            Type of {@link Field}.
	 * @param init           {@link Initialise}.
	 * @param fieldType      {@link Field} type.
	 * @param boxedFieldType Auto-boxed {@link Field} type.
	 * @param getter         {@link FieldValueGetter}.
	 * @param deserialiser   {@link FieldValueDeserialiser}.
	 */
	private static <V> void addFieldType(Initialise init, Class<V> fieldType, Class<V> boxedFieldType,
			ScalarFieldValueGetter<DocumentSnapshot, V> getter, FieldValueDeserialiser<V> deserialiser) {
		addFieldType(init, fieldType, getter, deserialiser);
		addFieldType(init, boxedFieldType, getter, deserialiser);
	}

	/**
	 * Convenience to add a field type.
	 * 
	 * @param <V>          Type of {@link Field}.
	 * @param init         {@link Initialise}.
	 * @param fieldType    {@link Field} type.
	 * @param getter       {@link FieldValueGetter}.
	 * @param toMap        {@link TransformToMapValue}.
	 * @param deserialiser {@link FieldValueDeserialiser}.
	 */
	private static <V, P> void addFieldType(Initialise init, Class<V> fieldType,
			ScalarFieldValueGetter<DocumentSnapshot, V> getter, Function<V, P> toMap,
			FieldValueDeserialiser<V> deserialiser) {
		init.addFieldType(fieldType, getter,
				(fieldName, fieldValue) -> fieldValue != null ? toMap.apply(fieldValue) : null,
				(map, fieldName, value, change) -> {
					if (value != null) {
						map.put(fieldName, value);
					}
				}, serialiser(), deserialiser);
	}

	/**
	 * Convenience to add a field type.
	 * 
	 * @param <V>            Type of {@link Field}.
	 * @param init           {@link Initialise}.
	 * @param fieldType      {@link Field} type.
	 * @param boxedFieldType Auto-boxed {@link Field} type.
	 * @param getter         {@link FieldValueGetter}.
	 * @param toMap          {@link Function} for {@link FieldValueTranslator}.
	 * @param deserialiser   {@link FieldValueDeserialiser}.
	 */
	private static <V, M> void addFieldType(Initialise init, Class<V> fieldType, Class<V> boxedFieldType,
			ScalarFieldValueGetter<DocumentSnapshot, V> getter, Function<V, M> toMap,
			FieldValueDeserialiser<V> deserialiser) {
		addFieldType(init, fieldType, getter, toMap, deserialiser);
		addFieldType(init, boxedFieldType, getter, toMap, deserialiser);
	}

	/**
	 * Instantiate.
	 * 
	 * @param officeStore {@link AbstractOfficeStore}.
	 */
	public FirestoreDocumentAdapter(
			AbstractOfficeStore<FirestoreDocumentMetaData<?>, FirestoreTransaction> officeStore) {
		super(officeStore);
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

		// Primitives
		addFieldType(init, boolean.class, Boolean.class, DocumentSnapshot::getBoolean,
				deserialiser(Boolean::parseBoolean));
		addFieldType(init, byte.class, Byte.class, (snapshot, fieldName) -> {
			Long value = snapshot.getLong(fieldName);
			return value != null ? value.byteValue() : null;
		}, Byte::intValue, deserialiser(Byte::parseByte));
		addFieldType(init, short.class, Short.class, (snapshot, fieldName) -> {
			Long value = snapshot.getLong(fieldName);
			return value != null ? value.shortValue() : null;
		}, Short::intValue, deserialiser(Short::parseShort));
		addFieldType(init, int.class, Integer.class, (snapshot, fieldName) -> {
			Long value = snapshot.getLong(fieldName);
			return value != null ? value.intValue() : null;
		}, deserialiser(Integer::parseInt));
		addFieldType(init, long.class, Long.class, DocumentSnapshot::getLong, deserialiser(Long::parseLong));
		addFieldType(init, float.class, Float.class, (snapshot, fieldName) -> {
			Double value = snapshot.getDouble(fieldName);
			return value != null ? value.floatValue() : null;
		}, deserialiser(Float::parseFloat));
		addFieldType(init, double.class, Double.class, DocumentSnapshot::getDouble, deserialiser(Double::parseDouble));
		addFieldType(init, char.class, Character.class, (snapshot, fieldName) -> {
			String value = snapshot.getString(fieldName);
			return value != null ? value.charAt(0) : null;
		}, (value) -> String.valueOf(value), charDeserialiser());

		// Open types
		addFieldType(init, String.class, DocumentSnapshot::getString, deserialiser((text) -> text));

		// Section types
		init.addFieldType(Map.class, (snapshot, fieldName) -> (Map<String, Object>) snapshot.get(fieldName),
				(fieldName, fieldValue) -> fieldValue, (map, fieldName, value, change) -> {
					if (value != null) {
						map.put(fieldName, value);
					}
				}, notSerialiseable(), notDeserialiseable(Map.class));
	}

}
