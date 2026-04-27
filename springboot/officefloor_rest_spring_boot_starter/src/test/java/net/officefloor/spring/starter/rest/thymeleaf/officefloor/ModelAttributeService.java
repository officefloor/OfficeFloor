package net.officefloor.spring.starter.rest.thymeleaf.officefloor;

import net.officefloor.spring.starter.rest.thymeleaf.common.UserModelAttribute;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.ModelAttribute;

public class ModelAttributeService {
    public void service(@ModelAttribute UserModelAttribute attributes, ObjectResponse<String> response) {
        response.send("name=" + attributes.getName() + ", email=" + attributes.getEmail());
    }
}
