package net.officefloor.activity.govern;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Configuration for the {@link net.officefloor.compile.spi.office.OfficeGovernance}.
 */
@Data
public class GovernanceConfiguration {

    /*
     * ========== Class Governance ===========
     */

    @JsonProperty("class")
    private String className;

    /*
     * ========== Governance Source ==========
     */

    private String source;

    private Map<String, String> config;

    /*
     * ========== Composition ===========
     */

    private Map<String, String> outputs;

    private Map<String, String> escalations;
}
