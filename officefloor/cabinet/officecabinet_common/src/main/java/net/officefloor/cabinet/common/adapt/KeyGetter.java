package net.officefloor.cabinet.common.adapt;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;

/**
 * Obtains the {@link Key} for the retrieved internal {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface KeyGetter<R> {

	/**
	 * Obtains the {@link Key} from the retrieved internal {@link Document}.
	 * 
	 * @param internalDocument Retrieved internal {@link Document}.
	 * @param keyName          Name of {@link Key}.
	 * @return {@link Key} value.
	 */
	String getKey(R internalDocument, String keyName);

}
