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

    // Item 7: @Qualifier — field-injected to avoid ambiguity between the two implementations
    @Autowired @Qualifier("primary")
    private QualifiedService primaryService;

    @Autowired @Qualifier("secondary")
    private QualifiedService secondaryService;

    // Item 8: @RequestScope — proxy delegates to per-request instance
    @Autowired
    private RequestScopedBean requestScopedBean;

    // Item 1a: @RequestParam with defaultValue
    @GetMapping("/request-param-default")
    public String requestParamDefault(
            @RequestParam(name = "value", required = false, defaultValue = "default-value") String value) {
        return value;
    }

    // Item 1b: @RequestParam optional with no default (null becomes "absent")
    @GetMapping("/request-param-absent")
    public String requestParamAbsent(
            @RequestParam(name = "value", required = false) String value) {
        return value != null ? value : "absent";
    }

    // Item 1c: @RequestHeader with defaultValue
    @GetMapping("/request-header-default")
    public String requestHeaderDefault(
            @RequestHeader(name = "X-Optional", required = false, defaultValue = "default-header") String header) {
        return header;
    }

    // Item 1d: @CookieValue with defaultValue
    @GetMapping("/cookie-default")
    public String cookieDefault(
            @CookieValue(name = "token", required = false, defaultValue = "default-cookie") String token) {
        return token;
    }

    // Item 2: @RestControllerAdvice JSON error body — exception is handled by MvcRestControllerAdvice
    @GetMapping("/rest-advice-error")
    public void restAdviceError() throws MvcException {
        throw new MvcException("TEST_CODE", "Test error message");
    }

    // Item 3a: content negotiation — JSON response
    @GetMapping(value = "/content", produces = MediaType.APPLICATION_JSON_VALUE)
    public ContentResponse contentJson() {
        return new ContentResponse("content");
    }

    // Item 3b: content negotiation — plain-text response (same path, different produces)
    @GetMapping(value = "/content", produces = MediaType.TEXT_PLAIN_VALUE)
    public String contentText() {
        return "content";
    }

    // Item 3c: consumes constraint — only accepts JSON bodies
    @PostMapping(value = "/consume-json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String consumeJson(@RequestBody ContentResponse body) {
        return "Consumed";
    }

    // Item 4: ProblemDetail (RFC 7807) — explicit handler returning application/problem+json
    @GetMapping("/problem-detail")
    public ResponseEntity<ProblemDetail> problemDetail() {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY, "This is a test problem");
        detail.setTitle("Test Problem");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(detail);
    }

    // Item 6: servlet Filter check — MvcFilter sets X-Filter-Applied header for this path
    @GetMapping("/filter-check")
    public String filterCheck() {
        return "ok";
    }

    // Item 7a: @Qualifier — primary implementation
    @GetMapping("/qualifier/primary")
    public String qualifierPrimary() {
        return primaryService.getValue();
    }

    // Item 7b: @Qualifier — secondary implementation
    @GetMapping("/qualifier/secondary")
    public String qualifierSecondary() {
        return secondaryService.getValue();
    }

    // Item 8: @RequestScope — stores token in the per-request bean, then reads it back
    @GetMapping("/request-scope")
    public String requestScope(@RequestParam(name = "token") String token) {
        requestScopedBean.setToken(token);
        return requestScopedBean.getToken();
    }

    // Item 9: global @InitBinder from MvcGlobalControllerAdvice converts lowercase string to MvcStatus
    @GetMapping("/global-binder")
    public String globalBinder(@RequestParam(name = "status") MvcStatus status) {
        return status.name();
    }

    // Item 10a: @MatrixVariable — single variable extracted from path segment
    @GetMapping("/matrix/{segment}")
    public String matrixVariable(
            @PathVariable(name = "segment") String segment,
            @MatrixVariable(name = "city") String city) {
        return city;
    }

    // Item 10b: @MatrixVariable — multiple variables from same segment
    @GetMapping("/matrix-multi/{segment}")
    public String matrixVariableMultiple(
            @PathVariable(name = "segment") String segment,
            @MatrixVariable(name = "color") String color,
            @MatrixVariable(name = "year") String year) {
        return color + "-" + year;
    }
}
