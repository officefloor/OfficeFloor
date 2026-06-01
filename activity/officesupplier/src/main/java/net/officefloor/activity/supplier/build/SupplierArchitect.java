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
