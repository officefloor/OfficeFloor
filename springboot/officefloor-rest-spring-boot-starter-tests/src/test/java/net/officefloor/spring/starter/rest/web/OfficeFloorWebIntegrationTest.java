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

package net.officefloor.spring.starter.rest.web;

import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests OfficeFloor web features via a real embedded server ({@code RANDOM_PORT}),
 * which means no {@code MockMvc} bean is present in the application context.
 * <p>
 * This tests the fix for the bug in {@code ModelAndViewBridge.processDispatchResult}
 * where {@code getBean("mockMvc")} was called unconditionally, throwing
 * {@code NoSuchBeanDefinitionException} when not running under MockMvc.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OfficeFloorWebIntegrationTest extends AbstractWebIntegrationVerification {
}
