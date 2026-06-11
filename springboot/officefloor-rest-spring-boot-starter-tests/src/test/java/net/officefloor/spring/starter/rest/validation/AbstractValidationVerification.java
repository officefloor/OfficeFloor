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

package net.officefloor.spring.starter.rest.validation;

import net.officefloor.spring.starter.rest.AbstractMockMvcVerification;
import net.officefloor.spring.starter.rest.validation.common.MultiValidRequest;
import net.officefloor.spring.starter.rest.validation.common.ValidRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.equalTo;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public abstract class AbstractValidationVerification extends AbstractMockMvcVerification {

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void valid() throws Exception {
        this.mvc.perform(post(this.getPath("/valid")).accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ValidRequest(0)))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void bindingResult() throws Exception {
        this.mvc.perform(post(this.getPath("/bindingResult")).accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ValidRequest(0)))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Errors: 1")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void multipleErrors() throws Exception {
        this.mvc.perform(post(this.getPath("/multipleErrors")).accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new MultiValidRequest("", 0, "not-an-email", "x")))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(equalTo("Errors: 4")));
    }

    @Test
    @WithMockUser(username = "User", roles = "USER")
    public void constraintTypes() throws Exception {
        this.mvc.perform(post(this.getPath("/multipleErrors")).accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new MultiValidRequest("Valid", 5, "test@example.com", "abc")))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("OK")));
    }

}
