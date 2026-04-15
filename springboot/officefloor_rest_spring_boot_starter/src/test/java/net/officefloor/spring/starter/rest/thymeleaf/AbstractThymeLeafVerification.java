package net.officefloor.spring.starter.rest.thymeleaf;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractThymeLeafVerification extends AbstractMockMvcVerification {

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void thymeleaf() throws Exception {
        this.mvc.perform(get(this.getPath("/thymeleaf?name=Spring")).accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("<html><body>Hello Spring</body></html>")));
    }

    @Test
    public void modelAttribute() throws Exception {
        this.mvc.perform(post(this.getPath("/modelAttribute")).accept(MediaType.TEXT_PLAIN)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("name=Daniel&email=daniel@officefloor.net"))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("name=Daniel, email=daniel@officefloor.net")));
    }

    @Test
    public void eachIteration() throws Exception {
        this.mvc.perform(get(this.getPath("/each")).accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Alpha")))
                .andExpect(content().string(containsString("Beta")))
                .andExpect(content().string(containsString("Gamma")));
    }

    @Test
    public void conditionalRenderTrue() throws Exception {
        this.mvc.perform(get(this.getPath("/conditional?visible=true")).accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Shown")))
                .andExpect(content().string(not(containsString("Hidden"))));
    }

    @Test
    public void conditionalRenderFalse() throws Exception {
        this.mvc.perform(get(this.getPath("/conditional?visible=false")).accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Hidden")))
                .andExpect(content().string(not(containsString("Shown"))));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void secureAuthorizeAuthenticated() throws Exception {
        this.mvc.perform(get(this.getPath("/secure")).accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("User Content")))
                .andExpect(content().string(not(containsString("Guest Content"))));
    }

    @Test
    public void secureAuthorizeGuest() throws Exception {
        this.mvc.perform(get(this.getPath("/secure")).accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Guest Content")))
                .andExpect(content().string(not(containsString("User Content"))));
    }

    @Test
    public void formFields() throws Exception {
        this.mvc.perform(get(this.getPath("/form")).accept(MediaType.TEXT_HTML))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("id=\"name\"")))
                .andExpect(content().string(containsString("id=\"email\"")));
    }

}
