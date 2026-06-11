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

package net.officefloor.spring.starter.rest.response;

import net.officefloor.spring.starter.rest.SpringServerHttpConnection;

/**
 * <p>
 * Handler for exceptions from {@link org.springframework.web.servlet.DispatcherServlet}.
 * <p>
 * Typically this is for {@link jakarta.servlet.FilterChain} handling.
 */
public interface SpringExceptionHandler {

    /**
     * Handles the {@link Exception} from {@link org.springframework.web.servlet.DispatcherServlet}.
     *
     * @param exception  {@link Throwable} from {@link org.springframework.web.servlet.DispatcherServlet}.
     * @param connection {@link SpringServerHttpConnection}.
     * @return <code>true</code> if handled.
     * @throws Exception If fails to handle.
     */
    boolean handle(Throwable exception, SpringServerHttpConnection connection) throws Exception;
}
