package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.metadata.InternalDocument;

/**
 * Meta-data of {@link Field}.
 * 
 * @param V Value type of the {@link Field}.
 * @param R Retrieved {@link InternalDocument} type.
 * @param L {@link Field} value type obtained from the retrieved
 *          {@link InternalDocument} to be loaded onto the {@link Document}.
 * @param S Stored {@link InternalDocument} type.
 * @param P {@link Field} value type obtained from {@link Document} to set on
 *          {@link InternalDocument} to be stored.
 * 
 * @author Daniel Sagenschneider
 */
public class FieldType<V, R, L, S, P> {

	/**
	 * {@link FieldValidator} to validate {@link Field}.
	 */
	public final FieldValidator validator;

	/**
	 * {@link FieldValueGetter} from retrieved {@link InternalDocument}.
	 */
	public final FieldValueGetter<R, L> getter;

	/**
	 * {@link FieldLoader} for loading value onto the {@link Document}.
	 */
	public final FieldLoader<L> loader;

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
	 * @param validator    {@link FieldValidator}.
	 * @param getter       {@link FieldValueGetter}.
	 * @param loader       {@link FieldLoader}.
	 * @param translator   {@link FieldValueTranslator}.
	 * @param setter       {@link FieldValueSetter} for store
	 *                     {@link InternalDocument}.
	 * @param serialiser   {@link FieldValueSerialiser}.
	 * @param deserialiser {@link FieldValueDeserialiser}.
	 */
	public FieldType(FieldValidator validator, FieldValueGetter<R, L> getter, FieldLoader<L> loader,
			FieldValueTranslator<V, P> translator, FieldValueSetter<S, P> setter, FieldValueSerialiser<V> serialiser,
			FieldValueDeserialiser<V> deserialiser) {
		this.validator = validator;
		this.getter = getter;
		this.loader = loader;
		this.translator = translator;
		this.setter = setter;
		this.serialiser = serialiser;
		this.deserialiser = deserialiser;
	}

}