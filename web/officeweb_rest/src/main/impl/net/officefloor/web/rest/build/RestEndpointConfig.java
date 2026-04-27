package net.officefloor.web.rest.build;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.Map;

@Data
public class RestEndpointConfig {

    /**
     * Named configurations.
     */
    @JsonAnySetter
    private Map<String, JsonNode> items;

}
