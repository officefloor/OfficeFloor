package net.officefloor.spring.starter.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
    @WithMockUser(username = "User", roles = "USER")
    public void me() throws Exception {
        this.mvc.perform(get("/me").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("User")));
    }

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

}
