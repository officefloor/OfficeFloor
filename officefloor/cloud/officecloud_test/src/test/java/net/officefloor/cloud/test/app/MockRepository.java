package net.officefloor.cloud.test.app;

import net.officefloor.cabinet.Cabinet;

/**
 * Mock repository.
 * 
 * @author Daniel Sagenschneider
 */
@Cabinet
public interface MockRepository {

	/**
	 * Obtains the {@link MockDocument}.
	 * 
	 * @param key Key.
	 * @return {@link MockDocument}.
	 */
	MockDocument getMockDocumentByKey(String key);

	/**
	 * Store {@link MockDocument}.
	 * 
	 * @param document {@link MockDocument}.
	 */
	void store(MockDocument document);

}
