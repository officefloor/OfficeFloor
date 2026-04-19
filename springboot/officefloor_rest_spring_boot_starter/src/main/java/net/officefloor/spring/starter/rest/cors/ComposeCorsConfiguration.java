package net.officefloor.spring.starter.rest.cors;

import lombok.Data;

import java.util.List;

@Data
public class ComposeCorsConfiguration {
    private List<String> allowedOrigins;
    private List<String> allowedOriginPatters;
    private List<String> allowedMethods;
    private List<String> allowedHeaders;
    private List<String> exposedHeaders;
    private Boolean allowCredentials;
    private Boolean allowPrivateNetwork;
    private Long maxAge;
}
