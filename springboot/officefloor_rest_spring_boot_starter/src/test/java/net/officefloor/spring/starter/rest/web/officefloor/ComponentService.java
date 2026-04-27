package net.officefloor.spring.starter.rest.web.officefloor;

import net.officefloor.spring.starter.rest.web.common.MockComponent;
import net.officefloor.web.ObjectResponse;

public class ComponentService {
    public void service(MockComponent component, ObjectResponse<String> response) {
        response.send(component.getValue());
    }
}
