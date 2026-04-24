package net.officefloor.spring.starter.rest.openapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that both Spring-routed and OfficeFloor-routed endpoints are visible
 * in the OpenAPI specification, confirming seamless API surface integration.
 *
 * Spring endpoints are discovered automatically by SpringDoc via @RestController scanning.
 * OfficeFloor endpoints require OfficeFloor's SpringDoc integration to register them;
 * the officeFloorEndpointIncluded test will pass once that integration is implemented.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class OpenApiTest {

    @Autowired
    private MockMvc mvc;

    // ── Spring endpoint — auto-discovered by SpringDoc's @RestController scan ─

    @Test
    public void springEndpointIncluded() throws Exception {
        this.mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/spring/async/callable")));
    }

    // ── OfficeFloor endpoint — requires OfficeFloor's SpringDoc integration ───

    @Test
    public void officeFloorEndpointIncluded() throws Exception {
        this.mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/officefloor/async/callable")));
    }
}
