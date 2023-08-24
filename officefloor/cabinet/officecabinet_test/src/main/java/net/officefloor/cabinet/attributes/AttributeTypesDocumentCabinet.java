package net.officefloor.cabinet.attributes;

import java.util.Iterator;
import java.util.Optional;

import net.officefloor.cabinet.spi.OfficeCabinet;

/**
 * Domain specific {@link OfficeCabinet} for the {@link AttributeTypesDocument}.
 * 
 * @author Daniel Sagenschneider
 */
public interface AttributeTypesDocumentCabinet {

	void save(AttributeTypesDocument document);

	Optional<AttributeTypesDocument> findByKey(String key);

	Iterator<AttributeTypesDocument> findByTestName(String testName);

}
