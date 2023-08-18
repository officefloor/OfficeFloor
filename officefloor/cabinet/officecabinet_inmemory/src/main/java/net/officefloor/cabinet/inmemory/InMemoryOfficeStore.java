package net.officefloor.cabinet.inmemory;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.cabinet.common.AbstractOfficeCabinet;
import net.officefloor.cabinet.common.AbstractOfficeStore;
import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.spi.CabinetManager;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Mock {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class InMemoryOfficeStore extends AbstractOfficeStore<Map<String, Object>, InMemoryTransaction> {

	/*
	 * ======================== AbstractOfficeStore ===============================
	 */

	@Override
	protected <R, S, D> AbstractDocumentAdapter<R, S> createDocumentAdapter(Class<D> documentType) {
		return (AbstractDocumentAdapter<R, S>) new InMemoryDocumentAdapter(this);
	}

	@Override
	public <R, S, D> Map<String, Object> createExtraMetaData(
			DocumentMetaData<R, S, D, Map<String, Object>, InMemoryTransaction> metaData, Index[] indexes)
			throws Exception {
		return new HashMap<>(); // document store
	}

	@Override
	public <D, R, S> AbstractOfficeCabinet<R, S, D, Map<String, Object>, InMemoryTransaction> createOfficeCabinet(
			DocumentMetaData<R, S, D, Map<String, Object>, InMemoryTransaction> metaData, CabinetManager cabinetManager) {

		// Return the created office cabinet
		try {
			return new InMemoryOfficeCabinet<>((DocumentMetaData) metaData, cabinetManager);
		} catch (Exception ex) {
			throw new IllegalStateException("Failed to create " + InMemoryOfficeCabinet.class.getName(), ex);
		}
	}

	@Override
	public void transact(TransactionalChange<InMemoryTransaction> change) throws Exception {

		// Create transaction
		InMemoryTransaction transaction = new InMemoryTransaction();

		// Undertake change within transaction
		change.transact(transaction);

		// Commit transaction
		transaction.commit();
	}

}