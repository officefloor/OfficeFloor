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

package net.officefloor.spring.starter.rest.web.spring;

import jakarta.servlet.http.HttpServletRequest;
import net.officefloor.spring.starter.rest.web.common.BindingTypes;
import net.officefloor.spring.starter.rest.web.common.MockException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.beans.PropertyEditorSupport;

@RestControllerAdvice
public class WebRestControllerAdvice {

    @ExceptionHandler(MockException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMockException(MockException ex, HttpServletRequest request) {
        return request.getRequestURI() + ": " + ex.getMessage();
    }

    @InitBinder
    public void configureBinder(WebDataBinder binder) {
        binder.registerCustomEditor(BindingTypes.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                this.setValue(BindingTypes.valueOf(text.toUpperCase()));
            }
        });
    }

}
