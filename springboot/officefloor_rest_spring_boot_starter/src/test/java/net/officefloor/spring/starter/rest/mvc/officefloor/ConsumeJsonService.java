package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.spring.starter.rest.mvc.common.ContentResponse;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestBody;

public class ConsumeJsonService {
    public void service(@RequestBody ContentResponse body, ObjectResponse<String> response) {
        response.send("Consumed");
    }
}
