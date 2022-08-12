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

	/**
	 * Default initialise for {@link Map} of values.
	 * 
	 * @param init {@link Initialise}.
	 */
	public static void defaultInitialiseMap(
			AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>, ?>.Initialise init) {

		// Primitives
		init.addFieldType(boolean.class, Boolean.class, getter(), translator(), setter(), serialiser(),
				deserialiser(Boolean::valueOf));
		init.addFieldType(byte.class, Byte.class, getter(), translator(), setter(), serialiser(),
				deserialiser(Byte::valueOf));
		init.addFieldType(short.class, Short.class, getter(), translator(), setter(), serialiser(),
				deserialiser(Short::valueOf));
		init.addFieldType(char.class, Character.class, getter((mapValue) -> ((String) mapValue).charAt(0)),
				translator((fieldValue) -> new String(new char[] { fieldValue })), setter(), serialiser(),
				charDeserialiser());
		init.addFieldType(int.class, Integer.class, getter(), translator(), setter(), serialiser(),
				deserialiser(Integer::valueOf));
		init.addFieldType(long.class, Long.class, getter(), translator(), setter(), serialiser(),
				deserialiser(Long::valueOf));
		init.addFieldType(float.class, Float.class, getter(), translator(), setter(), serialiser(),
				deserialiser(Float::valueOf));
		init.addFieldType(double.class, Double.class, getter(), translator(), setter(), serialiser(),
				deserialiser(Double::valueOf));

		// Open types
		init.addFieldType(String.class, getter(), translator(), setter(), serialiser(), deserialiser((value) -> value));

		// Further sections
		init.addFieldType(Map.class, getter(), translator(), setter(), notSerialiseable(),
				notDeserialiseable(Map.class));
	}

	/*
	 * ============= Initialise convenience methods ======================
	 */

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
		defaultInitialiseMap(init);
	}

}