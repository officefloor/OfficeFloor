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

package net.officefloor.spring.starter.rest.supplier;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirms suppliers declared in {@code officefloor/suppliers/} YAML files are loaded
 * automatically and their supplied objects are injectable into OfficeFloor REST service methods.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class OfficeFloorSupplierTest extends AbstractMockMvcVerification {

    @Test
    public void supplierProvidesInjectedObject() throws Exception {
        this.mvc.perform(get(this.getPath("/simple")))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("supplied")));
    }

    @Test
    public void multipleSuppliersFromDirectoryAreAllLoaded() throws Exception {
        this.mvc.perform(get(this.getPath("/multiple")))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("supplied:another-supplied")));
    }
}
