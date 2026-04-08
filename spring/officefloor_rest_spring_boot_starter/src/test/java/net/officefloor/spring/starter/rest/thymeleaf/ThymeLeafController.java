package net.officefloor.spring.starter.rest.thymeleaf;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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

}
