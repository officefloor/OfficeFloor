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

package net.officefloor.spring.starter.rest.web.officefloor;

import net.officefloor.spring.starter.rest.web.common.MockComponent;
import net.officefloor.web.ObjectResponse;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Service using {@link PathVariable} Integer with a Spring bean alongside it.
 * This combination replicates the real-server usage pattern (e.g. PetClinic GetOwnerService)
 * that triggers the mockMvc bean lookup bug in ModelAndViewBridge.processDispatchResult.
 */
public class PathVariableIntegerService {
    public void service(@PathVariable(name = "id") Integer id,
            MockComponent component,
            ObjectResponse<String> response) {
        response.send("ID=" + id + ":" + component.getValue());
    }
}
