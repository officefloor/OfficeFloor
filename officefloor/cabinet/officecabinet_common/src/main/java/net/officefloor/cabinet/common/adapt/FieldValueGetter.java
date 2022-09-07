package net.officefloor.cabinet.common.adapt;

import java.lang.reflect.Field;

import net.officefloor.cabinet.common.manage.ManagedDocumentState;
import net.officefloor.cabinet.common.metadata.InternalDocument;

/**
 * Obtains the {@link Field} value from retrieved {@link InternalDocument}.
 * 
 * @author Daniel Sagenschneider
 */
@FunctionalInterface
public interface FieldValueGetter<R, V> {

	/**
	 * Obtains the {@link Field} value from retrieved {@link InternalDocument}.
	 * 
	 * @param internalDocument Retrieved  {@link InternalDocument}.
	 * @param fieldName        Name of {@link Field}.
	 * @param state            {@link ManagedDocumentState}.
	 * @return Value for {@link Field}.
	 */
	V getValue(R internalDocument, String fieldName, ManagedDocumentState state);

}
