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

package net.officefloor.spring.reactive;

import java.util.function.Consumer;

import org.springframework.web.reactive.function.client.WebClientResponseException;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.server.http.HttpException;
import net.officefloor.server.http.HttpStatus;
import net.officefloor.web.ObjectResponse;
import net.officefloor.woof.WoOF;

/**
 * Utility methods for {@link WoOF} working with Spring Reactive.
 * 
 * @author Daniel Sagenschneider
 */
public class ReactiveWoof {

	/**
	 * Provides handling for on error.
	 * 
	 * @param flow {@link AsynchronousFlow}.
	 * @return {@link Consumer} for error handling.
	 */
	public static Consumer<? super Throwable> flowError(AsynchronousFlow flow) {
		return (error) -> flow.complete(() -> {
			throw error;
		});
	};

	/**
	 * Provides handling of an error and propagates
	 * 
	 * @param flow {@link AsynchronousFlow}.
	 * @return {@link Consumer} for error handling.
	 */
	public static Consumer<? super Throwable> propagateHttpError(AsynchronousFlow flow) {
		return (error) -> flow.complete(() -> {

			// Attempt to translate to HTTP exception
			if (error instanceof WebClientResponseException) {
				WebClientResponseException responseException = (WebClientResponseException) error;
				throw new HttpException(HttpStatus.getHttpStatus(responseException.getStatusCode().value()),
						responseException);
			}

			// Propagate the raw error
			throw error;
		});
	}

	/**
	 * Sends the response.
	 * 
	 * @param flow     {@link AsynchronousFlow}.
	 * @param response {@link ObjectResponse}.
	 * @return {@link Consumer} for sending the response.
	 */
	public static <T> Consumer<? super T> send(AsynchronousFlow flow, ObjectResponse<T> response) {
		return (payload) -> flow.complete(() -> response.send(payload));
	}

}
