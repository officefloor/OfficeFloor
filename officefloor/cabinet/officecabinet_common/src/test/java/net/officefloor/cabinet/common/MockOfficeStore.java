package net.officefloor.cabinet.common;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.Map;

import net.officefloor.cabinet.common.adapt.AbstractDocumentAdapter;
import net.officefloor.cabinet.common.metadata.DocumentMetaData;
import net.officefloor.cabinet.spi.Index;
import net.officefloor.cabinet.spi.OfficeCabinet;
import net.officefloor.cabinet.spi.OfficeStore;

/**
 * Mock {@link OfficeStore}.
 * 
 * @author Daniel Sagenschneider
 */
public class MockOfficeStore extends AbstractOfficeStore<Map<String, Object>> {

	/*
	 * ======================== AbstractOfficeStore ===============================
	 */

	@Override
	protected <R, S, D> AbstractDocumentAdapter<R, S> createDocumentAdapter(Class<D> documentType) {
		return (AbstractDocumentAdapter<R, S>) new MockDocumentAdapter<>(documentType, this);
	}

	@Override
	public <R, S, D> Map<String, Object> createExtraMetaData(DocumentMetaData<R, S, D, Map<String, Object>> metaData,
			Index[] indexes) throws Exception {
		return new HashMap<>(); // document store
	}

	@Override
	public <D, R, S> OfficeCabinet<D> createOfficeCabinet(DocumentMetaData<R, S, D, Map<String, Object>> metaData) {

		// Return the created office cabinet
		try {
			return new MockOfficeCabinet<>((DocumentMetaData) metaData);
		} catch (Exception ex) {
			return fail("Failed to create " + MockOfficeCabinet.class.getName(), ex);
		}
	}

}