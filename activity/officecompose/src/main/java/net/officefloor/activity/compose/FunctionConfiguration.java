package net.officefloor.activity.compose;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FunctionConfiguration {

    /*
     * ======== Procedure =============
     */

    @JsonProperty("class")
    private String className;

    private String method;

    private String next;

    /*
     * ======== Custom ProcedureSource ==========
     */

    private String resource;

    private String procedure;

    /*
     * ======== SectionSource ==========
     */

    private String source;

    private String location;

    private String input;

    private Map<String, String> properties;

    /*
     * ========= Govern ===========
     */

    private List<String> govern;

    /*
     * ========= Composition ===========
     */

    private Map<String, String> outputs;

    private Map<String, String> escalations;
}
