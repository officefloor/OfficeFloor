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
 * automatically, registered by name, and applied to REST service functions via
 * {@code govern:} in their REST YAML configuration.
 *
 * <p>The governance extension type is {@link TrackingExtension}.  {@link TrackingComponent}
 * is a Spring bean implementing that interface, making it a governed managed object in any
 * function decorated with a governance that uses {@link TrackingExtension}.  The test reads
 * {@link TrackingComponent#notificationCount} from the response body to confirm that the
 * correct number of {@code @Govern} callbacks fired before the service method executed.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class OfficeFloorGovernanceTest extends AbstractMockMvcVerification {

    @Autowired
    TrackingComponent trackingComponent;

    @BeforeEach
    void resetTrackingState() {
        trackingComponent.notificationCount = 0;
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
                .andExpect(content().string(equalTo("2")));
    }
}
