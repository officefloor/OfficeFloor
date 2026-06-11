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

package net.officefloor.spring.starter.rest.thymeleaf.officefloor;

import net.officefloor.spring.starter.rest.view.ViewResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;

public class ThymeLeafService {
    public void service(@RequestParam(name="name", required = false, defaultValue = "World") String name, Model model, ViewResponse response) {
        model.addAttribute("name", name);
        response.send("thymeleaf");
    }
}
