package net.officefloor.activity.admin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Configuration for the {@link net.officefloor.compile.spi.office.OfficeAdministration}.
 */
@Data
public class AdministrationConfiguration {

    /*
     * ========== Class Administration ===========
     */

    @JsonProperty("class")
    private String className;

    /*
     * ========== Administration Source ==========
     */

    private String source;

    private Map<String, String> properties;

    /*
     * ========== Composition ===========
     */

    private Map<String, String> outputs;

}
