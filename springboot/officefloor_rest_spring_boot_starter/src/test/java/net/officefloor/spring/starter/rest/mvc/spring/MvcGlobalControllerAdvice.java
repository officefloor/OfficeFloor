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

package net.officefloor.spring.starter.rest.mvc.spring;

import net.officefloor.spring.starter.rest.mvc.common.MvcStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

import java.beans.PropertyEditorSupport;

/**
 * Plain {@link ControllerAdvice} (not {@link org.springframework.web.bind.annotation.RestControllerAdvice})
 * registering a global {@link InitBinder} that applies to all controllers.
 */
@ControllerAdvice
public class MvcGlobalControllerAdvice {

    @InitBinder
    public void configureBinder(WebDataBinder binder) {
        binder.registerCustomEditor(MvcStatus.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                setValue(MvcStatus.valueOf(text.toUpperCase()));
            }
        });
    }
}
