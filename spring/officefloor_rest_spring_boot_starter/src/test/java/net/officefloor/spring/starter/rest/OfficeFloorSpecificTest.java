package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.officefloor.spring.starter.rest.data.jpa.User;
import net.officefloor.spring.starter.rest.data.jpa.UserRepository;
import net.officefloor.spring.starter.rest.view.ViewResponse;
import net.officefloor.spring.starter.rest.web.BindingTypes;
import net.officefloor.spring.starter.rest.web.MockComponent;
import net.officefloor.spring.starter.rest.web.MockException;
import net.officefloor.web.HttpHeaderParameter;
import net.officefloor.web.HttpObject;
import net.officefloor.web.HttpPathParameter;
import net.officefloor.web.HttpQueryParameter;
import net.officefloor.web.HttpResponse;
import net.officefloor.web.ObjectResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.internal.util.io.IOUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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

import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username= "User", roles = "USER")
public class OfficeFloorSpecificTest {

    @DynamicPropertySource
    public static void properties(DynamicPropertyRegistry registry) {
        registry.add("officefloor.rest.config.testcase", () -> OfficeFloorSpecificTest.class.getName());
    }

    protected @Autowired MockMvc mvc;

    protected @Autowired ObjectMapper mapper;

    protected @Autowired UserRepository userRepository;

    @BeforeEach
    public void loadTestData() {
        for (int i = 1; i < 100; i++) {
            this.userRepository.save(new User(null, "User_" + i, "Description_" + i, true, null, null, null));
        }
    }

    @AfterEach
    public void clearData() {
        this.userRepository.deleteAll();
    }

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

    /*
     * ======================= Security =========================
     */

    @Test
    public void spring_GET_UserDetails() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/userDetails", new Response("User"));
    }

    public static class SpringUserDetails {
        public void service(@AuthenticationPrincipal UserDetails user, ObjectResponse<Response> response) {
            response.send(new Response(user.getUsername()));
        }
    }

    @Test
    public void spring_GET_Authentication() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/authentication", new Response("User"));
    }

    public static class SpringAuthentication {
        public void service(Authentication authentication, ObjectResponse<Response> response) {
            response.send(new Response(authentication.getName()));
        }
    }

    @Test
    @WithMockUser(username = "user", roles = "ACCESS")
    public void spring_get_preAuthorize_Access() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/preAuthorize", new Response("Accessed"));
    }

    @Test
    public void spring_get_preAuthorize_NoAccess() throws Exception {
        SpringPreAuthorize.isAccessed = false;
        this.mvc.perform(MockMvcRequestBuilders.get("/spring/preAuthorize").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(equalTo("")));
        assertFalse(SpringPreAuthorize.isAccessed, "Should not be accessed");
    }

    public static class SpringPreAuthorize {

        public static volatile boolean isAccessed = false;

        @PreAuthorize("hasRole('ACCESS')")
        public void service(ObjectResponse<Response> response) {
            isAccessed = true;
            response.send(new Response("Accessed"));
        }
    }

    @Test
    @WithMockUser(username = "user", roles = "ACCESS")
    public void spring_get_postAuthorize_Access() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/postAuthorize", new Response("Accessed"));
    }

    @Test
    public void spring_get_postAuthorize_NoAccess() throws Exception {
        SpringPostAuthorize.isAccessed = false;
        this.mvc.perform(MockMvcRequestBuilders.get("/spring/postAuthorize").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(equalTo("")));
        assertTrue(SpringPostAuthorize.isAccessed, "Should be accessed then fail authorization");
    }

    public static class SpringPostAuthorize {

        public static volatile boolean isAccessed = false;

        @PostAuthorize("hasRole('ACCESS')")
        public void service(ObjectResponse<Response> response) {
            isAccessed = true;
            response.send(new Response("Accessed"));
        }
    }

    @Test
    @WithMockUser(username = "User", roles = "ACCESS")
    public void spring_get_secured_Access() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/secured", new Response("Accessed"));
    }

    @Test
    public void spring_get_secured_NoAccess() throws Exception {
        this.mvc.perform(get("/spring/secured").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(equalTo("")));
    }

    public static class SpringSecured {
        @Secured("ROLE_ACCESS")
        public void service(ObjectResponse<Response> response) {
            response.send(new Response("Accessed"));
        }
    }

    @Test
    @WithMockUser(username = "User", roles = "ACCESS")
    public void spring_get_rolesAllowed_Access() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/rolesAllowed", new Response("Accessed"));
    }

    @Test
    public void spring_get_rolesAllowed_NoAccess() throws Exception {
        this.mvc.perform(get("/spring/rolesAllowed").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(equalTo("")));
    }

    public static class SpringRolesAllowed {
        @RolesAllowed("ROLE_ACCESS")
        public void service(ObjectResponse<Response> response) {
            response.send(new Response("Accessed"));
        }
    }

    /*
     * ========================== Spring MVC =========================
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
            String content = String.join("", IOUtil.readLines(file.getInputStream()));
            response.send(new Response("file=" + file.getOriginalFilename() + ", content=" + content));
        }
    }

    /*
     * ========================= Validation ========================
     */

    @Test
    public void spring_POST_valid() throws Exception {
        this.mvc.perform(post("/spring/valid").accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ValidRequest(0)))
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
                        .content(mapper.writeValueAsString(new ValidRequest(0)))
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

    /*
     * ====================== ControllerAdvice =====================
     */

    @Test
    public void spring_GET_controllerAdvice() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.request(HttpMethod.GET, "/spring/controllerAdvice"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("/spring/controllerAdvice: OfficeFloor"));
    }

    public static class SpringControllerAdvice {
        public void service() throws MockException {
            throw new MockException("OfficeFloor");
        }
    }

    @Test
    public void spring_GET_initBinder() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/initBinder?status=complete", new Response("end"));
    }

    public static class SpringInitBinder {
        public void service(@RequestParam(name = "status") BindingTypes types, ObjectResponse<Response> response) {
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

    /*
     * ========================== View =========================
     */

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

    /*
     * ========================== Data =========================
     */

    @Test
    public void existsById() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/userExists/User_1", new Response("true"));
    }

    public static class SpringExistsById {
        public void service(@PathVariable(name = "name") String name, UserRepository userRepository, ObjectResponse<Response> response) {
            User user = userRepository.findByName(name).orElseThrow();
            response.send(new Response(String.valueOf(userRepository.existsById(user.getId()))));
        }
    }

    @Test
    public void findById() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/userById/User_1", new Response("Description_1"));
    }

    public static class SpringUserById {
        public void service(@PathVariable(name = "name") String name, UserRepository userRepository, ObjectResponse<Response> response) {
            User user = userRepository.findByName(name).orElseThrow();
            String description = userRepository.findById(user.getId())
                    .map(User::getDescription)
                    .orElse("Not Found");
            response.send(new Response(description));
        }
    }

    @Test
    public void findByName() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/user/User_1", new Response("Description_1"));
    }

    public static class SpringUserByName {
        public void service(@PathVariable(name = "name") String name, UserRepository userRepository, ObjectResponse<Response> response) {
            User user = userRepository.findByName(name).get();
            response.send(new Response(user.getDescription()));
        }
    }

    @Test
    public void findAll() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/allUsersCount", new Response("99"));
    }

    public static class SpringFindAll {
        public void service(UserRepository userRepository, ObjectResponse<Response> response) {
            response.send(new Response(String.valueOf(userRepository.findAll().size())));
        }
    }

    @Test
    public void countUsers() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/userCount", new Response("99"));
    }

    public static class SpringCountUsers {
        public void service(UserRepository userRepository, ObjectResponse<Response> response) {
            response.send(new Response(String.valueOf(userRepository.count())));
        }
    }

    @Test
    public void sortedUsers() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/users/sorted", new Response("User_1"));
    }

    public static class SpringSortedUsers {
        public void service(UserRepository userRepository, ObjectResponse<Response> response) {
            response.send(new Response(userRepository.findAll(Sort.by("name").ascending()).get(0).getName()));
        }
    }

    @Test
    public void customQuery() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/activeUser/User_1", new Response("Description_1"));
    }

    public static class SpringCustomQuery {
        public void service(@PathVariable(name = "name") String name, UserRepository userRepository, ObjectResponse<Response> response) {
            String description = userRepository.findActiveUserByName(name)
                    .map(User::getDescription)
                    .orElse("Not Found");
            response.send(new Response(description));
        }
    }

    @Test
    public void nativeSqlQuery() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/userDescriptionNative/User_1", new Response("Description_1"));
    }

    public static class SpringNativeSqlQuery {
        public void service(@PathVariable(name = "name") String name, UserRepository userRepository, ObjectResponse<Response> response) {
            response.send(new Response(userRepository.findDescriptionByNameNative(name)));
        }
    }

    @Test
    public void modifyingQuery() throws Exception {
        this.mvc.perform(post("/spring/deactivate/User_1").with(csrf()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(new Response("Deactivated: 1"))));
        this.assertRequest(HttpMethod.GET, "/spring/activeUser/User_1", new Response("Not Found"));
    }

    public static class SpringModifyingQuery {
        public void service(@PathVariable(name = "name") String name, UserRepository userRepository, ObjectResponse<Response> response) {
            User user = userRepository.findByName(name).orElseThrow();
            int count = userRepository.deactivateUser(user.getId());
            response.send(new Response("Deactivated: " + count));
        }
    }

    @Test
    public void pagination() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/users?page=0&size=10", new Response("total=99, pages=10, size=10, elements=10"));
    }

    public static class SpringPagination {
        public void service(@RequestParam(name = "page") int page, @RequestParam(name = "size") int size, UserRepository userRepository, ObjectResponse<Response> response) {
            Page<User> result = userRepository.findByActive(true, PageRequest.of(page, size));
            response.send(new Response("total=" + result.getTotalElements() + ", pages=" + result.getTotalPages()
                    + ", size=" + result.getSize() + ", elements=" + result.getNumberOfElements()));
        }
    }

    @Test
    public void paginationNextPage() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/users?page=1&size=10", new Response("total=99, pages=10, size=10, elements=10"));
    }

    @Test
    public void paginationLastPage() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/users?page=9&size=10", new Response("total=99, pages=10, size=10, elements=9"));
    }

    @Test
    public void createUser() throws Exception {
        this.mvc.perform(post("/spring/user").with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new User(null, "NewUser", "New Description", true, null, null, null))))
                .andExpect(status().isCreated())
                .andExpect(content().json(mapper.writeValueAsString(new Response("NewUser"))));
    }

    public static class SpringCreateUser {
        public void service(@RequestBody User user, UserRepository userRepository, @HttpResponse(status = 201) ObjectResponse<Response> response) {
            User newUser = userRepository.save(user);
            response.send(new Response(newUser.getName()));
        }
    }



    @Test
    public void spring_GET_transaction() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/transaction", new Response("Active"));
    }

    @Test
    public void spring_GET_noTransaction() throws Exception {
        this.assertRequest(HttpMethod.GET, "/spring/noTransaction", new Response("None"));
    }

    public static class SpringTransaction {
        public void service(UserRepository userRepository, ObjectResponse<Response> response) {
            String state = TransactionSynchronizationManager.isActualTransactionActive() ? "Active" : "None";
            response.send(new Response(state));
        }
    }


    /*
     * ========================== Testing =========================
     */

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
