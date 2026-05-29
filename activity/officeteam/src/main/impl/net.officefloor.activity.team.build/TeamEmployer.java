package net.officefloor.activity.team.build;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import net.officefloor.activity.team.TeamConfiguration;
import net.officefloor.activity.team.TypeQualificationConfiguration;
import net.officefloor.compile.properties.PropertyList;
import net.officefloor.compile.spi.office.OfficeArchitect;
import net.officefloor.compile.spi.office.OfficeTeam;
import net.officefloor.compile.spi.office.source.OfficeSourceContext;
import net.officefloor.compile.spi.officefloor.DeployedOffice;
import net.officefloor.compile.spi.officefloor.OfficeFloorDeployer;
import net.officefloor.compile.spi.officefloor.OfficeFloorTeam;
import net.officefloor.compile.spi.officefloor.source.OfficeFloorSourceContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamEmployer {

    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    /**
     * Employs the {@link TeamDeployer}.
     *
     * @param deployer {@link OfficeFloorDeployer}.
     * @param context  {@link OfficeFloorSourceContext}.
     * @param office   {@link DeployedOffice}. May be <code>null</code>.
     * @return {@link TeamDeployer}.
     */
    public static TeamDeployer employTeamDeployer(OfficeFloorDeployer deployer,
                                                  OfficeFloorSourceContext context,
                                                  DeployedOffice office) {
        return new TeamDeployer() {

            @Override
            public OfficeFloorTeam addTeam(String teamName, String teamLocation,
                                           PropertyList properties) throws Exception {
                TeamConfiguration config = MAPPER.readValue(
                        context.getConfigurationItem(teamLocation, properties).getReader(),
                        TeamConfiguration.class);
                deployer.enableAutoWireTeams();
                return createTeam(teamName, config, deployer);
            }

            @Override
            public Map<String, OfficeFloorTeam> addTeams(String teamsDirectory,
                                                         PropertyList properties) throws Exception {
                Map<String, OfficeFloorTeam> teams = new HashMap<>();

                String dir = teamsDirectory;
                while (dir.endsWith("/")) {
                    dir = dir.substring(0, dir.length() - 1);
                }
                dir = dir + "/";

                try (ScanResult result = new ClassGraph().acceptPaths(dir).scan()) {
                    for (String yamlExtension : new String[]{"yml", "yaml"}) {
                        for (Resource resource : result.getResourcesWithExtension(yamlExtension)) {
                            String path = resource.getPath();
                            String teamName = path.substring(dir.length(),
                                    path.length() - ".".length() - yamlExtension.length());
                            TeamConfiguration config = MAPPER.readValue(
                                    context.getConfigurationItem(path, properties).getReader(),
                                    TeamConfiguration.class);
                            teams.put(teamName, createTeam(teamName, config, deployer));
                        }
                    }
                }

                if (!teams.isEmpty()) {
                    deployer.enableAutoWireTeams();
                }

                return teams;
            }
        };
    }

    private static OfficeFloorTeam createTeam(String teamName, TeamConfiguration config,
                                              OfficeFloorDeployer deployer) {
        OfficeFloorTeam team = deployer.addTeam(teamName, config.getSource());

        if (config.getTeamSize() > 0) {
            team.setTeamSize(config.getTeamSize());
        }

        // Convenience single-type shorthand
        String type = config.getType();
        if (type != null && !type.isBlank()) {
            team.addTypeQualification(null, type);
        }

        // Multiple type qualifications
        List<TypeQualificationConfiguration> qualifications = config.getTypeQualifications();
        if (qualifications != null) {
            for (TypeQualificationConfiguration tq : qualifications) {
                team.addTypeQualification(tq.getQualifier(), tq.getType());
            }
        }

        Map<String, String> props = config.getProperties();
        if (props != null) {
            props.forEach(team::addProperty);
        }

        return team;
    }

}
