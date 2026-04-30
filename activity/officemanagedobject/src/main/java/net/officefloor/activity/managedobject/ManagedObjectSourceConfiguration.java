package net.officefloor.activity.managedobject;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

/**
 * Configuration for the {@link net.officefloor.compile.spi.office.OfficeManagedObject}.
 */
@Data
public class ManagedObjectSourceConfiguration {

    /*
     * ========== Class Managed Object ===========
     */

    @JsonProperty("class")
    private String className;

    /*
     * ========== Managed Object Source ==========
     */

    private String source;

    private Map<String, String> properties;

    /*
     * ========== Composition ===========
     */

    private Map<String, String> outputs;

}
