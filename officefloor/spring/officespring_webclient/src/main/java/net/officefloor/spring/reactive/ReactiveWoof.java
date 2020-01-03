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