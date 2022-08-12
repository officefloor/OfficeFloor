package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;

import net.officefloor.cabinet.common.metadata.InternalDocument;

/**
 * Meta-data of {@link Field}.
 * 
 * @author Daniel Sagenschneider
 */
public class FieldType<R, S, P, V> {

	/**
	 * {@link FieldValueGetter} from retrieved {@link InternalDocument}.
	 */
	public final FieldValueGetter<R, V> getter;

	/**
	 * {@link FieldValueTranslator} to stored persistent value.
	 */
	public final FieldValueTranslator<V, P> translator;

	/**
	 * {@link FieldValueSetter} for store {@link InternalDocument}.
	 */
	public final FieldValueSetter<S, P> setter;

	/**
	 * {@link FieldValueSerialiser} for retrieved {@link InternalDocument}.
	 */
	public final FieldValueSerialiser<V> serialiser;

	/**
	 * {@link FieldValueDeserialiser} for retrieved {@link InternalDocument}.
	 */
	public final FieldValueDeserialiser<V> deserialiser;

	/**
	 * Instantiate.
	 * 
	 * @param getter       {@link FieldValueGetter}.
	 * @param translator   {@link FieldValueTranslator}.
	 * @param setter       {@link FieldValueSetter} for store
	 *                     {@link InternalDocument}.
	 * @param serialiser   {@link FieldValueSerialiser}.
	 * @param deserialiser {@link FieldValueDeserialiser}.
	 */
	public FieldType(FieldValueGetter<R, V> getter, FieldValueTranslator<V, P> translator,
			FieldValueSetter<S, P> setter, FieldValueSerialiser<V> serialiser, FieldValueDeserialiser<V> deserialiser) {
		this.getter = getter;
		this.translator = translator;
		this.setter = setter;
		this.serialiser = serialiser;
		this.deserialiser = deserialiser;
	}

}