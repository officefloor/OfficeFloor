/*-
 * #%L
 * Web Plug-in
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

package net.officefloor.web.accept;

import java.util.LinkedList;
import java.util.List;

import net.officefloor.web.accept.AcceptNegotiatorImpl.AcceptHandler;
import net.officefloor.web.build.AcceptNegotiatorBuilder;
import net.officefloor.web.build.NoAcceptHandlersException;

/**
 * {@link AcceptNegotiatorBuilder} implementation.
 * 
 * @author Daniel Sagenschneider
 */
public class AcceptNegotiatorBuilderImpl<H> implements AcceptNegotiatorBuilder<H> {

	/**
	 * {@link AcceptHandler} instances.
	 */
	private final List<AcceptHandler<H>> acceptHandlers = new LinkedList<>();

	/*
	 * =================== AcceptNegotiatorBuilder =============================
	 */

	@Override
	public void addHandler(String contentType, H handler) {
		this.acceptHandlers.add(AcceptNegotiatorImpl.createAcceptHandler(contentType, handler));
	}

	@Override
	@SuppressWarnings("unchecked")
	public AcceptNegotiator<H> build() throws NoAcceptHandlersException {

		// Ensure have at least one accept handler
		if (this.acceptHandlers.size() == 0) {
			throw new NoAcceptHandlersException("Must have at least one " + AcceptHandler.class.getSimpleName()
					+ " configured for the " + AcceptNegotiator.class.getSimpleName());
		}

		// Return the negotiator
		return new AcceptNegotiatorImpl<>(this.acceptHandlers.toArray(new AcceptHandler[this.acceptHandlers.size()]));
	}

}
