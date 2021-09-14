package net.officefloor.cabinet.dynamo;

import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeCabinetArchive;

/**
 * Dynamo DB {@link OfficeCabinetArchive}.
 * 
 * @author Daniel Sagenschneider
 */
public class DynamoOfficeCabinetArchive<D> implements OfficeCabinetArchive<D> {

	/**
	 * {@link DynamoOfficeCabinetArchive}.
	 */
	private final DynamoDocumentMetaData<D> metaData;

	/**
	 * Instantiate.
	 * 
	 * @param adapter      {@link DynamoDocumentAdapter}.
	 * @param documentType Document type.
	 * @throws Exception If fails to load {@link OfficeCabinet}.
	 */
	public DynamoOfficeCabinetArchive(DynamoDocumentAdapter adapter, Class<D> documentType) throws Exception {
		this.metaData = (DynamoDocumentMetaData<D>) adapter.createDocumentMetaData(documentType);
	}

	/*
	 * ================= OfficeCabinetArchive ======================
	 */

	@Override
	public OfficeCabinet<D> createOfficeCabinet() {
		return new DynamoOfficeCabinet<>(this.metaData);
	}

	@Override
	public void close() throws Exception {
		// TODO consider closing connection
	}

}