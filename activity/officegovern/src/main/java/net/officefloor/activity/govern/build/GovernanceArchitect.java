package net.officefloor.activity.govern.build;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeGovernance;

import java.util.Map;

public interface GovernanceArchitect {

    /**
     * Adds a specific {@link OfficeGovernance}.
     *
     * @param governanceName     Name of the {@link OfficeGovernance}.
     * @param governanceLocation Location of {@link OfficeGovernance} configuration.
     * @param properties         {@link PropertyList} for configuration.
     * @return {@link OfficeGovernance}.
     * @throws Exception If fails to create {@link OfficeGovernance}.
     */
    OfficeGovernance addGovernance(String governanceName, String governanceLocation,
                                   PropertyList properties) throws Exception;

    /**
     * Adds the {@link OfficeGovernance} instances configured in a directory.
     *
     * @param governanceDirectory Location of the directory containing the {@link OfficeGovernance} configurations.
     * @param properties          {@link PropertyList} for configuration.
     * @return {@link Map} of {@link OfficeGovernance} instances by their name.
     * @throws Exception If fails to create the {@link OfficeGovernance} instances.
     */
    Map<String, OfficeGovernance> addGovernances(String governanceDirectory,
                                                 PropertyList properties) throws Exception;

}
