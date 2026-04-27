package net.officefloor.spring.starter.rest.web.officefloor;

import net.officefloor.spring.starter.rest.web.common.BindingTypes;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.RequestParam;

public class InitBinderService {
    public void service(@RequestParam(name = "status") BindingTypes types, ObjectResponse<String> response) {
        String responseText = switch (types) {
            case START -> "begin";
            case COMPLETE -> "end";
            default -> null;
        };
        response.send(responseText);
    }
}
