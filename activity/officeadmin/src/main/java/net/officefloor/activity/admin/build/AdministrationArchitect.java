package net.officefloor.activity.admin.build;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeAdministration;
import net.officefloor.compile.spi.office.OfficeGovernance;

import java.util.Map;

public interface AdministrationArchitect {

    /**
     * Adds a specific {@link OfficeAdministration}.
     *
     * @param administrationName     Name of the {@link OfficeAdministration}.
     * @param administrationLocation Location of {@link OfficeAdministration} configuration.
     * @param properties             {@link PropertyList} for configuration.
     * @return {@link OfficeAdministration}.
     * @throws Exception If fails to create {@link OfficeAdministration}.
     */
    OfficeAdministration addAdministration(String administrationName, String administrationLocation,
                                           PropertyList properties) throws Exception;

    /**
     * Adds the {@link OfficeAdministration} instances configured in a directory.
     *
     * @param administrationDirectory Location of the directory containing the {@link OfficeAdministration} configurations.
     * @param properties              {@link PropertyList} for configuration.
     * @return {@link Map} of {@link OfficeAdministration} instances by their name.
     * @throws Exception If fails to create the {@link OfficeAdministration} instances.
     */
    Map<String, OfficeAdministration> addAdministrations(String administrationDirectory,
                                                         PropertyList properties) throws Exception;

}
