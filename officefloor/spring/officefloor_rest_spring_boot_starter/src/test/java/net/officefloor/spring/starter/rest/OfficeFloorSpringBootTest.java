package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.web.HttpHeaderParameter;
import net.officefloor.web.HttpObject;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.ObjectResponse;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.io.IOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
        this.mvc.perform(MockMvcRequestBuilders.get("/spring/httpServletResponse"))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Servlet")));
    }

    public static class SpringHttpServletResponse {
        public void service(HttpServletResponse response) throws IOException {
            response.getWriter().write("Servlet");
        }
    }

    @Test
    public void spring_GET_UserDetails() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/userDetails", new Response("User"));
    }

    public static class SpringUserDetails {
        public void service(@AuthenticationPrincipal UserDetails user, ObjectResponse<Response> response) {
            response.send(new Response(user.getUsername()));
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
        public void service(@PathVariable(name = "id") String id, ObjectResponse<Response> response) {
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

    @Test
    public void spring_GET_headerParameter() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/header", new Response("HEADER"), "header", "HEADER");
    }

    public static class SpringHeaderParameter {
        public void service(@RequestHeader(name = "header") String header, ObjectResponse<Response> response) {
            response.send(new Response(header));
        }
    }

    @Test
    public void spring_GET_cookieParameter() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .request(HttpMethod.GET, "/spring/cookie")
                .accept(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("biscuit", "shortbread"));

        // Undertake request
        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(new Response("shortbread"))));
    }

    public static class SpringCookieParameter {
        public void service(@CookieValue(name = "biscuit") String cookie, ObjectResponse<Response> response) {
            response.send(new Response(cookie));
        }
    }

    @Test
    public void spring_POST_requestBody() throws Exception {
        this.assertRequest(HttpMethod.POST, "/spring/requestBody", new RequestEntity("ENTITY"), new Response("ENTITY"));
    }

    public static class SpringRequestBody {
        public void service(@RequestBody RequestEntity entity, ObjectResponse<Response> response) {
            response.send(new Response(entity.getRequest()));
        }
    }

    @Test
    public void spring_GET_modelAttribute() throws Exception {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .request(HttpMethod.POST, "/spring/modelAttribute")
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .content("name=Daniel&email=daniel@officefloor.net");

        // Undertake request
        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(new Response("name=Daniel, email=daniel@officefloor.net"))));
    }

    @Data
    public static class MockModelAttribute {
        private String name;
        private String email;
    }

    public static class SpringModelAttribute {
        public void service(@ModelAttribute MockModelAttribute attributes, ObjectResponse<Response> response) {
            response.send(new Response("name=" + attributes.getName() + ", email=" + attributes.getEmail()));
        }
    }

    @Test
    public void spring_GET_responseEntity() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.request(HttpMethod.GET, "/spring/responseEntity"))
                .andExpect(status().isCreated())
                .andExpect(header().string("name", "value"))
                .andExpect(content().json(mapper.writeValueAsString(new Response("BODY"))));
    }

    public static class SpringResponseEntity {
        public void service(ObjectResponse<ResponseEntity<Response>> response) {
            response.send(ResponseEntity
                    .status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("name", "value")
                    .body(new Response("BODY")));
        }
    }

    @Test
    public void spring_POST_requestPart() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.multipart("/spring/requestPart")
                        .file(new MockMultipartFile("file", "Upload.txt", "plain/text", "Hello from File".getBytes()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(new Response("file=Upload.txt, content=Hello from File"))));
    }

    public static class SpringRequestPart {
        public void service(@RequestPart(name = "file") MultipartFile file, ObjectResponse<Response> response) throws IOException {
            String content = IOUtil.readLines(file.getInputStream()).stream().collect(Collectors.joining());
            response.send(new Response("file=" + file.getOriginalFilename() + ", content=" + content));
        }
    }

    @Test
    public void spring_POST_valid() throws Exception {
        this.mvc.perform(post("/spring/valid").accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new MockRestController.ValidRequest(0)))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("")));
    }

    @Validated
    public static class SpringValid {
        public void service(@Valid @RequestBody ValidRequest request, ObjectResponse<Response> response) {
            fail("Should not be invoked as invalid (" + request.getAmount() + ")");
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ValidRequest {
        private @Min(1) int amount;
    }

    @Test
    public void spring_POST_bindingResult() throws Exception {
        this.mvc.perform(post("/spring/bindingResult").accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new MockRestController.ValidRequest(0)))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().json(mapper.writeValueAsString(new Response("Errors: 1"))));
    }

    @Validated
    public static class SpringBindingResult {
        public void service(@Valid @RequestBody ValidRequest request, BindingResult result, ObjectResponse<ResponseEntity<Response>> response) {
            if (result.hasErrors()) {
                response.send(ResponseEntity.badRequest().body(new Response("Errors: " + result.getErrorCount())));
                return;
            }
            response.send(ResponseEntity.ok().body(new Response("OK")));
        }
    }

    @Test
    public void spring_GET_value() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/value", new Response("TestValue"));
    }

    public static class SpringValue {
        public void service(@Value("${officefloor.spring.test.value}") String propertyValue, ObjectResponse<Response> response) {
            response.send(new Response(propertyValue));
        }
    }

    @Test
    public void spring_GET_controllerAdvice() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.request(HttpMethod.GET, "/spring/controllerAdvice"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("/spring/controllerAdvice: OfficeFloor"));
    }

    public static class SpringControllerAdvice {
        public void service() throws MockRestControllerAdvice.MockException {
            throw new MockRestControllerAdvice.MockException("OfficeFloor");
        }
    }

    @Test
    public void spring_GET_initBinder() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/initBinder?status=complete", new Response("end"));
    }

    public static class SpringInitBinder {
        public void service(@RequestParam(name = "status") MockRestControllerAdvice.BindingTypes types, ObjectResponse<Response> response) {
            switch (types) {
                case START:
                    response.send(new Response("begin"));
                    break;
                case COMPLETE:
                    response.send(new Response("end"));
                    break;
            }
        }
    }

    @Test
    public void spring_GET_thymeleaf() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.request(HttpMethod.GET, "/spring/thymeleaf?name=OfficeFloor"))
                .andExpect(status().isOk())
                .andExpect(content().string("<html><body>Hello OfficeFloor</body></html>"));
    }

    public static class SpringThymeleaf {
        public void service(@RequestParam(name = "name", defaultValue = "World") String name,
                            Model model, ViewResponse response) {
            model.addAttribute("name", name);
            response.send("thymeleaf");
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
            request = request.contentType(MediaType.APPLICATION_JSON);
            request = request.content(this.mapper.writeValueAsString(requestEntity));
        }

        // Undertake request
        this.mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }
}
