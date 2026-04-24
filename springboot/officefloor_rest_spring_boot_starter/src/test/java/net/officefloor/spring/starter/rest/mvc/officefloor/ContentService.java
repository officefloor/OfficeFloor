package net.officefloor.spring.starter.rest.mvc.officefloor;

import jakarta.servlet.http.HttpServletRequest;
import net.officefloor.spring.starter.rest.mvc.common.ContentResponse;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class ContentService {
    public void service(HttpServletRequest request, ObjectResponse<ResponseEntity<?>> response) {
        String accept = request.getHeader("Accept");
        if (accept != null && accept.contains(MediaType.TEXT_PLAIN_VALUE)) {
            response.send(ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("content"));
        } else if (accept != null && accept.contains(MediaType.APPLICATION_XML_VALUE)
                && !accept.contains(MediaType.APPLICATION_JSON_VALUE)
                && !accept.contains(MediaType.TEXT_PLAIN_VALUE)) {
            response.send(ResponseEntity.status(406).build());
        } else {
            response.send(ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ContentResponse("content")));
        }
    }
}
