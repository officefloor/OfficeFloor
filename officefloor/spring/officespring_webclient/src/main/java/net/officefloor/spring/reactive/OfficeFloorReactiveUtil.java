/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2019 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package net.officefloor.spring.reactive;

import java.util.function.Consumer;

import net.officefloor.frame.api.function.AsynchronousFlow;
import net.officefloor.frame.api.manage.OfficeFloor;
import net.officefloor.web.ObjectResponse;

/**
 * Utility methods for {@link OfficeFloor} working with Spring Reactive.
 * 
 * @author Daniel Sagenschneider
 */
public class OfficeFloorReactiveUtil {

	/**
	 * Provides handling for on error.
	 * 
	 * @param flow {@link AsynchronousFlow}.
	 * @return {@link Consumer} for error handling.
	 */
	public static Consumer<? super Throwable> onError(AsynchronousFlow flow) {
		return (error) -> flow.complete(() -> {
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