package net.officefloor.cabinet.dynamo;

import java.util.Map;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;

/**
 * Dynamo DB {@link AbstractSectionAdapter}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoSectionAdapter extends AbstractSectionAdapter<DynamoSectionAdapter> {

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
		init.addFieldType(byte.class, Byte.class, getter(Integer::byteValue), setter(Byte::intValue));
	}

}