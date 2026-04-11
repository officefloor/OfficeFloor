package net.officefloor.spring.starter.rest.security;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractSecurityVerification extends AbstractMockMvcVerification {

    @Test
    public void login() throws Exception {
        this.mvc.perform(post(this.getPath("/login", true))
                        .param("username", "user")
                        .param("password", "password")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/spring/security/greeting"));
    }

    @Test
    public void loginFail() throws Exception {
        this.mvc.perform(post(this.getPath("/login", true))
                        .param("username", "user")
                        .param("password", "incorrect")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    @Test
    public void greeting_guest() throws Exception {
        this.mvc.perform(get(this.getPath("/greeting")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void greeting_user() throws Exception {
        this.mvc.perform(get(this.getPath("/greeting")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello User")));
    }

    @Test
    @WithUserDetails("user")
    public void greeting_userDetails() throws Exception {
        this.mvc.perform(get(this.getPath("/greeting")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hello user")));
    }

    @Test
    public void helloUser() throws Exception {
        this.mvc.perform(get(this.getPath("/hello/USER")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Hello USER")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void userDetails() throws Exception {
        this.mvc.perform(get(this.getPath("/userDetails")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("User")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void authentication() throws Exception {
        this.mvc.perform(get(this.getPath("/authentication")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("User")));
    }

    @Test
    @WithMockUser(username = "User", roles = "ACCESS")
    public void preAuthorize_Access() throws Exception {
        this.mvc.perform(get(this.getPath("/preAuthorize")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Accessed")));
    }

    @Test
    @WithMockUser(username = "User", roles = "NO_ACCESS")
    public void preAuthorize_NoAccess() throws Exception {
        this.mvc.perform(get(this.getPath("/preAuthorize")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(equalTo("")));
    }

    @Test
    @WithMockUser(username = "User", roles = "ACCESS")
    public void postAuthorize_Access() throws Exception {
        this.mvc.perform(get(this.getPath("/postAuthorize")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Accessed")));
    }

    @Test
    @WithMockUser(username = "User", roles = "NO_ACCESS")
    public void postAuthorize_NoAccess() throws Exception {
        this.mvc.perform(get(this.getPath("/postAuthorize")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(equalTo("")));
    }

    @Test
    @WithMockUser(username = "User", roles = "ACCESS")
    public void secured_Access() throws Exception {
        this.mvc.perform(get(this.getPath("/secured")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Accessed")));
    }

    @Test
    @WithMockUser(username = "User", roles = "NO_ACCESS")
    public void secured_NoAccess() throws Exception {
        this.mvc.perform(get(this.getPath("/secured")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(equalTo("")));
    }

    @Test
    @WithMockUser(username = "User", roles = "ACCESS")
    public void rolesAllowed_Access() throws Exception {
        this.mvc.perform(get(this.getPath("/rolesAllowed")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Accessed")));
    }

    @Test
    @WithMockUser(username = "User", roles = "NO_ACCESS")
    public void rolesAllowed_NoAccess() throws Exception {
        this.mvc.perform(get(this.getPath("/rolesAllowed")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().string(equalTo("")));
    }

}
