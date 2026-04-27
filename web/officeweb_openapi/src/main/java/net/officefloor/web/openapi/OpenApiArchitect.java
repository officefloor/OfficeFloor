package net.officefloor.web.openapi;

import io.swagger.v3.oas.models.OpenAPI;

import java.util.Set;

/**
 * Architect for creating Open API.
 */
public interface OpenApiArchitect {

    /**
     * Builds the {@link OpenAPI}.
     *
     * @param openAPI     {@link OpenAPI}.
     * @param ignorePaths Paths to ignore.
     */
    void buildOpenApi(OpenAPI openAPI, Set<String> ignorePaths);

}