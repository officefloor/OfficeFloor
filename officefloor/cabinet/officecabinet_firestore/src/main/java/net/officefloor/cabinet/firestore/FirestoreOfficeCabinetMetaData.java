package net.officefloor.cabinet.firestore;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.AbstractOfficeCabinetMetaData;
import net.officefloor.cabinet.common.CabinetUtil;

/**
 * Meta-data for the {@link FirestoreOfficeCabinet}.
 * 
 * @author Daniel Sagenschneider
 */
public class FirestoreOfficeCabinetMetaData<D> extends AbstractOfficeCabinetMetaData<D> {

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
	static interface TransformToMapValue<F, M> {
		M toMap(F value);
	}

	@FunctionalInterface
	static interface TransformFromSnapshot<F> {
		F fromSnapshot(DocumentSnapshot snapshot, String fieldName);
	}

	static class MapValueType<F, M> {

		final TransformToMapValue<F, M> toMap;

		final TransformFromSnapshot<F> fromSnapshot;

		private MapValueType(TransformToMapValue<F, M> toMap, TransformFromSnapshot<F> fromSnapshot) {
			this.toMap = toMap;
			this.fromSnapshot = fromSnapshot;
		}
	}

	static class MapValue<F, M> {

		final Field field;

		final MapValueType<F, M> mapValueType;

		private MapValue(Field field, MapValueType<F, M> mapValueType) {
			this.field = field;
			this.mapValueType = mapValueType;
		}
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
	 * {@link MapValue} instances.
	 */
	final MapValue<?, ?>[] mapValues;

	/**
	 * Instantiate.
	 * 
	 * @param documentType Type of document.
	 * @param firestore    {@link Firestore}.
	 * @throws Exception If fails to create {@link OfficeCabinet}.
	 */
	public FirestoreOfficeCabinetMetaData(Class<D> documentType, Firestore firestore) throws Exception {
		super(documentType);
		this.firestore = firestore;

		// Obtain the collection id
		this.collectionId = CabinetUtil.getDocumentName(documentType);

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

}