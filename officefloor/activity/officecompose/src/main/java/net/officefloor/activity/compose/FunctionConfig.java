package net.officefloor.activity.compose;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class FunctionConfig {

    @JsonProperty("class")
    private String className;

    private String method;

    private String next;

    private Map<String, String> outputs;
}
