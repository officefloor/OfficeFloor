package net.officefloor.compile.spi.supplier.source;

import net.officefloor.compile.state.autowire.AutoWireStateManager;
import net.officefloor.frame.api.manage.ObjectUser;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.frame.api.manage.UnknownObjectException;

/**
 * <p>
 * Exposes internal objects from the {@link SupplierSource}.
 * <p>
 * Objects provided are not available to the {@link OfficeFloor} application for
 * use (as they are considered private to the {@link SupplierSource}).
 * <p>
 * This, however, is available to enable the {@link AutoWireStateManager} to
 * provide the object. Typically, this is to provide access for tests to
 * manipulate internal objects.
 * 
 * @author Daniel Sagenschneider
 */
public interface InternalSupplier {

	/**
	 * Indicates if the object by auto-wiring is available.
	 * 
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Required object type.
	 * @return <code>true</code> if the object is available.
	 */
	boolean isObjectAvailable(String qualifier, Class<?> objectType);

	/**
	 * Loads the object asynchronously.
	 * 
	 * @param qualifier  Qualifier. May be <code>null</code>.
	 * @param objectType Required object type.
	 * @param user       {@link ObjectUser} to receive the loaded object (or
	 *                   possible failure).
	 * @throws UnknownObjectException If unknown bound object name.
	 */
	<O> void load(String qualifier, Class<? extends O> objectType, ObjectUser<O> user) throws UnknownObjectException;

}