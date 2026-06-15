/*-
 * #%L
 * OfficeFloor REST Spring Boot Starter
 * %%
 * Copyright (C) 2005 - 2026 Daniel Sagenschneider
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package net.officefloor.spring.starter.rest.security.spring;

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

    @GetMapping("/authorize")
    @ResponseBody
    @PreAuthorize("hasRole('ACCESS')")
    public String authorize() {
        return "Accessed";
    }

    @GetMapping("/authorized/child")
    @ResponseBody
    @PreAuthorize("hasRole('ACCESS')")
    public String authorizedChild() {
        return "Accessed";
    }

    @GetMapping("/authorized/override")
    @ResponseBody
    @PreAuthorize("hasRole('ADMIN')")
    public String authorizedOverride() {
        return "Accessed";
    }

    @GetMapping("/authorized/open")
    @ResponseBody
    public String authorizedOpen() {
        return "Accessed";
    }

    @GetMapping("/authorized/open/child")
    @ResponseBody
    public String authorizedOpenChild() {
        return "Accessed";
    }

}
