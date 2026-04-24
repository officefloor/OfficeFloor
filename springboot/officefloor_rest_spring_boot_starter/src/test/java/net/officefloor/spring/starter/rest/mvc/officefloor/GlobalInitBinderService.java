package net.officefloor.spring.starter.rest.mvc.officefloor;

import net.officefloor.spring.starter.rest.mvc.common.MvcStatus;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestParam;

public class GlobalInitBinderService {
    public void service(@RequestParam MvcStatus status, ObjectResponse<String> response) {
        response.send(status.name());
    }
}
