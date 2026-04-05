package net.officefloor.activity.compose;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class FunctionConfiguration {

    /*
     * ======== Procedure =============
     */

    @JsonProperty("class")
    private String className;

    private String method;

    /*
     * ======== SectionSource ==========
     */

    private String source;

    private String location;

    /*
     * ========= Composition ===========
     */

    private String next;

    private Map<String, String> outputs;

    private Map<String, String> escalations;
}
