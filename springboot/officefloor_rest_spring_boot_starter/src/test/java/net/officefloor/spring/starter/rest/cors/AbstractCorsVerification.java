package net.officefloor.spring.starter.rest.cors;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractCorsVerification extends AbstractMockMvcVerification {

    @Test
    public void origins() throws Exception {
        this.mvc.perform(get(this.getPath("/origins"))
                        .header("Origin", "https://example.com"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"))
                .andExpect(content().string(equalTo("origin")));
    }

}
