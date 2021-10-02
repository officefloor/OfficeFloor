package net.officefloor.cabinet.cosmosdb;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.common.adapt.Index;

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
		init.addFieldType(byte.class, Byte.class, getter(Integer::byteValue), setter(Byte::intValue));
		init.addFieldType(short.class, Short.class, getter(Integer::shortValue), setter(Short::intValue));
		init.addFieldType(long.class, Long.class, getter((mapValue) -> Long.parseLong((String) mapValue)),
				setter((fieldValue) -> String.valueOf(fieldValue)));
		init.addFieldType(float.class, Float.class, getter(Number::floatValue), setter(Number::doubleValue));
		init.addFieldType(double.class, Double.class, getter(Number::doubleValue), setter(Number::doubleValue));
	}

}