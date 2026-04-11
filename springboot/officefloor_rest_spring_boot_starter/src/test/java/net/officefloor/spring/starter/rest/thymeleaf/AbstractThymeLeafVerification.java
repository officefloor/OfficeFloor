package net.officefloor.spring.starter.rest.thymeleaf;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.equalTo;
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

}
