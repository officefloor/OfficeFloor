package net.officefloor.spring.starter.rest.web.officefloor;

import net.officefloor.web.ObjectResponse;
import org.springframework.beans.factory.annotation.Value;

public class PropertyValueService {
    public void service(@Value("${officefloor.spring.test.value}") String propertyValue, ObjectResponse<String> response) {
        response.send(propertyValue);
    }
}
