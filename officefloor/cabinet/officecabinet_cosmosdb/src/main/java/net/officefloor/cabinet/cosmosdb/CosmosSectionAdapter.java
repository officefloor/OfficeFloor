package net.officefloor.cabinet.cosmosdb;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.spi.Index;

/**
 * Cosmos DB {@link AbstractSectionAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosSectionAdapter extends AbstractSectionAdapter<CosmosSectionAdapter> {

	/**
	 * Creates the {@link CosmosSectionMetaData}.
	 * 
	 * @param <D>          Type of {@link Document}.
	 * @param documentType {@link Document} type.
	 * @param indexes      {@link Index} instances for the {@link Document}.
	 * @param adapter      {@link CosmosSectionAdapter}.
	 * @return {@link CosmosSectionMetaData}.
	 * @throws Exception If fails to create {@link CosmosSectionMetaData}.
	 */
	private <D> CosmosSectionMetaData<D> createSectionMetaData(Class<D> documentType, Index[] indexes,
			CosmosSectionAdapter adapter) throws Exception {
		return new CosmosSectionMetaData<>(adapter, documentType);
	}

	/*
	 * ================== AbstractSectionAdapter =======================
	 */

	@Override
	protected void initialise(Initialise init) throws Exception {

		// Document meta-data
		init.setDocumentMetaDataFactory(this::createSectionMetaData);

		// Primitives overrides (only boolean, int and double supported)
		init.addFieldType(byte.class, Byte.class, getter(Integer::byteValue), translator(Byte::intValue), setter(),
				serialiser(), deserialiser(Byte::parseByte));
		init.addFieldType(short.class, Short.class, getter(Integer::shortValue), translator(Short::intValue), setter(),
				serialiser(), deserialiser(Short::parseShort));
		init.addFieldType(long.class, Long.class, getter((mapValue) -> Long.parseLong((String) mapValue)),
				translator(String::valueOf), setter(), serialiser(), deserialiser(Long::parseLong));
		init.addFieldType(float.class, Float.class, getter(Number::floatValue), translator(Number::doubleValue),
				setter(), serialiser(), deserialiser(Float::parseFloat));
		init.addFieldType(double.class, Double.class, getter(Number::doubleValue), translator(Number::doubleValue),
				setter(), serialiser(), deserialiser(Double::parseDouble));
	}

}