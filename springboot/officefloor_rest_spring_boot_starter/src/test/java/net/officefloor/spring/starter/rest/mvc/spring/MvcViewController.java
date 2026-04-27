package net.officefloor.spring.starter.rest.mvc.spring;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/spring/mvc")
public class MvcViewController {

    // Item 5a: redirect via "redirect:" view name — requires @Controller (not @RestController)
    @GetMapping("/redirect")
    public String redirect() {
        return "redirect:/mvc-destination";
    }

    // Item 5b: redirect with query parameter added via RedirectAttributes
    @GetMapping("/redirect-attrs")
    public String redirectAttributes(RedirectAttributes attrs) {
        attrs.addAttribute("status", "ok");
        return "redirect:/mvc-destination";
    }
}
