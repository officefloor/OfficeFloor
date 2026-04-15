package net.officefloor.spring.starter.rest.web;

import jakarta.servlet.http.Cookie;
import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import net.officefloor.spring.starter.rest.web.common.RequestBodyEntity;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractWebVerification extends AbstractMockMvcVerification {

    @Test
    public void pathVariable() throws Exception {
        this.mvc.perform(get(this.getPath("/path/1")).accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("ID=1")));
    }

    @Test
    public void requestParam() throws Exception {
        this.mvc.perform(get(this.getPath("/query?name=value")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("value")));
    }

    @Test
    public void requestHeader() throws Exception {
        this.mvc.perform(get(this.getPath("/header")).header("header", "VALUE").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("VALUE")));
    }

    @Test
    public void cookieValue() throws Exception {
        this.mvc.perform(get(this.getPath("/cookie")).cookie(new Cookie("biscuit", "shortbread")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("shortbread"));
    }

    @Test
    public void requestBody() throws Exception {
        this.mvc.perform(post(this.getPath("/requestBody")).with(csrf())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new RequestBodyEntity("ENTITY"))))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("ENTITY")));
    }

    @Test
    public void responseStatus() throws Exception {
        this.mvc.perform(get(this.getPath("/responseStatus")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andExpect(content().string(equalTo("Response Status")));
    }

    @Test
    public void responseEntity() throws Exception {
        this.mvc.perform(get(this.getPath("/responseEntity")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().is(299))
                .andExpect(content().string(equalTo("Response Entity")));
    }

    @Test
    public void component() throws Exception {
        this.mvc.perform(get(this.getPath("/component")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("COMPONENT")));
    }

    @Test
    public void intercept() throws Exception {
        this.mvc.perform(get(this.getPath("/intercept", true)).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Intercepted")));
    }

    @Test
    public void requestPart() throws Exception {
        this.mvc.perform(MockMvcRequestBuilders.multipart(this.getPath("/requestPart"))
                        .file(new MockMultipartFile("file", "Upload.txt", "plain/text", "Hello from File".getBytes()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("file=Upload.txt, content=Hello from File")));
    }

    @Test
    public void propertyValue() throws Exception {
        this.mvc.perform(get(this.getPath("/propertyValue")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("TestValue")));
    }

    @Test
    public void controllerAdvice() throws Exception {
        this.mvc.perform(get(this.getPath("/controllerAdvice")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo(this.getPath("/controllerAdvice") + ": TEST")));
    }

    @Test
    public void initBinder() throws Exception {
        this.mvc.perform(get(this.getPath("/initBinder?status=start")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("begin"));
    }

    @Test
    public void httpServletRequest() throws Exception {
        this.mvc.perform(get(this.getPath("/httpServletRequest?name=Servlet")).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Servlet")));
    }

    @Test
    public void httpServletResponse() throws Exception {
        this.mvc.perform(get(this.getPath("/httpServletResponse")).accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Servlet")));
    }

    @Test
    public void cors() throws Exception {
        this.mvc.perform(get(this.getPath("/cors"))
                        .header("Origin", "https://example.com")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://example.com"))
                .andExpect(content().string(equalTo("CORS")));
    }

}