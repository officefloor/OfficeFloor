/*-
 * #%L
 * Managed Object
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
