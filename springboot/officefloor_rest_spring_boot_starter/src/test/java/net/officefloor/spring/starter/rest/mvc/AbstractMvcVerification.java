package net.officefloor.spring.starter.rest.mvc;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import net.officefloor.spring.starter.rest.mvc.common.ContentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractMvcVerification extends AbstractMockMvcVerification {

    // ── Item 1: Optional parameters with defaults ─────────────────────────────

    @Test
    public void requestParamDefault() throws Exception {
        this.mvc.perform(get(this.getPath("/request-param-default"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("default-value")));
    }

    @Test
    public void requestParamAbsent() throws Exception {
        this.mvc.perform(get(this.getPath("/request-param-absent"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("absent")));
    }

    @Test
    public void requestHeaderDefault() throws Exception {
        this.mvc.perform(get(this.getPath("/request-header-default"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("default-header")));
    }

    @Test
    public void cookieDefault() throws Exception {
        this.mvc.perform(get(this.getPath("/cookie-default"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("default-cookie")));
    }

    // ── Item 2: @RestControllerAdvice with structured JSON error body ─────────

    @Test
    public void restAdviceErrorBody() throws Exception {
        this.mvc.perform(get(this.getPath("/rest-advice-error"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value("TEST_CODE"))
                .andExpect(jsonPath("$.message").value("Test error message"));
    }

    // ── Item 3: Content negotiation via produces / consumes ───────────────────

    @Test
    public void produceJson() throws Exception {
        this.mvc.perform(get(this.getPath("/content"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.value").value("content"));
    }

    @Test
    public void produceText() throws Exception {
        this.mvc.perform(get(this.getPath("/content"))
                        .accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(content().string(equalTo("content")));
    }

    @Test
    public void consumeJson() throws Exception {
        this.mvc.perform(post(this.getPath("/consume-json")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.mapper.writeValueAsString(new ContentResponse("input"))))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Consumed")));
    }

    @Test
    public void contentNegotiationReject() throws Exception {
        this.mvc.perform(get(this.getPath("/content"))
                        .accept(MediaType.APPLICATION_XML))
                .andExpect(status().isNotAcceptable());
    }

    // ── Item 4: ProblemDetail (RFC 7807) ─────────────────────────────────────

    @Test
    public void problemDetail() throws Exception {
        this.mvc.perform(get(this.getPath("/problem-detail"))
                        .accept(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Test Problem"))
                .andExpect(jsonPath("$.detail").value("This is a test problem"))
                .andExpect(jsonPath("$.status").value(422));
    }

    // ── Item 5: Redirect and RedirectAttributes ───────────────────────────────

    @Test
    public void redirect() throws Exception {
        this.mvc.perform(get(this.getPath("/redirect")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mvc-destination"));
    }

    @Test
    public void redirectWithAttribute() throws Exception {
        this.mvc.perform(get(this.getPath("/redirect-attrs")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mvc-destination?status=ok"));
    }

    // ── Item 6: Servlet Filter sets response header ───────────────────────────

    @Test
    public void filterApplied() throws Exception {
        this.mvc.perform(get(this.getPath("/filter-check")))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Filter-Applied", equalTo("true")));
    }

    // ── Item 7: @Qualifier-based bean disambiguation ──────────────────────────

    @Test
    public void qualifierPrimary() throws Exception {
        this.mvc.perform(get(this.getPath("/qualifier/primary"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("PRIMARY")));
    }

    @Test
    public void qualifierSecondary() throws Exception {
        this.mvc.perform(get(this.getPath("/qualifier/secondary"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("SECONDARY")));
    }

    // ── Item 8: @RequestScope — each request gets its own bean instance ───────

    @Test
    public void requestScopeFirstToken() throws Exception {
        this.mvc.perform(get(this.getPath("/request-scope"))
                        .param("token", "abc123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("abc123")));
    }

    @Test
    public void requestScopeDifferentToken() throws Exception {
        this.mvc.perform(get(this.getPath("/request-scope"))
                        .param("token", "xyz789")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("xyz789")));
    }

    // ── Item 9: Global @InitBinder from plain @ControllerAdvice ──────────────

    @Test
    public void globalInitBinder() throws Exception {
        this.mvc.perform(get(this.getPath("/global-binder"))
                        .param("status", "active")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("ACTIVE")));
    }

    // ── Item 10: @MatrixVariable extraction from path segments ───────────────

    @Test
    public void matrixVariable() throws Exception {
        this.mvc.perform(get(this.getPath("/matrix/segment;city=Chicago"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Chicago")));
    }

    @Test
    public void matrixVariableMultiple() throws Exception {
        this.mvc.perform(get(this.getPath("/matrix-multi/segment;color=red;year=2020"))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("red-2020")));
    }
}
