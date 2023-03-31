package net.officefloor.cabinet.cosmosdb;

import java.util.logging.Logger;

import com.azure.cosmos.CosmosDatabase;

import net.officefloor.cabinet.common.AbstractOfficeStore;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Cosmos {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class CosmosOfficeStore extends AbstractOfficeStore<CosmosDocumentMetaData<?>> {

	/**
	 * {@link CosmosDatabase}.
	 */
	private final CosmosDatabase database;

	/**
	 * {@link Logger}.
	 */
	private final Logger logger;

	/**
	 * Instantiate.
	 * 
	 * @param database {@link CosmosDatabase}.
	 * @param logger   {@link Logger}.
	 */
	public CosmosOfficeStore(CosmosDatabase database, Logger logger) {
		this.database = database;
		this.logger = logger;
	}

	/*
	 * ====================== OfficeStore ===========================
	 */

	@Override
	protected <R, S, D> AbstractDocumentAdapter<R, S> createDocumentAdapter(Class<D> documentType) throws Exception {
		return (AbstractDocumentAdapter<R, S>) new CosmosDocumentAdapter(this);
	}

	@Override
	public <R, S, D> CosmosDocumentMetaData<?> createExtraMetaData(
			DocumentMetaData<R, S, D, CosmosDocumentMetaData<?>> metaData, Index[] indexes) throws Exception {
		return new CosmosDocumentMetaData<>(metaData.documentType, indexes, this.database, this.logger);
	}

	@Override
	public AbstractSectionAdapter createSectionAdapter() throws Exception {
		return new CosmosSectionAdapter(this);
	}

	@Override
	public <D, R, S> OfficeCabinet<D> createOfficeCabinet(DocumentMetaData<R, S, D, CosmosDocumentMetaData<?>> metaData,
			CabinetManager cabinetManager) {
		return new CosmosOfficeCabinet<>((DocumentMetaData) metaData, cabinetManager);
	}

}