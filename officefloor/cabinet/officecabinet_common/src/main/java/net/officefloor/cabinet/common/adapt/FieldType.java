package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;

/**
 * Meta-data of {@link Field}.
 * 
 * @author Daniel Sagenschneider
 */
public class FieldType<R, S, V> {

	/**
	 * {@link FieldValueGetter}.
	 */
	public final FieldValueGetter<R, V> getter;

	/**
	 * {@link FieldValueSetter}.
	 */
	public final FieldValueSetter<S, V> setter;

	/**
	 * Instantiate.
	 * 
	 * @param getter {@link FieldValueGetter}.
	 * @param setter {@link FieldValueSetter}.
	 */
	public FieldType(FieldValueGetter<R, V> getter, FieldValueSetter<S, V> setter) {
		this.getter = getter;
		this.setter = setter;
	}

}
