package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.spring.starter.rest.mvc.common.ContentResponse;
import net.officefloor.web.ObjectResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;

public class ContentService {
    public void service(@RequestHeader(name = "accept") String accept,
                        ObjectResponse<ContentResponse> jsonResponse,
                        ObjectResponse<String> textResponse,
                        ObjectResponse<ResponseEntity<String>> notSupportedResponse) {

        /*
        Note that OfficeFloor uses different responders based on accept type.

        To be compatible with Spring, need to manually handle accept type when response
        type is not compatible.
         */

        // Relying on Spring response handling, so need to provide appropriate type
        MediaType acceptMediaType = MediaType.parseMediaType(accept);
        if (MediaType.TEXT_PLAIN.isCompatibleWith(acceptMediaType)) {
            textResponse.send("content");
        } else if (MediaType.APPLICATION_JSON.isCompatibleWith(acceptMediaType)) {
            jsonResponse.send(new ContentResponse("content"));
        } else {
            notSupportedResponse.send(ResponseEntity.status(406).body("Media type " + accept + " not supported"));
        }
    }
}
