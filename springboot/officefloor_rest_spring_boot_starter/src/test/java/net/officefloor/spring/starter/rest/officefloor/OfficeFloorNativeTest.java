package net.officefloor.spring.starter.rest.officefloor;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirms native {@link net.officefloor.frame.api.manage.OfficeFloor} functionality can sit beside Spring.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class OfficeFloorNativeTest extends AbstractMockMvcVerification {

    @Test
    public void httpPathParameter() throws Exception {
        this.mvc.perform(get(this.getPath("/path/1")))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("ID=1")));
    }

    @Test
    public void httpQueryParameter() throws Exception {
        this.mvc.perform(get(this.getPath("/query?name=value")))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("value")));
    }

    @Test
    public void httpHeaderParameter() throws Exception {
        this.mvc.perform(get(this.getPath("/header")).header("header", "VALUE"))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("VALUE")));
    }

    @Test
    public void httpObject() throws Exception {
        this.mvc.perform(post(this.getPath("/httpObject")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new RequestEntity("ENTITY"))))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("ENTITY")));
    }

    @Test
    public void httpResponse() throws Exception {
        this.mvc.perform(get(this.getPath("/httpResponse")))
                .andExpect(status().isCreated())
                .andExpect(content().string(equalTo("created")));
    }

    @Test
    public void httpServletRequest() throws Exception {
        this.mvc.perform(get(this.getPath("/httpServletRequest?name=Servlet")))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Servlet")));
    }

    @Test
    public void httpServletResponse() throws Exception {
        this.mvc.perform(get(this.getPath("/httpServletResponse")))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Servlet")));
    }

}
