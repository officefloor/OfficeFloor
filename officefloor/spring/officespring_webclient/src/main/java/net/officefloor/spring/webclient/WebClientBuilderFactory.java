/*-
 * #%L
 * Spring WebClient
 * %%
 * Copyright (C) 2005 - 2020 Daniel Sagenschneider
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
