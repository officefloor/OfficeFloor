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
