package net.officefloor.cabinet.common.adapt;

import net.officefloor.cabinet.Document;
import net.officefloor.cabinet.Key;

/**
 * Specifies the {@link Key} on the internal {@link Document}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface KeySetter<S> {

	/**
	 * Specifies the {@link Key} on the internal {@link Document}.
	 * 
	 * @param internalDocument Stored internal {@link Document}.
	 * @param keyName          Name of {@link Key}.
	 * @param keyValue         {@link Key} value.
	 */
	void setKey(S internalDocument, String keyName, String keyValue);

}
