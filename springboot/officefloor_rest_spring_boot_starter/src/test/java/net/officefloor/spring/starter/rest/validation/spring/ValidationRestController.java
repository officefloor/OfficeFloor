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

package net.officefloor.spring.starter.rest.validation.spring;

import jakarta.validation.Valid;
import net.officefloor.spring.starter.rest.validation.common.MultiValidRequest;
import net.officefloor.spring.starter.rest.validation.common.ValidRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.fail;

@RestController
@RequestMapping("/spring/validation")
public class ValidationRestController {

    @PostMapping("/valid")
    public String valid(@Valid @RequestBody ValidRequest request) {
        return fail("Should not be invoked");
    }

    @PostMapping("/bindingResult")
    public ResponseEntity<String> bindingResult(@Valid @RequestBody ValidRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Errors: " + result.getErrorCount());
        }
        return ResponseEntity.ok("OK");
    }

    @PostMapping("/multipleErrors")
    public ResponseEntity<String> multipleErrors(@Valid @RequestBody MultiValidRequest request, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("Errors: " + result.getErrorCount());
        }
        return ResponseEntity.ok("OK");
    }

}
