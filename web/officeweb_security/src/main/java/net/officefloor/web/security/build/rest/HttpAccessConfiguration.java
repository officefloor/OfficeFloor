package net.officefloor.web.security.build.rest;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class HttpAccessConfiguration {

    @JsonProperty("any-role")
    List<String> anyRole;

    @JsonProperty("all-roles")
    List<String> allRoles;

    @JsonProperty("inherit-all-roles")
    boolean isInheritAllRoles;
}
