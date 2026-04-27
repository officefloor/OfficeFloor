package net.officefloor.activity.compose;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.Map;

@Data
public class CompositionConfiguration {

    private Map<String, String> escalations;

    /**
     * Allow for other meta-data of more specific composition.
     */
    @JsonAnySetter
    private Map<String, JsonNode> allowOtherMetaData;
}
