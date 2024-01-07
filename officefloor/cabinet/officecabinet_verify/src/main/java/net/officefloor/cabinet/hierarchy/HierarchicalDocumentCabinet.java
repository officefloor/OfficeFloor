package net.officefloor.cabinet.hierarchy;

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

	Iterator<HierarchicalDocument> findByTestName(String testName);

}
