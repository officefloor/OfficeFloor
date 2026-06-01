package net.officefloor.activity.team;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Configuration for an {@link net.officefloor.compile.spi.officefloor.OfficeFloorTeam}.
 */
@Data
public class TeamConfiguration {

    private String source;

    @JsonProperty("team-size")
    private int teamSize;

    private Map<String, String> properties;

    /**
     * Convenience shorthand for a single unqualified type qualification.
     */
    private String type;

    /**
     * Multiple type qualifications (qualifier + type pairs).
     */
    @JsonProperty("type-qualifications")
    private List<TypeQualificationConfiguration> typeQualifications;

}
