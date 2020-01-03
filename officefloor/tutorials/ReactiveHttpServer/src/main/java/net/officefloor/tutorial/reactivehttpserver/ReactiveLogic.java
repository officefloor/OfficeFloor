/*-
 * #%L
 * Reactive Tutorial
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

package net.officefloor.tutorial.reactivehttpserver;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.spring.reactive.ReactiveWoof;
import net.officefloor.web.ObjectResponse;

/**
 * Reactive logic.
 * 
 * @author Daniel Sagenschneider
 */
// START SNIPPET: tutorial
public class ReactiveLogic {

	public void reactive(WebClient client, AsynchronousFlow flow, ObjectResponse<ServerResponse> response) {
		client.get().uri("http://localhost:7878/server").accept(MediaType.APPLICATION_JSON).retrieve()
				.bodyToMono(ServerResponse.class)
				.subscribe(ReactiveWoof.send(flow, response), ReactiveWoof.propagateHttpError(flow));
	}
}
// END SNIPPET: tutorial
