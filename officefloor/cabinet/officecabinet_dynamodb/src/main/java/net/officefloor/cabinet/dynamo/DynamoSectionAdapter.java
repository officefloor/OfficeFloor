package net.officefloor.cabinet.dynamo;

import java.math.BigDecimal;
import java.util.Map;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.common.adapt.ScalarFieldValueGetter;
import net.officefloor.cabinet.spi.Index;

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

	/**
	 * Creates the {@link DynamoSectionMetaData}.
	 * 
	 * @param <D>          Type of {@link Document}.
	 * @param documentType {@link Document} type.
	 * @param indexes      {@link Index} instances for the {@link Document}.
	 * @param adapter      {@link DynamoSectionAdapter}.
	 * @return {@link DynamoSectionMetaData}.
	 * @throws Exception If fails to create {@link DynamoSectionMetaData}.
	 */
	private <D> DynamoSectionMetaData<D> createSectionMetaData(Class<D> documentType, Index[] indexes,
			DynamoSectionAdapter adapter) throws Exception {
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
		init.addFieldType(byte.class, Byte.class, bigGetter(BigDecimal::byteValue), translator(Byte::longValue),
				setter());
		init.addFieldType(short.class, Short.class, bigGetter(BigDecimal::shortValue), translator(Short::longValue),
				setter());
		init.addFieldType(int.class, Integer.class, bigGetter(BigDecimal::intValue), translator(Integer::longValue),
				setter());
		init.addFieldType(long.class, Long.class, bigGetter(BigDecimal::longValue), translator(Long::longValue),
				setter());
		init.addFieldType(float.class, Float.class, bigGetter(BigDecimal::floatValue), translator(Float::doubleValue),
				setter());
		init.addFieldType(double.class, Double.class, bigGetter(BigDecimal::doubleValue),
				translator(Double::doubleValue), setter());
	}

}