package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;
import java.util.Map;

import net.officefloor.cabinet.spi.OfficeCabinet;

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
	public static <V> ScalarFieldValueGetter<Map<String, Object>, V> getter() {
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
	public static <M, V> ScalarFieldValueGetter<Map<String, Object>, V> getter(FieldValueTransform<M, V> transform) {
		return (map, fieldName) -> {
			@SuppressWarnings("unchecked")
			M mapValue = (M) map.get(fieldName);
			return mapValue != null ? transform.transform(mapValue) : null;
		};
	}

	/**
	 * Non-translating translator.
	 * 
	 * @param <V> {@link Field} type.
	 * @return {@link FieldValueTranslator}.
	 */
	public static <V> FieldValueTranslator<V, V> translator() {
		return (fieldName, value) -> value;
	}

	/**
	 * {@link FieldValueTranslator} with {@link FieldValueTransform}.
	 * 
	 * @param <V>       {@link Field} type.
	 * @param <P>       Persistent value type.
	 * @param transform {@link FieldValueTransform}.
	 * @return {@link FieldValueTranslator}.
	 */
	public static <V, P> FieldValueTranslator<V, P> translator(FieldValueTransform<V, P> transform) {
		return (fieldName, value) -> value != null ? transform.transform(value) : null;
	}

	/**
	 * {@link FieldValueSetter} for {@link Field} value.
	 * 
	 * @param <P> Persistent value.
	 * @return {@link FieldValueSetter}.
	 */
	public static <P> FieldValueSetter<Map<String, Object>, P> setter() {
		return (map, fieldName, value) -> map.put(fieldName, value);
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
		init.addFieldType(boolean.class, Boolean.class, getter(), translator(), setter());
		init.addFieldType(byte.class, Byte.class, getter(), translator(), setter());
		init.addFieldType(short.class, Short.class, getter(), translator(), setter());
		init.addFieldType(char.class, Character.class, getter((mapValue) -> ((String) mapValue).charAt(0)),
				translator((fieldValue) -> new String(new char[] { fieldValue })), setter());
		init.addFieldType(int.class, Integer.class, getter(), translator(), setter());
		init.addFieldType(long.class, Long.class, getter(), translator(), setter());
		init.addFieldType(float.class, Float.class, getter(), translator(), setter());
		init.addFieldType(double.class, Double.class, getter(), translator(), setter());

		// Open types
		init.addFieldType(String.class, getter(), translator(), setter());

		// Further sections
		init.addFieldType(Map.class, getter(), translator(), setter());
	}

}