package net.officefloor.web.rest.config;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.Map;

@Data
public class EndpointConfig {
    private String service;

    @JsonAnySetter
    private Map<String, FunctionConfig> functions;
}
