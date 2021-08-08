/*-
 * #%L
 * Spring WebClient
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
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

package net.officefloor.spring.webclient;

import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.Builder;

import net.officefloor.frame.api.source.SourceContext;

/**
 * Factory to create custom {@link WebClient} {@link Builder}.
 * 
 * @author Daniel Sagenschneider
 */
public interface WebClientBuilderFactory {

	/**
	 * Creates a custom {@link WebClient} {@link Builder}.
	 * 
	 * @param context {@link SourceContext}.
	 * @return {@link Builder}.
	 * @throws Exception If fails to create {@link Builder}.
	 */
	Builder createWebClientBuilder(SourceContext context) throws Exception;

}
