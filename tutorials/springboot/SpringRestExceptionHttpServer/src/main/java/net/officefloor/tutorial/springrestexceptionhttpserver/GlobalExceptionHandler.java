package net.officefloor.tutorial.springrestexceptionhttpserver;

import net.officefloor.plugin.section.clazz.Parameter;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

// START SNIPPET: tutorial
public class GlobalExceptionHandler {

    public void handleBadRequest(
            @Parameter EscalationException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setDetail(ex.getMessage());
        response.send(ResponseEntity.badRequest().body(pd));
    }

    public void handleNotFound(
            @Parameter EscalationNotFoundException ex,
            ObjectResponse<ResponseEntity<ProblemDetail>> response) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        pd.setDetail(ex.getMessage());
        response.send(ResponseEntity.status(HttpStatus.NOT_FOUND).body(pd));
    }
}
// END SNIPPET: tutorial
