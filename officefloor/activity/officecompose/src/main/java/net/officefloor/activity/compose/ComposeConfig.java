package net.officefloor.activity.compose;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.Map;

@Data
public class ComposeConfig {
    private String service;

    @JsonAnySetter
    private Map<String, FunctionConfig> functions;
}
