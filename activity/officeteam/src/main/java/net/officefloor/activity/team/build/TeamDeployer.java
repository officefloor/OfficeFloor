package net.officefloor.activity.team.build;

import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;

import java.util.Map;

/**
 * Deployer to configure {@link OfficeFloorTeam} instances.
 */
public interface TeamDeployer {

    /**
     * Adds a single {@link OfficeFloorTeam}.
     *
     * @param teamName     Name of the team.
     * @param teamLocation Classpath resource path to the configuration.
     * @param properties   {@link PropertyList} for interpolation.
     * @return Configured {@link OfficeFloorTeam}.
     * @throws Exception If fails to load.
     */
    OfficeFloorTeam addTeam(String teamName, String teamLocation, PropertyList properties) throws Exception;

    /**
     * Adds all {@link OfficeFloorTeam} instances from a directory.
     *
     * @param teamsDirectory Classpath directory path to scan for configuration files.
     * @param properties     {@link PropertyList} for interpolation.
     * @return Map of team name to {@link OfficeFloorTeam}.
     * @throws Exception If fails to load.
     */
    Map<String, OfficeFloorTeam> addTeams(String teamsDirectory, PropertyList properties) throws Exception;

}