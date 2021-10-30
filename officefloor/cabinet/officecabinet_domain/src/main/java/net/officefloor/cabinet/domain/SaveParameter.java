package net.officefloor.cabinet.domain;

/**
 * Saves parameter value.
 * 
 * @author Daniel Sagenschneider
 */
public interface SaveParameter {

	void save(CabinetSession session, Object parameter);
}
