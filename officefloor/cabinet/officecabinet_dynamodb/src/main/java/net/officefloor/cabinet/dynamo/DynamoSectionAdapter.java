package net.officefloor.cabinet.dynamo;

import java.math.BigDecimal;
import java.util.Map;

import net.officefloor.cabinet.common.AbstractOfficeStore;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.common.adapt.ScalarFieldValueGetter;

/**
 * Dynamo DB {@link AbstractSectionAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoSectionAdapter extends AbstractSectionAdapter {

	private static <V> ScalarFieldValueGetter<Map<String, Object>, V> bigGetter(
			FieldValueTransform<BigDecimal, V> transform) {
		return getter(value -> transform.transform((BigDecimal) value));
	}

	public DynamoSectionAdapter(AbstractOfficeStore<DynamoDocumentMetaData<?>> officeStore) {
		super(officeStore);
	}

	/*
	 * =================== AbstractSectionAdapter ====================
	 */

	@Override
	protected void initialise(AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>>.Initialise init)
			throws Exception {

		// Override primitive types
		init.addFieldType(byte.class, Byte.class, bigGetter(BigDecimal::byteValue), translator(Byte::longValue),
				setter(), serialiser(), deserialiser(Byte::valueOf));
		init.addFieldType(short.class, Short.class, bigGetter(BigDecimal::shortValue), translator(Short::longValue),
				setter(), serialiser(), deserialiser(Short::valueOf));
		init.addFieldType(int.class, Integer.class, bigGetter(BigDecimal::intValue), translator(Integer::longValue),
				setter(), serialiser(), deserialiser(Integer::valueOf));
		init.addFieldType(long.class, Long.class, bigGetter(BigDecimal::longValue), translator(Long::longValue),
				setter(), serialiser(), deserialiser(Long::valueOf));
		init.addFieldType(float.class, Float.class, bigGetter(BigDecimal::floatValue), translator(Float::doubleValue),
				setter(), serialiser(), deserialiser(Float::valueOf));
		init.addFieldType(double.class, Double.class, bigGetter(BigDecimal::doubleValue),
				translator(Double::doubleValue), setter(), serialiser(), deserialiser(Double::valueOf));
	}

}