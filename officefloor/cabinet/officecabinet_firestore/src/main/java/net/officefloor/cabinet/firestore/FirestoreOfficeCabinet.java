package net.officefloor.cabinet.firestore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.CabinetUtil;
import net.officefloor.cabinet.common.DocumentKey;

/**
 * {@link Firestore} {@link OfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreOfficeCabinet<D> implements OfficeCabinet<D> {

	/**
	 * Mapping of {@link Field} type to {@link Map} type.
	 */
	private static final Map<Class<?>, MapValueType<?, ?>> fieldTypeToMapType = new HashMap<>();

	private static <F, M> void addFielfdTypeToMapType(Class<F> fieldType, TransformToMapValue<F, M> toMap,
			TransformFromSnapshot<F> fromMap) {
		fieldTypeToMapType.put(fieldType, new MapValueType<>(toMap, fromMap));
	}

	private static <F> void addFielfdTypeToMapType(Class<F> fieldType, TransformFromSnapshot<F> fromMap) {
		fieldTypeToMapType.put(fieldType, new MapValueType<>((value) -> value, fromMap));
	}

	static {
		addFielfdTypeToMapType(boolean.class, DocumentSnapshot::getBoolean);
		addFielfdTypeToMapType(byte.class, Byte::intValue,
				(snapshot, fieldName) -> snapshot.getLong(fieldName).byteValue());
		addFielfdTypeToMapType(short.class, Short::intValue,
				(snapshot, fieldName) -> snapshot.getLong(fieldName).shortValue());
		addFielfdTypeToMapType(int.class, (snapshot, fieldName) -> snapshot.getLong(fieldName).intValue());
		addFielfdTypeToMapType(long.class, DocumentSnapshot::getLong);
		addFielfdTypeToMapType(float.class, (snapshot, fieldName) -> snapshot.getDouble(fieldName).floatValue());
		addFielfdTypeToMapType(double.class, DocumentSnapshot::getDouble);
		addFielfdTypeToMapType(char.class, (value) -> String.valueOf(value),
				(snapshot, fieldName) -> snapshot.getString(fieldName).charAt(0));
		addFielfdTypeToMapType(String.class, DocumentSnapshot::getString);
	}

	@FunctionalInterface
	private static interface TransformToMapValue<F, M> {
		M toMap(F value);
	}

	@FunctionalInterface
	private static interface TransformFromSnapshot<F> {
		F fromSnapshot(DocumentSnapshot snapshot, String fieldName);
	}

	private static class MapValueType<F, M> {

		private final TransformToMapValue<F, M> toMap;

		private final TransformFromSnapshot<F> fromSnapshot;

		private MapValueType(TransformToMapValue<F, M> toMap, TransformFromSnapshot<F> fromSnapshot) {
			this.toMap = toMap;
			this.fromSnapshot = fromSnapshot;
		}
	}

	private static class MapValue<F, M> {

		private final Field field;

		private final MapValueType<F, M> mapValueType;

		private MapValue(Field field, MapValueType<F, M> mapValueType) {
			this.field = field;
			this.mapValueType = mapValueType;
		}
	}

	/**
	 * Type of document.
	 */
	private final Class<D> documentType;

	/**
	 * {@link Firestore}.
	 */
	private final Firestore firestore;

	/**
	 * Id of {@link CollectionReference}.
	 */
	private final String collectionId;

	/**
	 * {@link DocumentKey}.
	 */
	private final DocumentKey<D> documentKey;

	/**
	 * {@link MapValue} instances.
	 */
	private final MapValue<?, ?>[] mapValues;

	/**
	 * Instantiate.
	 * 
	 * @param documentType Type of document.
	 * @param firestore    {@link Firestore}.
	 * @throws Exception If fails to create {@link OfficeCabinet}.
	 */
	public FirestoreOfficeCabinet(Class<D> documentType, Firestore firestore) throws Exception {
		this.documentType = documentType;
		this.firestore = firestore;

		// Obtain the collection id
		this.collectionId = CabinetUtil.getDocumentName(documentType);

		// Obtain the document key
		this.documentKey = CabinetUtil.getDocumentKey(documentType);

		// Load the attributes
		List<MapValue<?, ?>> mapValues = new ArrayList<>();
		CabinetUtil.processFields(this.documentType, (context) -> {

			// Ignore key
			if (context.isKey()) {
				return;
			}

			// Ensure accessible
			Field field = context.getField();
			field.setAccessible(true);

			// Determine the attribute type
			Class<?> fieldClass = field.getType();
			MapValueType<?, ?> attributeType = fieldTypeToMapType.get(fieldClass);
			if (attributeType == null) {
				// TODO load as embedded type
				throw new UnsupportedOperationException(
						"TODO implement embedded for " + field.getName() + " of type " + fieldClass.getName());
			}

			// Create and load the map value
			MapValue<?, ?> mapValue = new MapValue<>(field, attributeType);
			mapValues.add(mapValue);
		});
		this.mapValues = mapValues.toArray(MapValue[]::new);
	}

	/*
	 * =================== OfficeCabinet =======================
	 */

	@Override
	public Optional<D> retrieveByKey(String key) {

		// Retrieve the document
		DocumentReference docRef = this.firestore.collection(this.collectionId).document(key);
		try {

			// Obtain the document
			DocumentSnapshot snapshot = docRef.get().get();

			// Create the document
			D document = this.documentType.getConstructor().newInstance();

			// Load the attributes
			for (MapValue<?, ?> mapValue : this.mapValues) {

				// Obtain the value
				String fieldName = mapValue.field.getName();
				Object fieldValue = mapValue.mapValueType.fromSnapshot.fromSnapshot(snapshot, fieldName);

				// Load value to document
				mapValue.field.set(document, fieldValue);
			}

			// Return the document
			return Optional.of(document);

		} catch (Exception ex) {
			throw new IllegalStateException(
					"Failed to obtain document " + this.documentType.getName() + " by key " + key, ex);
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void store(D document) {

		// Obtain key and determine if new
		String key;
		boolean isNew = false;
		try {
			// Obtain the key for the document
			key = this.documentKey.getKey(document);
			if (key == null) {

				// Generate and load key
				key = CabinetUtil.newKey();
				this.documentKey.setKey(document, key);

				// Flag creating
				isNew = true;
			}

		} catch (Exception ex) {
			throw new IllegalStateException("Unable to store document " + document.getClass().getName(), ex);
		}

		// Create the fields to store
		Map<String, Object> fields = new HashMap<>();
		try {
			for (MapValue mapValue : this.mapValues) {

				// Obtain the name
				String fieldName = mapValue.field.getName();

				// Obtain the value
				Object fieldValue = mapValue.field.get(document);
				Object value = mapValue.mapValueType.toMap.toMap(fieldValue);

				// Load value
				fields.put(fieldName, value);
			}
		} catch (Exception ex) {
			throw new IllegalStateException(
					"Failed extracting data from document " + this.documentType.getName() + " by key " + key, ex);
		}

		// Save document
		try {
			DocumentReference docRef = this.firestore.collection(this.collectionId).document(key);
			if (isNew) {
				docRef.create(fields).get();
			} else {
				docRef.set(fields).get();
			}
		} catch (ExecutionException | InterruptedException ex) {
			throw new IllegalStateException(
					"Failed to store document " + this.documentType.getName() + " by key " + key, ex);
		}
	}

}