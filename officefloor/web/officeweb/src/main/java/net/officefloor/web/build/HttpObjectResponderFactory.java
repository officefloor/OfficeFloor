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
package net.officefloor.web.build;

import net.officefloor.frame.api.escalate.Escalation;

/**
 * Factory for the creation of {@link HttpObjectResponder} instances.
 * 
 * @author Daniel Sagenschneider
 */
public interface HttpObjectResponderFactory {

	/**
	 * Obtains the <code>Content-Type</code> supported by the create
	 * {@link HttpObjectResponder} instances.
	 * 
	 * @return <code>Content-Type</code>.
	 */
	String getContentType();

	/**
	 * Creates the {@link HttpObjectResponder} for the {@link Object} type.
	 * 
	 * @param objectType
	 *            {@link Object} type.
	 * @return {@link HttpObjectResponder} for the {@link Object} type.
	 */
	<T> HttpObjectResponder<T> createHttpObjectResponder(Class<T> objectType);

	/**
	 * Creates the {@link HttpObjectResponder} for the {@link Escalation} type.
	 * 
	 * @param escalationType
	 *            {@link Escalation} type.
	 * @return {@link HttpObjectResponder} for the {@link Escalation} type.
	 */
	<E extends Throwable> HttpObjectResponder<E> createHttpEscalationResponder(Class<E> escalationType);

}