package net.officefloor.spring.starter.rest.security.officefloor;

import net.officefloor.spring.starter.rest.view.ViewResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

public class GreetingService {
    public void service(@RequestParam(name="name", required = false, defaultValue = "World") String name, Model model, ViewResponse response) {
        model.addAttribute("name", name);
        response.send("greeting");
    }
}
