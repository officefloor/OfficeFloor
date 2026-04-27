package net.officefloor.spring.starter.rest.thymeleaf.officefloor;

import net.officefloor.spring.starter.rest.view.ViewResponse;
import org.springframework.ui.Model;

import java.util.List;

public class EachService {
    public void service(Model model, ViewResponse response) {
        model.addAttribute("items", List.of("Alpha", "Beta", "Gamma"));
        response.send("each");
    }
}
