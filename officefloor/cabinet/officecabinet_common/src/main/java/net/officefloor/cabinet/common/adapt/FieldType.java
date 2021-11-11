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
	 * Instantiate.
	 * 
	 * @param getter     {@link FieldValueGetter}.
	 * @param translator {@link FieldValueTranslator}.
	 * @param setter     {@link FieldValueSetter} for store
	 *                   {@link InternalDocument}.
	 */
	public FieldType(FieldValueGetter<R, V> getter, FieldValueTranslator<V, P> translator,
			FieldValueSetter<S, P> setter) {
		this.getter = getter;
		this.translator = translator;
		this.setter = setter;
	}

}