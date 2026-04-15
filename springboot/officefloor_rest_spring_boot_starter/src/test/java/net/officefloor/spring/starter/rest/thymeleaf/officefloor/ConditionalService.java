package net.officefloor.spring.starter.rest.thymeleaf.officefloor;

import net.officefloor.spring.starter.rest.view.ViewResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

public class ConditionalService {
    public void service(@RequestParam(name = "visible") boolean isVisible,
                        Model model,
                        ViewResponse response) {
        model.addAttribute("visible", isVisible);
        response.send("conditional");
    }
}
