package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import net.officefloor.web.ObjectResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OfficeFloorSpringBootTest {

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("officefloor.rest.config.testcase", () -> OfficeFloorSpringBootTest.class.getName());
    }

    protected @Autowired MockMvc mvc;

    protected @Autowired ObjectMapper mapper;

    @Test
    @WithMockUser(username= "User", roles = "USER")
    public void GET_officefloor() throws Exception {
        this.mvc.perform(get("/officefloor").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(new Response("GET"))));
    }

    public static class ServiceGet {
        public void service(ObjectResponse<Response> response) {
            response.send(new Response("GET"));
        }
    }

    @Data
    public static class Response {
        private final String officeFloor;
    }

    @Test
    @WithMockUser(username= "User", roles = "USER")
    public void POST_officefloor() throws Exception {
        this.mvc.perform(post("/officefloor").accept(MediaType.APPLICATION_JSON).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(new Response("POST"))));
    }

    public static class ServicePost {
        public void service(ObjectResponse<Response> response) {
            response.send(new Response("POST"));
        }
    }

}
