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

package net.officefloor.spring.starter.rest.validation.officefloor;

import jakarta.validation.Valid;
import net.officefloor.spring.starter.rest.validation.common.ValidRequest;
import org.springframework.web.bind.annotation.RequestBody;

import static org.junit.jupiter.api.Assertions.fail;

public class ValidService {
    public void service(@Valid @RequestBody ValidRequest request) {
        fail("Should not be invoked");
    }
}
