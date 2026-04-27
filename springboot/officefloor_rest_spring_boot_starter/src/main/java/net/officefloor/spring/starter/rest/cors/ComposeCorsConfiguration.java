package net.officefloor.spring.starter.rest.cors;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ComposeCorsConfiguration {

    @JsonProperty("allowed-origins")
    private List<String> allowedOrigins;

    @JsonProperty("allowed-origin-patterns")
    private List<String> allowedOriginPatterns;

    @JsonProperty("allowed-methods")
    private List<String> allowedMethods;

    @JsonProperty("allowed-headers")
    private List<String> allowedHeaders;

    @JsonProperty("exposed-headers")
    private List<String> exposedHeaders;

    @JsonProperty("allow-credentials")
    private Boolean allowCredentials;

    @JsonProperty("allow-private-network")
    private Boolean allowPrivateNetwork;

    @JsonProperty("max-age")
    private Long maxAge;

}
