package net.officefloor.cabinet.common.metadata;

import java.util.Map;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.OfficeCabinet;
import net.officefloor.cabinet.common.adapt.AbstractSectionAdapter;

/**
 * Meta-data for the {@link OfficeCabinet} {@link Document} section.
 * 
 * @author Daniel Sagenschneider
 */
public class AbstractSectionMetaData<A extends AbstractSectionAdapter<A>, D>
		extends AbstractDocumentMetaData<Map<String, Object>, Map<String, Object>, A, D> {

	/**
	 * Instantiate.
	 * 
	 * @param adapter      {@link AbstractSectionAdapter}.
	 * @param documentType {@link Document} type.
	 * @throws Exception If fails to create {@link AbstractSectionMetaData}.
	 */
	public AbstractSectionMetaData(A adapter, Class<D> documentType) throws Exception {
		super(adapter, documentType);
	}

}