package net.officefloor.spring.starter.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.bind.annotation.RequestParam;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractBaselineSpringRestVerification {

    protected @Autowired MockMvc mvc;

    private @Autowired MockRestController restController;

    private @Autowired ObjectMapper mapper;

    private @Autowired UserRepository userRepository;

    @BeforeEach
    public void loadTestData() {
        for (int i = 1; i < 100; i++) {
            this.userRepository.save(new User(null, "User_" + i, "Description_" + i, true));
        }
    }

    @AfterEach
    public void clearData() {
        this.userRepository.deleteAll();
    }

    /*
     * ======================= Security ==========================
     */

    @Test
    public void login() throws Exception {
        this.mvc.perform(post("/login")
                        .param("username", "user")
                        .param("password", "password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/greeting"));
    }

    @Test
    public void loginFail() throws Exception {
        this.mvc.perform(post("/login")
                        .param("username", "user")
                        .param("password", "incorrect")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    public void greeting_guest() throws Exception {
        this.mvc.perform(get("/greeting"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void greeting_user() throws Exception {
        this.mvc.perform(get("/greeting"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello User")));
    }

    @Test
    @WithUserDetails("user")
    public void greeting_userDetails() throws Exception {
        this.mvc.perform(get("/greeting"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello user")));
    }


    @Test
    public void hello() throws Exception {
        this.mvc.perform(get("/hello").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Hello")));
    }

    @Test
    public void helloUser() throws Exception {
        this.mvc.perform(get("/hello/USER").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Hello USER")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void me() throws Exception {
        this.mvc.perform(get("/me").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("User")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void authentication() throws Exception {
        this.mvc.perform(get("/authentication").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("User")));
    }

    @Test
    @WithMockUser(username = "User", roles = "ACCESS")
    public void preAuthorize_Access() throws Exception {
        this.mvc.perform(get("/preauthorize").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Accessed")));
    }

    @Test
    @WithMockUser(username = "User", roles = "NO_ACCESS")
    public void preAuthorize_NoAccess() throws Exception {
        this.mvc.perform(get("/preauthorize").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(equalTo("")));
    }

    @Test
    @WithMockUser(username = "User", roles = "ACCESS")
    public void secured_Access() throws Exception {
        this.mvc.perform(get("/secured").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Accessed")));
    }

    @Test
    @WithMockUser(username = "User", roles = "NO_ACCESS")
    public void secured_NoAccess() throws Exception {
        this.mvc.perform(get("/secured").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(equalTo("")));
    }

    @Test
    @WithMockUser(username = "User", roles = "ACCESS")
    public void rolesAllowed_Access() throws Exception {
        this.mvc.perform(get("/rolesAllowed").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Accessed")));
    }

    @Test
    @WithMockUser(username = "User", roles = "NO_ACCESS")
    public void rolesAllowed_NoAccess() throws Exception {
        this.mvc.perform(get("/rolesAllowed").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(equalTo("")));
    }

    /*
     * ===================== MVC =========================
     */

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void component() throws Exception {
        this.mvc.perform(get("/component").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("COMPONENT")));
    }

    @Test
    public void helloDirect()  throws Exception {
        ResponseEntity<String> response = this.restController.hello();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Hello", response.getBody());
    }

    @Test
    @WithMockUser(username= "User", roles = "USER")
    public void intercept() throws Exception {
        this.mvc.perform(get("/intercept").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Intercepted")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void multipart() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.multipart("/requestPart")
                        .file(new MockMultipartFile("file", "Upload.txt", "plain/text", "Hello from File".getBytes()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("file=Upload.txt, content=Hello from File")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void value() throws Exception {
        this.mvc.perform(get("/value").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("TestValue")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void controllerAdvice() throws Exception {
        this.mvc.perform(get("/controllerAdvice").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("/controllerAdvice: TEST")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void initBinder() throws Exception {
        this.mvc.perform(get("/initBinder?status=start").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("begin"));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void thymeleaf() throws Exception {
        this.mvc.perform(get("/thymeleaf?name=Spring").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("<html><body>Hello Spring</body></html>")));
    }

    /*
     * ================== Validation =====================
     */

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void valid() throws Exception {
        this.mvc.perform(post("/valid").accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new MockRestController.ValidRequest(0)))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void bindingResult() throws Exception {
        this.mvc.perform(post("/bindingResult").accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new MockRestController.ValidRequest(0)))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Errors: 1")));
    }

    /*
     * ========================= Data =============================
     */

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void retrieveUser() throws Exception {
        this.mvc.perform(get("/user/User_1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Description_1")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void countUsers() throws Exception {
        this.mvc.perform(get("/userCount").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("99")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void pagination() throws Exception {
        this.mvc.perform(get("/users?page=0&size=10").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("total=99, pages=10, size=10, elements=10")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void sortedUsers() throws Exception {
        this.mvc.perform(get("/users/sorted").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("User_1")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void customQuery() throws Exception {
        this.mvc.perform(get("/activeUser/User_1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Description_1")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void modifyingQuery() throws Exception {
        this.mvc.perform(post("/deactivate/User_1").with(csrf()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Deactivated: 1")));
        this.mvc.perform(get("/activeUser/User_1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Not Found")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void createUser() throws Exception {
        this.mvc.perform(post("/user").with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"NewUser\",\"description\":\"New Description\",\"active\":true}"))
                .andExpect(status().isCreated())
                .andExpect(content().string(equalTo("NewUser")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void deleteUser() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.delete("/user/User_1").with(csrf())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        this.mvc.perform(get("/activeUser/User_1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Not Found")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void duplicateUser() throws Exception {
        this.mvc.perform(post("/user").with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"User_1\",\"description\":\"Duplicate\",\"active\":true}"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void findById() throws Exception {
        this.mvc.perform(get("/userById/User_1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Description_1")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void updateUser() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.put("/user/User_1").with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"Updated Description\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Updated Description")));
        this.mvc.perform(get("/user/User_1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Updated Description")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void existsById() throws Exception {
        this.mvc.perform(get("/userExists/User_1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("true")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void transaction() throws Exception {
        this.mvc.perform(get("/transaction").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Active")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void noTransaction() throws Exception {
        this.mvc.perform(get("/noTransaction").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("None")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void readOnlyTransaction() throws Exception {
        this.mvc.perform(get("/readOnlyTransaction").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("ReadOnly")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void transactionRollback() throws Exception {
        this.mvc.perform(post("/saveAndFail").with(csrf()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        this.mvc.perform(get("/userCount").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("99")));
    }

}
