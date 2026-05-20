package net.officefloor.activity.managedobject.build;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeManagedObject;

import java.util.Map;

/**
 * Architect to build {@link net.officefloor.frame.api.managedobject.ManagedObject} instances.
 */
public interface ManagedObjectArchitect {

    /**
     * Adds a specific {@link OfficeManagedObject}.
     *
     * @param managedObjectName     Name of the {@link OfficeManagedObject}.
     * @param managedObjectLocation Location of the {@link OfficeManagedObject} configuration.
     * @param properties            {@link PropertyList} for configuration.
     * @return {@link OfficeManagedObject}.
     * @throws Exception If fails to create {@link OfficeManagedObject}.
     */
    OfficeManagedObject addManagedObject(String managedObjectName, String managedObjectLocation,
                                         PropertyList properties) throws Exception;

    /**
     * Adds the {@link OfficeManagedObject} instances configured in a directory.
     *
     * @param managedObjectDirectory Location of the directory containing the {@link OfficeManagedObject} configurations.
     * @param properites             {@link PropertyList} for configuration.
     * @return {@link OfficeManagedObject} instances by their name.
     * @throws Exception If fails to create the {@link OfficeManagedObject} instances.
     */
    Map<String, OfficeManagedObject> addManagedObjects(String managedObjectDirectory,
                                                       PropertyList properites) throws Exception;

}
