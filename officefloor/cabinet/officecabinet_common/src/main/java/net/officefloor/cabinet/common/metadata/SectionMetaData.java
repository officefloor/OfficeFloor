package net.officefloor.cabinet.common.metadata;

import java.util.Map;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;
import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Meta-data for the {@link OfficeCabinet} {@link Document} section.
 * 
 * @author Daniel Sagenschneider
 */
public class SectionMetaData<D> extends DocumentMetaData<Map<String, Object>, Map<String, Object>, D, Void> {

	/**
	 * Instantiate.
	 * 
	 * @param adapter      {@link AbstractSectionAdapter}.
	 * @param documentType {@link Document} type.
	 * @throws Exception If fails to create {@link SectionMetaData}.
	 */
	public SectionMetaData(AbstractSectionAdapter adapter, Class<D> documentType) throws Exception {
		super(adapter, documentType, null, null, null);
	}

}