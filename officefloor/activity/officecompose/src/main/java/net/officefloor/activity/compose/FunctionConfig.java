package net.officefloor.activity.compose;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FunctionConfig {

    @JsonProperty("class")
    private String className;

    private String method;
}
