package net.officefloor.spring.starter.rest.mvc.spring;

import net.officefloor.spring.starter.rest.mvc.common.ContentResponse;
import net.officefloor.spring.starter.rest.mvc.common.MvcException;
import net.officefloor.spring.starter.rest.mvc.common.MvcStatus;
import net.officefloor.spring.starter.rest.mvc.common.QualifiedService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping("/spring/mvc")
public class MvcRestController {

    private @Autowired @Qualifier("primary") QualifiedService primaryService;

    private @Autowired @Qualifier("secondary") QualifiedService secondaryService;

    private @Autowired RequestScopedBean requestScopedBean;

    @GetMapping("/request-param-default")
    public String requestParamDefault(
            @RequestParam(name = "value", required = false, defaultValue = "default-value") String value) {
        return value;
    }

    @GetMapping("/request-param-absent")
    public String requestParamAbsent(
            @RequestParam(name = "value", required = false) String value) {
        return value != null ? value : "absent";
    }

    @GetMapping("/request-header-default")
    public String requestHeaderDefault(
            @RequestHeader(name = "X-Optional", required = false, defaultValue = "default-header") String header) {
        return header;
    }

    @GetMapping("/cookie-default")
    public String cookieDefault(
            @CookieValue(name = "token", required = false, defaultValue = "default-cookie") String token) {
        return token;
    }

    @GetMapping("/rest-advice-error")
    public void restAdviceError() throws MvcException {
        throw new MvcException("TEST_CODE", "Test error message");
    }

    @GetMapping(value = "/content", produces = MediaType.APPLICATION_JSON_VALUE)
    public ContentResponse contentJson() {
        return new ContentResponse("content");
    }

    @GetMapping(value = "/content", produces = MediaType.TEXT_PLAIN_VALUE)
    public String contentText() {
        return "content";
    }

    @PostMapping(value = "/consume-json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String consumeJson(@RequestBody ContentResponse body) {
        return "Consumed";
    }

    @GetMapping("/problem-detail")
    public ResponseEntity<ProblemDetail> problemDetail() {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, "This is a test problem");
        detail.setTitle("Test Problem");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(detail);
    }

    @GetMapping("/filter-check")
    public String filterCheck() {
        return "ok";
    }

    @GetMapping("/qualifier/primary")
    public String qualifierPrimary() {
        return primaryService.getValue();
    }

    @GetMapping("/qualifier/secondary")
    public String qualifierSecondary() {
        return secondaryService.getValue();
    }

    @GetMapping("/request-scope")
    public String requestScope(@RequestParam(name = "token") String token) {
        requestScopedBean.setToken(token);
        return requestScopedBean.getToken();
    }

    @GetMapping("/global-binder")
    public String globalBinder(@RequestParam(name = "status") MvcStatus status) {
        return status.name();
    }

}
