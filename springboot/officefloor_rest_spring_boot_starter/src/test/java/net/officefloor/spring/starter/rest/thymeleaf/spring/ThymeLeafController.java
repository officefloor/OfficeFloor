package net.officefloor.spring.starter.rest.thymeleaf.spring;

import net.officefloor.spring.starter.rest.thymeleaf.common.UserModelAttribute;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/spring/thymeleaf")
public class ThymeLeafController {

    @GetMapping("/thymeleaf")
    public String thymeleaf(@RequestParam(name="name", required = false, defaultValue = "World") String name, Model model) {
        model.addAttribute("name", name);
        return "thymeleaf";
    }

    @PostMapping("/modelAttribute")
    @ResponseBody
    public String modelAttribute(@ModelAttribute UserModelAttribute attributes) {
        return "name=" + attributes.getName() + ", email=" + attributes.getEmail();
    }

    @GetMapping("/each")
    public String each(Model model) {
        model.addAttribute("items", List.of("Alpha", "Beta", "Gamma"));
        return "each";
    }

    @GetMapping("/conditional")
    public String conditional(@RequestParam("visible") boolean isVisible, Model model) {
        model.addAttribute("visible", isVisible);
        return "conditional";
    }

    @GetMapping("/secure")
    public String secure() {
        return "secure";
    }

    @GetMapping("/form")
    public String form(Model model) {
        UserModelAttribute user = new UserModelAttribute();
        user.setName("Form");
        user.setEmail("form@test.com");
        model.addAttribute("user", user);
        return "form";
    }

}
