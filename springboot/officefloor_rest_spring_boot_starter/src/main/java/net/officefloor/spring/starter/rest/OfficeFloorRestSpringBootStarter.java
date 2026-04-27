package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import jakarta.annotation.PreDestroy;
import net.officefloor.compile.OfficeFloorCompiler;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.server.http.servlet.HttpServletHttpServerImplementation;
import net.officefloor.server.http.servlet.HttpServletOfficeFloorBridge;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Handles starting {@link OfficeFloor}.
 */
public class OfficeFloorRestSpringBootStarter {

    private final OfficeFloorRestProperties properties;

    private final ConfigurableApplicationContext applicationContext;

    private final ObjectMapper mapper;

    private final List<OfficeFloorRestEndpoint> restEndpoints = new LinkedList<>();

    private final OpenAPI openApi = new OpenAPI();

    private HttpServletOfficeFloorBridge bridge;

    private OfficeFloor officeFloor;

    public OfficeFloorRestSpringBootStarter(OfficeFloorRestProperties properties,
                                            ConfigurableApplicationContext applicationContext,
                                            ObjectMapper mapper) {
        this.properties = properties;
        this.applicationContext = applicationContext;
        this.mapper = mapper;

        // Set up OpenAPI
        this.openApi.setPaths(new Paths());
    }

    @PreDestroy
    public void destroy() throws Exception {
        if (this.officeFloor != null) {

            // Close and clean up
            this.officeFloor.closeOfficeFloor();
            this.officeFloor = null;
            this.bridge = null;
        }
    }

    /**
     * Ensures {@link OfficeFloor} is started.
     */
    public void startOfficeFloor() throws Exception {

        // Determine if already started
        if (this.officeFloor != null) {
            return; // already started
        }

        // Load OfficeFloor (capturing the REST endpoints)
        this.bridge = HttpServletHttpServerImplementation.load(() -> {

            // Compile the OfficeFloor
            OfficeFloorCompiler compiler = OfficeFloorCompiler.newOfficeFloorCompiler(null);
            compiler.setOfficeFloorSource(new SpringBootOfficeFloorSource(this.mapper, this.restEndpoints,
                    this.applicationContext, this.openApi));
            Map<String, String> sourceProperties = this.properties.getConfig();
            if (sourceProperties != null) {
                sourceProperties.forEach(compiler::addProperty);
            }
            this.officeFloor = compiler.compile("OfficeFloor");
            this.officeFloor.openOfficeFloor();
        });
    }

    /**
     * Obtains the {@link HttpServletOfficeFloorBridge}.
     *
     * @return {@link HttpServletOfficeFloorBridge}.
     */
    public HttpServletOfficeFloorBridge getBridge() {
        return this.bridge;
    }

    /**
     * Obtains the {@link OfficeFloorRestEndpoint} instances.
     *
     * @return {@link OfficeFloorRestEndpoint} instances.
     */
    public List<OfficeFloorRestEndpoint> getRestEndpoints() {
        return this.restEndpoints;
    }

    /**
     * Obtains the {@link OpenAPI}.
     *
     * @return {@link OpenAPI}.
     */
    public OpenAPI getOpenApi() {
        return this.openApi;
    }

}
