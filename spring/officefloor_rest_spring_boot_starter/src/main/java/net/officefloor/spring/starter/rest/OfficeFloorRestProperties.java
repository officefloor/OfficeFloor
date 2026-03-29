package net.officefloor.spring.starter.rest;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties("officefloor.rest")
public class OfficeFloorRestProperties {

    /**
     * Configuration properties in loading REST files.
     */
    private Map<String, String> config;

}
