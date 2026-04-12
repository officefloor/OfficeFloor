package net.officefloor.spring.starter.rest.validation;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import net.officefloor.spring.starter.rest.validation.common.ValidRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractValidationVerification extends AbstractMockMvcVerification {

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void valid() throws Exception {
        this.mvc.perform(post(this.getPath("/valid")).accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ValidRequest(0)))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void bindingResult() throws Exception {
        this.mvc.perform(post(this.getPath("/bindingResult")).accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ValidRequest(0)))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Errors: 1")));
    }

}
