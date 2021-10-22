package net.officefloor.cabinet;

import java.util.Iterator;
import java.util.Optional;

import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Domain specific {@link OfficeCabinet} for the {@link HierarchicalDocument}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HierarchicalDocumentCabinet {

	void save(HierarchicalDocument document);

	Optional<HierarchicalDocument> findByKey(String key);

	Iterator<HierarchicalDocument> findByQueryValue(int queryValue);

}
