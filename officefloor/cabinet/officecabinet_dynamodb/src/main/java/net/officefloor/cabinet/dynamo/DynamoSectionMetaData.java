package net.officefloor.cabinet.dynamo;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.metadata.SectionMetaData;

/**
 * Dynamo DB {@link SectionMetaData}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoSectionMetaData<D> extends SectionMetaData<DynamoSectionAdapter, D> {

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
