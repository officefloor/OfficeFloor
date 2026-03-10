package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username= "User", roles = "USER")
public class OfficeFloorSpringBootTest {

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("officefloor.rest.config.testcase", () -> OfficeFloorSpringBootTest.class.getName());
    }

    protected @Autowired MockMvc mvc;

    protected @Autowired ObjectMapper mapper;

    /*
     * ======================= OfficeFloor simple =======================
     */

    @Test
    public void GET_officefloor() throws Exception {
        this.assertRequest(HttpMethod.GET, "/officefloor", new Response("GET"));
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
    public void POST_officefloor() throws Exception {
        this.assertRequest(HttpMethod.POST, "/officefloor", new Response("POST"));
    }

    public static class ServicePost {
        public void service(ObjectResponse<Response> response) {
            response.send(new Response("POST"));
        }
    }

    /*
     * ======================= OfficeFloor annotations =======================
     */

    @Test
    public void GET_pathParameter() throws Exception {
        this.assertRequest(HttpMethod.GET, "/path/1", new Response("ID=1"));
    }

    public static class ServicePathParameter {
        public void service(@HttpPathParameter("id") String id, ObjectResponse<Response> response) {
            response.send(new Response("ID=" + id));
        }
    }

    @Test
    public void GET_queryParameter() throws Exception {
        this.assertRequest(HttpMethod.GET, "/query?name=value", new Response("value"));
    }

    public static class ServiceQueryParameter {
        public void service(@HttpQueryParameter("name") String name, ObjectResponse<Response> response) {
            response.send(new Response(name));
        }
    }

    @Test
    public void GET_header() throws Exception {
        this.assertRequest(HttpMethod.GET, "/header", new Response("VALUE"), "header", "VALUE");
    }

    public static class ServiceHeader {
        public void service(@HttpHeaderParameter("header") String header, ObjectResponse<Response> response) {
            response.send(new Response(header));
        }
    }

    @Test
    public void POST_object() throws Exception {
        this.assertRequest(HttpMethod.POST, "/object", new RequestEntity("ENTITY"), new Response("ENTITY"));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RequestEntity {
        private String request;
    }

    public static class ServiceObject {
        public void service(@HttpObject RequestEntity entity, ObjectResponse<Response> response) {
            response.send(new Response(entity.request));
        }
    }

    /*
     * ====================== Spring beans =============================
     */

    @Test
    public void spring_GET_component() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/component", new Response("COMPONENT"));
    }

    public static class SpringComponent {
        public void service(MockComponent bean, ObjectResponse<Response> response) {
            response.send(new Response(bean.getValue()));
        }
    }

    @Test
    public void spring_GET_HttpServletRequest() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/httpServletRequest?name=Servlet", new Response("Servlet"));
    }

    public static class SpringHttpServletRequest {
        public void service(HttpServletRequest request, ObjectResponse<Response> response) {
            response.send(new Response(request.getParameter("name")));
        }
    }

    @Test
    public void spring_GET_HttpServletResponse() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/httpServletResponse", new Response("Servlet"));
    }

    public static class SpringHttpServletResponse {
        public void service(HttpServletResponse response) {
            try {
                response.getWriter().write("Servlet");
            } catch (Exception ex) {
                fail(ex);
            }
        }
    }

    @Test
    public void spring_GET_UserDetails() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/userDetails", new Response("USER"));
    }

    public static class SpringUserDetails {
//        public void service(@AuthenticationPrincipal UserDetails user, ObjectResponse<Response> response) {
//            response.send(new Response(user.getUsername()));
//        }

        public void todoRemove(ObjectResponse<Response> response) {
            response.send(new Response("TODO implement obtaining UserDetails"));
        }
    }

    /*
     * ======================= Spring annotations =======================
     */

    @Test
    public void spring_GET_pathParameter() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/path/1", new Response("ID=1"));
    }

    public static class SpringPathParameter {
        public void service(@HttpPathParameter("id") @PathVariable String id, ObjectResponse<Response> response) {
            response.send(new Response("ID=" + id));
        }
    }

    @Test
    public void spring_GET_queryParameter() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/query?name=value", new Response("value"));
    }

    public static class SpringQueryParameter {
        public void service(@RequestParam(name = "name") String name, ObjectResponse<Response> response) {
            response.send(new Response(name));
        }
    }

    private void assertRequest(HttpMethod method, String path, Response expectedResponse, String... headerNameValues) throws Exception {
        this.assertRequest(method, path, null, expectedResponse, headerNameValues);
    }

    private void assertRequest(HttpMethod method, String path, Object requestEntity, Response expectedResponse, String... headerNameValues) throws Exception {

        // Create the request
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.request(method, path).accept(MediaType.APPLICATION_JSON);
        for (int i = 0; i < headerNameValues.length; i += 2) {
            request = request.header(headerNameValues[i], headerNameValues[i + 1]);
        }
        if (!method.matches("GET")) {
            request = request.with(csrf());
        }
        if (requestEntity != null) {
            request.header("Content-Type", "application/json");
            request = request.content(this.mapper.writeValueAsString(requestEntity));
        }

        // Undertake request
        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }
}
