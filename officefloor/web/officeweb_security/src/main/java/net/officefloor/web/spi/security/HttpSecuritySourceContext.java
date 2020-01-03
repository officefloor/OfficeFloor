package net.officefloor.web.spi.security;

import net.officefloor.frame.api.managedobject.ManagedObject;
import net.officefloor.frame.api.managedobject.source.ManagedObjectSource;
import net.officefloor.frame.api.source.SourceContext;
import net.officefloor.frame.api.team.Team;
import net.officefloor.frame.internal.structure.Flow;
import net.officefloor.frame.internal.structure.ManagedObjectScope;

/**
 * Context for the {@link HttpSecuritySource}.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpSecuritySourceContext extends SourceContext {

	/**
	 * <p>
	 * Adds a {@link HttpSecuritySupportingManagedObject}.
	 * <p>
	 * Note that the {@link ManagedObjectSource} can not invoke {@link Flow} or use
	 * {@link Team} instances. Should this be required, use the
	 * {@link HttpSecurityExecuteContext} to invoke {@link Flow} instances.
	 * 
	 * @param managedObjectName   Name of the {@link ManagedObject}.
	 * @param managedObjectSource {@link ManagedObjectSource} for the
	 *                            {@link ManagedObject}.
	 * @param managedObjectScope  {@link ManagedObjectScope} for the resulting
	 *                            {@link ManagedObject}.
	 * @return {@link HttpSecuritySupportingManagedObject} to configure the
	 *         {@link ManagedObject}.
	 */
	<O extends Enum<O>> HttpSecuritySupportingManagedObject<O> addSupportingManagedObject(String managedObjectName,
			ManagedObjectSource<O, ?> managedObjectSource, ManagedObjectScope managedObjectScope);

}