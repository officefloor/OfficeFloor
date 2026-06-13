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

package net.officefloor.spring.starter.rest.escalation;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirms escalation handlers declared in {@code officefloor/escalation/} YAML files are loaded
 * automatically and invoked when REST service functions throw matching exception types.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class OfficeFloorEscalationTest extends AbstractMockMvcVerification {

    @Test
    public void checkedExceptionIsHandledByEscalation() throws Exception {
        this.mvc.perform(get(this.getPath("/error")))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("ESCALATION: TEST")));
    }

    @Test
    public void uncheckedExceptionIsHandledByEscalation() throws Exception {
        this.mvc.perform(get(this.getPath("/unchecked")))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("UNCHECKED: UNCHECKED")));
    }
}
