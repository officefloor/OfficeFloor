package net.officefloor.cabinet.domain;

import net.officefloor.cabinet.spi.CabinetManager;

/**
 * Saves parameter value.
 * 
 * @author Daniel Sagenschneider
 */
public interface SaveParameter {

	void save(CabinetManager cabinetManager, Object parameter);
}
