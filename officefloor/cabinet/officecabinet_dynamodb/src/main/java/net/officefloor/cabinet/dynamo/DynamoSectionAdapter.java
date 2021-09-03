package net.officefloor.cabinet.dynamo;

import java.math.BigDecimal;
import java.util.Map;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.common.adapt.FieldValueSetter;
import net.officefloor.cabinet.common.adapt.ScalarFieldValueGetter;

/**
 * Dynamo DB {@link AbstractSectionAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoSectionAdapter extends AbstractSectionAdapter<DynamoSectionAdapter> {

	private static <V> ScalarFieldValueGetter<Map<String, Object>, V> bigGetter(
			FieldValueTransform<BigDecimal, V> transform) {
		return getter(value -> transform.transform((BigDecimal) value));
	}

	private static <V> FieldValueSetter<Map<String, Object>, V> bigSetterLong(FieldValueTransform<V, Long> transform) {
		return setter(value -> BigDecimal.valueOf(transform.transform(value)));
	}

	private static <V> FieldValueSetter<Map<String, Object>, V> bigSetterDouble(
			FieldValueTransform<V, Double> transform) {
		return setter(value -> BigDecimal.valueOf(transform.transform(value)));
	}

	/**
	 * Creates the {@link DynamoSectionMetaData}.
	 * 
	 * @param <D>          Type of {@link Document}.
	 * @param documentType {@link Document} type.
	 * @param adapter      {@link DynamoSectionAdapter}.
	 * @return {@link DynamoSectionMetaData}.
	 * @throws Exception If fails to create {@link DynamoSectionMetaData}.
	 */
	private <D> DynamoSectionMetaData<D> createSectionMetaData(Class<D> documentType, DynamoSectionAdapter adapter)
			throws Exception {
		return new DynamoSectionMetaData<>(adapter, documentType);
	}

	/*
	 * =================== AbstractSectionAdapter ====================
	 */

	@Override
	protected void initialise(
			AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>, DynamoSectionAdapter>.Initialise init)
			throws Exception {

		// Document meta-data
		init.setDocumentMetaDataFactory(this::createSectionMetaData);

		// Override primitive types
		init.addFieldType(byte.class, Byte.class, bigGetter(BigDecimal::byteValue), bigSetterLong(Byte::longValue));
		init.addFieldType(short.class, Short.class, bigGetter(BigDecimal::shortValue), bigSetterLong(Short::longValue));
		init.addFieldType(int.class, Integer.class, bigGetter(BigDecimal::intValue), bigSetterLong(Integer::longValue));
		init.addFieldType(long.class, Long.class, bigGetter(BigDecimal::longValue), bigSetterLong(Long::longValue));
		init.addFieldType(float.class, Float.class, bigGetter(BigDecimal::floatValue),
				bigSetterDouble(Float::doubleValue));
		init.addFieldType(double.class, Double.class, bigGetter(BigDecimal::doubleValue),
				bigSetterDouble(Double::doubleValue));
	}

}