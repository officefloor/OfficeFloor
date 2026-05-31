package net.officefloor.web.security.build.rest;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.Map;

/** HTTP security configuration. */
@Data
public class HttpSecurityConfiguration {

    /**
     * Provides the access configuration for a particular {@link net.officefloor.web.spi.security.HttpSecurity}.
     */
    @JsonAnySetter
    private Map<String, HttpAccessConfiguration> accesses;

}
