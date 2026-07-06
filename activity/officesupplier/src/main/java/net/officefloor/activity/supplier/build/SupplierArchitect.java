/*-
 * #%L
 * Supplier
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

package net.officefloor.activity.supplier.build;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeSupplier;

import java.util.Map;

/**
 * Architect to configure {@link OfficeSupplier} instances.
 */
public interface SupplierArchitect {

    /**
     * Adds a single {@link OfficeSupplier} from a YAML configuration file.
     *
     * @param supplierName     Name of the supplier.
     * @param supplierLocation Classpath resource path to the YAML configuration.
     * @param properties       {@link PropertyList} for interpolation.
     * @return Configured {@link OfficeSupplier}.
     * @throws Exception If fails to load.
     */
    OfficeSupplier addSupplier(String supplierName, String supplierLocation, PropertyList properties) throws Exception;

    /**
     * Adds all {@link OfficeSupplier} instances from YAML files in a directory.
     *
     * @param supplierDirectory Classpath directory path to scan for YAML files.
     * @param properties        {@link PropertyList} for interpolation.
     * @return Map of supplier name to {@link OfficeSupplier}.
     * @throws Exception If fails to load.
     */
    Map<String, OfficeSupplier> addSuppliers(String supplierDirectory, PropertyList properties) throws Exception;

}
