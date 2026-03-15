package net.officefloor.activity.compose;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.Map;

@Data
public class CompositionConfig {

    private Map<String, String> escalations;

    /**
     * Allow for other meta-data of more specific composition.
     */
    @JsonAnySetter
    private Map<String, Object> allowOtherMetaData;
}
