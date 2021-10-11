package net.officefloor.cabinet.dynamo;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.metadata.AbstractSectionMetaData;

/**
 * Dynamo DB {@link AbstractSectionMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoSectionMetaData<D> extends AbstractSectionMetaData<DynamoSectionAdapter, D> {

	/**
	 * Instantiate.
	 * 
	 * @param adapter      {@link DynamoSectionAdapter}.
	 * @param documentType {@link Document} type.
	 * @throws Exception If fails to instantiate.
	 */
	public DynamoSectionMetaData(DynamoSectionAdapter adapter, Class<D> documentType) throws Exception {
		super(adapter, documentType);
	}

}
