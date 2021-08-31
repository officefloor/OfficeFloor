package net.officefloor.cabinet.common.adapt;

import java.util.Map;

import net.officefloor.cabinet.OfficeCabinet;

/**
 * Adapter of {@link OfficeCabinet} to underlying implementation.
 * 
 * @author Daniel Sagenschneider
 */
public abstract class AbstractSectionAdapter<A extends AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>, A>>
		extends AbstractDocumentAdapter<Map<String, Object>, Map<String, Object>, A> {

	/**
	 * Instantiate.
	 */
	public AbstractSectionAdapter() {
		super(false, null);
	}
}