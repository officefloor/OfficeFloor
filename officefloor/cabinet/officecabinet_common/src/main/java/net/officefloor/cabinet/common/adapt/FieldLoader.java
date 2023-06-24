package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.spi.CabinetManager;

/**
 * Loads the value onto the {@link Field} of the {@link Document}.
 * 
 * @author Daniel Sagenschneider
 *
 */
@FunctionalInterface
public interface FieldLoader<V> {

	/**
	 * Loads the value onto the {@link Field} of the {@link Document}.
	 * 
	 * @param document       {@link Document}.
	 * @param field          {@link Field} to have value loaded.
	 * @param fieldValue     Value to load.
	 * @param cabinetManager {@link CabinetManager}.
	 * @throws Exception If fails to load value to {@link Field}.
	 */
	void load(Object document, Field field, V fieldValue, CabinetManager cabinetManager) throws Exception;

}
