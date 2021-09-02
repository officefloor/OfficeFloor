package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;
import java.util.Map;

import net.officefloor.cabinet.OfficeCabinet;

/**
 * Adapter of {@link OfficeCabinet} to underlying implementation.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSectionAdapter<A extends AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>, A>>
		extends AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>, A> {

	/*
	 * ============= Initialise convenience methods ======================
	 */

	/**
	 * Transforms the field value for the {@link Map}.
	 */
	@FunctionalInterface
	public static interface FieldValueTransform<I, O> {
		O transform(I inputValue);
	}

	/**
	 * {@link FieldValueGetter} for {@link Field} value.
	 * 
	 * @param <V> {@link Field} type.
	 * @return {@link FieldValueGetter}.
	 */
	@SuppressWarnings("unchecked")
	public static <V> FieldValueGetter<Map<String, Object>, V> getter() {
		return (map, fieldName) -> (V) map.get(fieldName);
	}

	/**
	 * {@link FieldValueGetter} for transformed {@link Field} value.
	 * 
	 * @param <M>       {@link Map} value.
	 * @param <V>       {@link Field} value.
	 * @param transform {@link FieldValueTransform}.
	 * @return {@link FieldValueGetter}.
	 */
	public static <M, V> FieldValueGetter<Map<String, Object>, V> getter(FieldValueTransform<M, V> transform) {
		return (map, fieldName) -> {
			@SuppressWarnings("unchecked")
			M mapValue = (M) map.get(fieldName);
			return mapValue != null ? transform.transform(mapValue) : null;
		};
	}

	/**
	 * {@link FieldValueSetter} for {@link Field} value.
	 * 
	 * @param <V> {@link Field} type.
	 * @return {@link FieldValueSetter}.
	 */
	public static <V> FieldValueSetter<Map<String, Object>, V> setter() {
		return (map, fieldName, value) -> map.put(fieldName, value);
	}

	/**
	 * {@link FieldValueSetter} for transformed {@link Field} value.
	 * 
	 * @param <M>       {@link Map} value.
	 * @param <V>       {@link Field} value.
	 * @param transform {@link FieldValueTransform}.
	 * @return {@link FieldValueSetter}.
	 */
	public static <M, V> FieldValueSetter<Map<String, Object>, V> setter(FieldValueTransform<V, M> transform) {
		return (map, fieldName, value) -> {
			M mapValue = value != null ? transform.transform(value) : null;
			map.put(fieldName, mapValue);
		};
	}

	/**
	 * Instantiate.
	 */
	public AbstractSectionAdapter() {
		super(false, null);
	}

	/**
	 * Provide default initialise for section {@link Map}.
	 */
	@Override
	protected void defaultInitialise(Initialise init) {

		// Primitives
		init.addFieldType(boolean.class, Boolean.class, getter(), setter());
		init.addFieldType(byte.class, Byte.class, getter(), setter());
		init.addFieldType(short.class, Short.class, getter(), setter());
		init.addFieldType(char.class, Character.class, getter((mapValue) -> ((String) mapValue).charAt(0)),
				setter((fieldValue) -> new String(new char[] { fieldValue })));
		init.addFieldType(int.class, Integer.class, getter(), setter());
		init.addFieldType(long.class, Long.class, getter(), setter());
		init.addFieldType(float.class, Float.class, getter(), setter());
		init.addFieldType(double.class, Double.class, getter(), setter());

		// Open types
		init.addFieldType(String.class, getter(), setter());

		// Further sections
		init.addFieldType(Map.class, getter(), setter());
	}

}