package net.officefloor.spring.starter.rest.security;

import jakarta.annotation.security.RolesAllowed;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/spring/security")
public class SecurityController {

    @GetMapping("/greeting")
    public String greeting(@RequestParam(name="name", required = false, defaultValue = "World") String name, Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }

    @GetMapping("/hello/{name}")
    @ResponseBody
    public String hello(@PathVariable(name="name") String name) {
        return "Hello " + name;
    }

    @GetMapping("/userDetails")
    @ResponseBody
    public String userDetails(@AuthenticationPrincipal UserDetails userDetails) {
        return userDetails.getUsername();
    }

    @GetMapping("/authentication")
    @ResponseBody
    public String authentication(Authentication authentication) {
        return authentication.getName();
    }

    @GetMapping("/preAuthorize")
    @ResponseBody
    @PreAuthorize("hasRole('ACCESS')")
    public String preAuthorize() {
        return "Accessed";
    }

    @GetMapping("/postAuthorize")
    @ResponseBody
    @PostAuthorize("hasRole('ACCESS')")
    public String postAuthorize() {
        return "Accessed";
    }

    @GetMapping("/secured")
    @ResponseBody
    @Secured("ROLE_ACCESS")
    public String secured() {
        return "Accessed";
    }

    @GetMapping("/rolesAllowed")
    @ResponseBody
    @RolesAllowed("ACCESS")
    public String rolesAllowed() {
        return "Accessed";
    }

}
