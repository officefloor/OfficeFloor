/*
 * OfficeFloor - http://www.officefloor.net
 * Copyright (C) 2005-2017 Daniel Sagenschneider
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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