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

package net.officefloor.spring.starter.rest.govern;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Confirms governances declared in {@code officefloor/govern/} YAML files are loaded
 * automatically, registered by name in ComposeArchitect, and applied to REST service
 * functions via {@code govern:} in their REST YAML configuration.
 *
 * <p>Each governance uses a distinct extension interface so that each governs only its own
 * dedicated Spring bean.  This avoids the OfficeFloor frame limitation where multiple
 * governances sharing an extension interface on the same managed object require globally
 * sequential indices that are not guaranteed when other governances (e.g. transaction)
 * are registered first.
 *
 * <p>The {@code @Govern} callback fires before the service method, so the governance
 * counts are already incremented when the service reads them.  The response body carries
 * the evidence that the correct number of governance callbacks fired.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class OfficeFloorGovernanceTest extends AbstractMockMvcVerification {

    @Autowired
    TrackingComponent trackingComponent;

    @Autowired
    AuditComponent auditComponent;

    @BeforeEach
    void resetGovernanceState() {
        trackingComponent.notificationCount = 0;
        auditComponent.auditCount = 0;
    }

    @Test
    public void classBasedGovernanceFromYamlIsApplied() throws Exception {
        this.mvc.perform(get(this.getPath("/governed")))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("1")));
    }

    @Test
    public void multipleGovernancesFromDirectoryAreAllApplied() throws Exception {
        this.mvc.perform(get(this.getPath("/multi-governed")))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("1:1")));
    }
}
