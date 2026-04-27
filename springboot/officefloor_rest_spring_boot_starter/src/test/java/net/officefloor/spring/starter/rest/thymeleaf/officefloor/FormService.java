package net.officefloor.spring.starter.rest.thymeleaf.officefloor;

import net.officefloor.spring.starter.rest.thymeleaf.common.UserModelAttribute;
import net.officefloor.spring.starter.rest.view.ViewResponse;
import org.springframework.ui.Model;

public class FormService {
    public void service(Model model, ViewResponse response) {
        UserModelAttribute user = new UserModelAttribute();
        user.setName("Form");
        user.setEmail("form@test.com");
        model.addAttribute("user", user);
        response.send("form");
    }
}
